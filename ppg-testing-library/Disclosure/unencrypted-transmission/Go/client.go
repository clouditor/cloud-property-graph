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

    // Disclosure threat results from sending personal data to an http address
	resp, err := http.PostForm("http://test.com/data", identifier)
	if err != nil {
		log.Fatal(err)
	}
}
