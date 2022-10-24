package main

import (
	"net/http"
	"net/url"
)

func main() {
    //@PseudoIdentifier
    name := "firstname lastname"
	data := url.Values{
		"Name": {name},
		"Message": {"helloworld"},
	}

	http.PostForm("http://test.com/data", data)
}

