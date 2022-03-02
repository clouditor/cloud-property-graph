package main

import (
	"net/http"
	"net/url"
	"log"
)

func main() {
	var err error

	data := url.Values{
	        "name":       {"firstname lastname"},
	}

    resp, err := http.PostForm("http://example.com/data", data)
    if err != nil {
        log.Fatal(err)
    }
}
