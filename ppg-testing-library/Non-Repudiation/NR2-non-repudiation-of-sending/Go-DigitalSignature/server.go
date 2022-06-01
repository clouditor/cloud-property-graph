package main

import (
	"fmt"
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

	r.POST("/data", parse_data)

	return r
}

func parse_data(c *gin.Context) {
    c.Request.ParseForm()
    name := c.Request.Form.Get("Personal_datum")
    signature := c.Request.Form.Get("Signature")
	log.Info().Msg(data)
}