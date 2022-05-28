package main

import (
	"log"
	"net/http"
	"net/url"
)

func main() {
    //@Identifier
	identifier := url.Values{
		"name": {"firstname lastname"},
	}
	http.PostForm("http://test.com/registration", identifier)
}
