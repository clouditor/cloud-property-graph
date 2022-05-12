package main

import (
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/rs/zerolog/log"
)

type Message struct {
	Name      string
	Joke      string
}

func main() {
	http.ListenAndServe(":8080", NewRouter())
}

func NewRouter() *gin.Engine {
	r := gin.New()
	r.Use(gin.Recovery())
	r.Use(logger.SetLogger())

	r.POST("/data", parse_data)
	r.POST("/data1", parse_data1)

	return r
}

func parse_data(c *gin.Context) {
	var message Message

	if err := c.ShouldBindJSON(&message); err != nil {
		fmt.Println("error")
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	// TODO process message somehow
}

func parse_data1(c *gin.Context) {
	var message Message

	if err := c.ShouldBindJSON(&message); err != nil {
		fmt.Println("error")
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	// here, message is not further processed
}

func process(){
}