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
	var err error

    url := "http://test.com/data"
    //@Identifier
	name := "firstname lastname"
    joke = "My grandpa always used to say 'as one door closes, another one opens.' A lovely man. A terrible cabinet maker."

	message := &Message{
	    Name: name,
	    Joke: joke,
	}

	reqBody, _ := json.Marshal(message)
	reqBodyBytes := bytes.NewBuffer(reqBody)
	req, _ := http.NewRequest("POST", url, reqBodyBytes)
	client := new(http.Client)
	client.Do(req)
}


func main() {
    query()
}
