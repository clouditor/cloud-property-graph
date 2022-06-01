package main

import (
	"net/http"
)

func main() {
    http.Get("http://other-domain.com/data?Name=firstnamelastname")
}
