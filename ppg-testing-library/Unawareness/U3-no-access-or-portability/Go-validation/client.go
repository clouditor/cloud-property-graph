package main

import (
	"log"
	"net/http"
	"net/url"
)

// working query: MATCH p=(:Identifier)--()-[:DFG*]-(:HttpEndpoint) RETURN p
func main() {
	//@Identifier
	identifier := url.Values{
		"name": {"firstnamelastname"},
	}
	http.PostForm("http://test.com/data", identifier)
	http.Get("http://test.com/data?name=firstnamelastname")
}
