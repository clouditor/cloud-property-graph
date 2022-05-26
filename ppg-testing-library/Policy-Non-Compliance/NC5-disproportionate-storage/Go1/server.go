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
    Name: name,
    Joke: joke,
}

type UserMessage struct {
    Name: name,
}

func main() {
	Init()
	http.ListenAndServe(":8080", NewRouter())
}

func Init() (err error) {
	host := "localhost"
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

    err = db.AutoMigrate(&UserMessage{})
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

	return r
}

func parse_data(c *gin.Context) {
	var message Message
	var err error

	if err = c.ShouldBindJSON(&message); err != nil {
		fmt.Println("error")
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	// Create the message in the database
	db.Create(message)
}