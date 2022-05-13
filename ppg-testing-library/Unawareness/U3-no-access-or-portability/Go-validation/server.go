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

    // GET method is available to retrieve the personal data so no U3 Unawareness threat should be detected here
	r.POST("/data", post_data)
	r.GET("/data", get_data)

	return r
}

func post_data(c *gin.Context) {
	c.Request.ParseForm()
    name := c.Request.Form.Get("name")
    data := &Data{Name: name}
    db.Create(data)
}

func get_data(c *gin.Context) {
    var message Message
    c.Request.ParseForm()
    name := c.Request.Form.Get("name")
    db.Get().Where("name = ?", name).First(&message).Error
}
