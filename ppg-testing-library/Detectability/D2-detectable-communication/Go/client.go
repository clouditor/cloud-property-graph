package main

import (
	"log"
	"net/http"
	"net/url"
)

func main() {
    //@Identifier
    name := "firstname lastname"
	data := url.Values{
		"Name": {name},
		"Message": {"helloworld"},
	}

	http.PostForm("http://test.com/data", data)
}

