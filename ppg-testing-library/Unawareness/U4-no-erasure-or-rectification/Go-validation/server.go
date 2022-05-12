package main

import (
	"fmt"
	"net/http"

	"github.com/gin-contrib/logger"
	"github.com/gin-gonic/gin"
)

type Data struct {
	Name string
}

func main() {
	http.ListenAndServe(":8080", NewRouter())
}

func NewRouter() *gin.Engine {
	r := gin.New()
	r.Use(gin.Recovery())
	r.Use(logger.SetLogger())

    // All methods for submitting, retrieving, modifying, and deleting personal data are foreseen, so no Unawareness threat should be detected
	r.POST("/data", post_data)
	r.GET("/data", get_data)
	r.PUT("/data", put_data)
	r.DELETE("/data", delete_data)

	return r
}

func post_data(c *gin.Context) {
	// TODO
}

func put_data(c *gin.Context) {
	// TODO
}

func get_data(c *gin.Context) {
	// TODO
}

func delete_data(c *gin.Context) {
	// TODO
}
