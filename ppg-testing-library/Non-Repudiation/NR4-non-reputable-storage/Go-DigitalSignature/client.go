package main

import (
	"bytes"
	"crypto/ed25519"
	"crypto/rand"
	"encoding/json"
	"log"
	"net/http"
)

type SignedMessage struct {
	Personal_datum      string
	Signature string
}

func main() {
	var err error
	//@Identifier
	pd := "firstname lastname"

	//@Identifier
	personal_datum := []byte("firstname lastname")

	// generate signature
	_, priv, err := ed25519.GenerateKey(rand.Reader)
	if err != nil {
		log.Fatal(err)
	}
	signature := ed25519.Sign(priv, personal_datum)
	data := url.Values{
		Personal_datum:      personal_datum,
		Signature: signature, // string(signature[:])
	}

	http.PostForm("http://test.com/data", data)

}
