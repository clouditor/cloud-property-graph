package main

import (
	"log"
	"net/http"
	"net/url"
)

func main() {
	var err error

    //@Identifier
	identifier := url.Values{
		"name": {"firstname lastname"},
	}
	// the identifier is not sent anywhere
}
