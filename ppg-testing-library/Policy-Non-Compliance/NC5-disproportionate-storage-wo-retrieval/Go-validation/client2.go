package main

import (
	"net/http"
	"net/url"
)

// a second client makes a get request to retrieve the data (which could also be another microservice in the backend)
func query() {
    http.Get("http://test.com/data?name=firstnamelastname")
}


func main() {
    query()
}
