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

    // All methods for submitting, retrieving, modifying, and deleting personal data are foreseen, so no Unawareness threat should be detected
	r.POST("/data", post_data)
	r.GET("/data", get_data)
	r.PUT("/data", put_data)
	r.DELETE("/data", delete_data)

	return r
}

func post_data(c *gin.Context) {
	c.Request.ParseForm()
	name := c.Request.Form.Get("name")
	message := &Message{
		Name: name,
	}
	db.Create(message)
}

func put_data(c *gin.Context) {
    c.Request.ParseForm()
    name := c.Request.Form.Get("name")
    // TODO: the type of message is unknown in the graph
    message := &Message{
        Name: name,
    }
    db.Model(&message).Update("name", name)
}

func get_data(c *gin.Context) {
    var message Message

    c.Request.ParseForm()
    name := c.Request.Form.Get("name")
    db.Get().Where("name = ?", name).First(&message).Error
}

func delete_data(c *gin.Context) {
    c.Request.ParseForm()
    name := c.Request.Form.Get("name")
    data := &Data{
        Name: name,
    }
    db.Delete(message)
}
