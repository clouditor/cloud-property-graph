package main

import (
	"net/http"
	"net/url"
)

func main() {
	//@PseudoIdentifier
	var name = "firstname lastname"
	//name := "firstname lastname"
	data := url.Values{
		"Name":    {name},
		"Message": {"hello world"},
	}

	http.PostForm("http://test.com/data", data)
}
