package main

import (
	"net/http"
)

func main() {
    //@Identifier
	identifier := url.Values{
		"name": {"firstnamelastname"},
	}
	http.PostForm("http://test.com/data", identifier)

	http.Get("http://test.com/data?name=firstnamelastname")
}
