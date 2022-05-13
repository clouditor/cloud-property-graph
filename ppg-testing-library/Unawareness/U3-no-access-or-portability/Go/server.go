package main

import (
	"fmt"
	"net/http"

	"github.com/gin-contrib/logger"
	"github.com/gin-gonic/gin"
	"gorm.io/driver/sqlite"
    "gorm.io/gorm"
)

var db *gorm.DB

type Data struct {
	Name string
}

func main() {
    Init()
	http.ListenAndServe(":8080", NewRouter())
}

func Init() (err error) {
	if db, err = gorm.Open(sqlite.Open("file::memory:?cache=shared"), &gorm.Config{}); err != nil {
		return fmt.Errorf("db sqlite connect: %w", err)
	}
	err = db.AutoMigrate(&Data{})
	if err != nil {
		return err
	}
	return
}

func NewRouter() *gin.Engine {
	r := gin.New()
	r.Use(gin.Recovery())
	r.Use(logger.SetLogger())

	r.POST("/data", post_data)
	// No GET is foreseen for the personal data which is an Unawareness threat

	return r
}

func post_data(c *gin.Context) {
	c.Request.ParseForm()
	name := c.Request.Form.Get("name")
    data := &Data{Name: name}
	db.Create(data)
}
