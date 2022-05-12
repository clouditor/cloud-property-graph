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

	r.POST("/data", post_data)
	r.GET("/data", get_data)
	r.PUT("/data", put_data)
	// No DELETE is foreseen for the personal data which is an Unawareness threat

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
