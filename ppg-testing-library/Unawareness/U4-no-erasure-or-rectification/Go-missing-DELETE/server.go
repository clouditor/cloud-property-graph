package main

import (
	"fmt"
	"net/http"

	"github.com/gin-contrib/logger"
	"github.com/gin-gonic/gin"
	"gorm.io/driver/postgres"
    "gorm.io/gorm"
)

type Message struct {
    Name string
}

func main() {
    Init()
	http.ListenAndServe(":8080", NewRouter())
}

var db *gorm.DB

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
	r.GET("/data", get_data)
	r.PUT("/data", put_data)
	// No DELETE is foreseen for the personal data which is an Unawareness threat

	return r
}

func post_data(c *gin.Context) {
	c.Request.ParseForm()
	name := c.Request.Form.Get("name")
	message := &Message{
		Name: name,
	}
	db.Create(message)
}

func put_data(c *gin.Context) {
    c.Request.ParseForm()
    name := c.Request.Form.Get("name")
    // TODO: the type of message is unknown in the graph
    message := &Message{
        Name: name,
    }
    db.Model(&message).Update("name", name)
}

func get_data(c *gin.Context) {
    var message Message

    c.Request.ParseForm()
    name := c.Request.Form.Get("name")
    db.Get().Where("name = ?", name).First(&message).Error
}
