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
		"name": {"firstname lastname"},
	}
    url := "http://test.com/data"
	resp, err := http.PostForm(url, identifier)
	if err != nil {
		log.Fatal(err)
	}
}
