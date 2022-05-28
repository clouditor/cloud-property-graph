package main

import (
	"fmt"
	"net/http"

	"github.com/gin-contrib/logger"
	"github.com/gin-gonic/gin"
	"github.com/rs/zerolog/log"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
)

var db *gorm.DB

type Message struct {
    Name string
}

func main() {
	Init()
	http.ListenAndServe(":8080", NewRouter())
}

func Init() (err error) {
	host := "postgres"
	user := "postgres"
	password := "postgres"
	dbname := "userdata"

	dsn := fmt.Sprintf("host=%s user=%s password=%s dbname=%s port=5432 sslmode=disable",
		host,
		user,
		password,
		dbname,
	)

	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{})
	if err != nil {
		return err
	}

	err = db.AutoMigrate(&Message{})
	if err != nil {
		return err
	}

	return
}

func NewRouter() *gin.Engine {
	r := gin.New()
	r.Use(gin.Recovery())
	r.Use(logger.SetLogger())

	r.POST("/data", parse_data)
	r.GET("/data", get_data)

	return r
}

func parse_data(c *gin.Context) {
    c.Request.ParseForm()
    name := c.Request.Form.Get("Name")
    message := &Message{
        Name: name,
    }
    // Create the message in the database
    db.Create(message)
}

func get_data(c *gin.Context) {
    var message Message

    c.Request.ParseForm()
    name := c.Request.Form.Get("Name")
    db.Get().Where("Name = ?", name).First(&message).Error
}