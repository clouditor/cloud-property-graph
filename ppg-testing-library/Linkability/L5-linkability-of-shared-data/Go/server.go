package main

import (
	"net/http"

	"github.com/gin-contrib/logger"
	"github.com/gin-gonic/gin"
)

func main() {
	http.ListenAndServe(":8080", NewRouter())
}

func NewRouter() *gin.Engine {
	r := gin.New()
	r.Use(gin.Recovery())
	r.Use(logger.SetLogger())

	r.POST("/data", forward_data)

	return r
}

func forward_data(c *gin.Context) {
	c.Request.ParseForm()
	name := c.Request.Form.Get("Name")
	message := c.Request.Form.Get("Message")
	data := url.Values{
		"Name":    {name},
		"Message": {message},
	}
	http.PostForm("http://third-party.com/externaldata", data)
}