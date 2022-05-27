package main

import (
	"log"
	"net/http"
	"net/url"
)

func main() {
	//@Identifier
	identifier := url.Values{
		"Name": {"firstname lastname"},
	}
	http.PostForm("http://test.com/data", identifier)
}
