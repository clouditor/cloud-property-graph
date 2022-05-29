package main

import (
	"net/http"

	"github.com/gin-contrib/logger"
	"github.com/gin-gonic/gin"
	"github.com/rs/zerolog/log"

	"gorm.io/driver/postgres"
    "gorm.io/gorm"
)

var db *gorm.DB

type Data struct {
    Name string
    Message string
}

func main() {
    Init()
	http.ListenAndServe(":8080", NewRouter())
}

func Init() (err error) {
    dsn := fmt.Sprintf("host=%s user=%s password=%s dbname=%s port=5432 sslmode=disable",
        "postgres",
        "postgres",
        "postgres",
        "postgres",
    )

    db = gorm.Open(postgres.Open(dsn), &gorm.Config{})
	db.AutoMigrate(&Message{})
	return
}

func NewRouter() *gin.Engine {
	r := gin.New()
	r.Use(gin.Recovery())
	r.Use(logger.SetLogger())

    r.POST("/data", post_data)
    r.GET("/getdata", get_data)

	return r
}

func post_data(c *gin.Context) {
	c.Request.ParseForm()
	name := c.Request.Form.Get("Name")
	message := c.Request.Form.Get("Message")
    data := &Data{
        Name: name,
        Message: message,
    }
	db.Create(data)
}

func get_data(c *gin.Context) {
    var data Data

    c.Request.ParseForm()
    name := c.Request.Form.Get("Name")
    db.Get().Where("Name = ?", name).First(&data).Error
    c.JSON(http.StatusNotFound, gin.H{"msg":"Not Found"})
}
