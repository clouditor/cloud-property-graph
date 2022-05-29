package main

import (
	"net/http"
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

