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

	db, _ := gorm.Open(postgres.Open(dsn), &gorm.Config{})

	db.AutoMigrate(&Message{})

	return
}

func NewRouter() *gin.Engine {
	r := gin.New()
	r.Use(gin.Recovery())
	r.Use(logger.SetLogger())

	r.POST("/data", parse_data)

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