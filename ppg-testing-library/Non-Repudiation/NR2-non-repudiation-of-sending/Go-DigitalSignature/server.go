package main

import (
	"fmt"
	"net/http"

	"github.com/gin-contrib/logger"
	"github.com/gin-gonic/gin"
	"github.com/rs/zerolog/log"
)

type SignedMessage struct {
	Personal_datum      string `json:"personal_datum"`
	Signature string `json:"signature"`
}

func main() {
	http.ListenAndServe(":8080", NewRouter())
}

func NewRouter() *gin.Engine {
	r := gin.New()
	r.Use(gin.Recovery())
	r.Use(logger.SetLogger())

	r.POST("/data", parse_data)

	return r
}

func parse_data(c *gin.Context) {
	var signedMessage SignedMessage

	if err := c.ShouldBindJSON(&signedMessage); err != nil {
		fmt.Println("error")
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	// non-repudiation threat results from logging the tainted personal data
	log.Info().Msg(fmt.Sprintf("%#v", signedMessage))
}