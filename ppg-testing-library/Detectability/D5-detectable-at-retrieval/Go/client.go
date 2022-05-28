package main

import (
	"net/http"
)

func main() {
    //@Identifier
	data := url.Values{
		"name": {"firstnamelastname"},
	}
	http.PostForm("http://test.com/data", data)

	http.Get("http://test.com/getdata?name=firstnamelastname")
}
