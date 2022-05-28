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

type Message struct{
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

	r.POST("/registration", registration)

	return r
}

func registration(c *gin.Context) {
	c.Request.ParseForm()
	name := c.Request.Form.Get("name")
	message := &Message{
		Name: name,
	}
	err := db.Create(message).error
	// TODO test with sqlite
	if err.Code.Name() == "unique_violation" {
        c.JSON(http.StatusConflict, gin.H{"error": err.Error()})
	}
}
