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
	r.POST("/login", login)

	return r
}

func login(c *gin.Context) {
    // TODO struct
	var (
	    data string
	)
	if err := c.BindJSON(&data); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
	}
	// TODO database request
	c.JSON(http.StatusNotFound)
}
