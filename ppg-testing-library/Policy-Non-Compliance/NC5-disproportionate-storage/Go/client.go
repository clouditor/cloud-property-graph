package main

import (
	"net/http"
)

type Message struct {
	Name      string
}

func query() {
    //@Identifier
    name := "firstname lastname"
    joke := "My grandpa always used to say 'as one door closes, another one opens.' A lovely man. A terrible cabinet maker."

	message := url.Values{
        "Name": {name},
    }
    http.PostForm("http://test.com/data", message)
}

func main() {
    query()
}
