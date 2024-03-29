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
	r.DELETE("/data", delete_data)
	// No PUT is foreseen for the personal data which is an Unawareness threat

	return r
}

func delete_data(c *gin.Context) {
    c.Request.ParseForm()
    name := c.Request.Form.Get("name")
    data := &Data{
        Name: name,
    }
    db.Delete(message)
}

func post_data(c *gin.Context) {
	c.Request.ParseForm()
	name := c.Request.Form.Get("name")
	data := &Data{
		Name: name,
	}
	db.Create(message)
}

func get_data(c *gin.Context) {
    var data Data

    c.Request.ParseForm()
    name := c.Request.Form.Get("name")
    db.Get().Where("name = ?", name).First(&data).Error
}