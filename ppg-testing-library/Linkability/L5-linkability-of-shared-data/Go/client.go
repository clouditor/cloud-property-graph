package main

import (
	"net/http"
	"net/url"
)

func main() {
	//@PseudoIdentifier
	identifier := url.Values{
		"name": {"firstname lastname"},
	}
	http.PostForm("http://test.com/data", identifier)
}
