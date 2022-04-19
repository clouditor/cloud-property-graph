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

	resp, err := http.PostForm("http://test.com/data", identifier)
	if err != nil {
		log.Fatal(err)
	}
}
