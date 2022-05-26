package main

import (
	"bytes"
	"crypto/ed25519"
	"crypto/rand"
	"encoding/json"
	"log"
	"net/http"
)

type Message struct {
	Name      string
	Joke      string
}

func query() {
    //@Identifier
    name := "firstname lastname"
    joke := "My grandpa always used to say 'as one door closes, another one opens.' A lovely man. A terrible cabinet maker."

	message := url.Values{
        "Name": {name},
        "Joke": {joke},
    }
    http.PostForm("http://test.com/data", message)
}

func main() {
    query()
}
