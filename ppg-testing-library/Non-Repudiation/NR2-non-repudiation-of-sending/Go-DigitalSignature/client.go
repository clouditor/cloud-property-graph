package main

import (
	"crypto/ed25519"
	"crypto/rand"
	"log"
	"net/http"
	"net/url"
)

func main() {
	var err error

	//@Identifier
	name := []byte("firstname lastname")

	// generate signature
	_, priv, err := ed25519.GenerateKey(rand.Reader)
	if err != nil {
		log.Fatal(err)
	}
	signature := ed25519.Sign(priv, name)
	data := url.Values{
		"Name":      {name},
		"Signature": {signature},
	}
	http.PostForm("http://test.com/data", data)
}
