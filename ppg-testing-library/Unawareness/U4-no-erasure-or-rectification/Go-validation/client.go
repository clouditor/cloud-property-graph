package main

import (
	"log"
	"net/http"
	"net/url"
)

func main() {
	var err error

	//@Identifier
	identifier := url.Values{
		"Name": {"firstname lastname"},
	}
	_, err = http.PostForm("http://test.com/data", identifier)
	if err != nil {
		log.Fatal(err)
	}
}
