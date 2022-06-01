package main

import (
	"fmt"
	"net/http"

	"github.com/gin-contrib/logger"
	"github.com/gin-gonic/gin"
	"gorm.io/driver/postgres"
    "gorm.io/gorm"
)

var db *gorm.DB

type Data struct {
	Name string
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
	// No GET is foreseen for the personal data which is an Unawareness threat

	return r
}

func post_data(c *gin.Context) {
	c.Request.ParseForm()
	name := c.Request.Form.Get("name")
    data := &Data{Name: name}
	db.Create(data)
}
