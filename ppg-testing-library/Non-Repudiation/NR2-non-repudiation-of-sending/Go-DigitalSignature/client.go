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
	personal_datum := []byte("firstname lastname") // TODO is there a cast expression? Then there should be a DFG

	// generate signature
	_, priv, err := ed25519.GenerateKey(rand.Reader)
	if err != nil {
		log.Fatal(err)
	}
	signature := ed25519.Sign(priv, personal_datum)
	data := &SignedMessage{
		Personal_datum:      personal_datum,
		Signature: signature, // string(signature[:])
	}

	reqBody, _ := json.Marshal(data)
	reqBodyBytes := bytes.NewBuffer(reqBody)
	req, _ := http.NewRequest("POST", "http://test.com/data", reqBodyBytes)
	client := new(http.Client)
	client.Do(req)
}
