package main

import (
	"net/http"

	"github.com/gin-contrib/logger"
	"github.com/gin-gonic/gin"
	"github.com/rs/zerolog/log"
)

func main() {
	http.ListenAndServe(":8080", NewRouter())
}

func NewRouter() *gin.Engine {
	r := gin.New()
	r.Use(gin.Recovery())
	r.Use(logger.SetLogger())

    // non-repudiation threat results from receiving the tainted personal data
	r.POST("/data", post_data)

	return r
}

func post_data(c *gin.Context) {
	// nothing to do
}
