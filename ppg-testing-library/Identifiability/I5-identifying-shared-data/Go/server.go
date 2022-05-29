package main

import (
	"net/http"
	"net/url"

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

	r.POST("/data", parse_data)

	return r
}

func parse_data(c *gin.Context) {
    c.Request.ParseForm()
	http.PostForm("http://third-party.com/externaldata", c.Request.Form)
}
