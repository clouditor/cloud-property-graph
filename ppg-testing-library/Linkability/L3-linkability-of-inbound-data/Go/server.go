package main

import (
    "log"
	"net/http"

	"github.com/gin-contrib/logger"
	"github.com/gin-gonic/gin"
	"github.com/rs/zerolog/log"
)

type Data struct {
	Name    string
	Message string
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
	c.Request.ParseForm()
	name := c.Request.Form.Get("Name")
	message := c.Request.Form.Get("Message")
	data := &Data{
		Name:    name,
		Message: message,
	}
	log.Info().Msg(data.Name)
}
