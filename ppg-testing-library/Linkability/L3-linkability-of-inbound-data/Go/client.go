package main

import (
	"log"
	"net/http"
	"net/url"
)

func main() {
	var err error

	// @PseudoIdentifier
	data := url.Values{
		"name": {"firstname lastname"},
	}

	resp, err := http.PostForm("http://server.aks.clouditor.io/data", data)
	if err != nil {
		log.Fatal(err)
	}
}
