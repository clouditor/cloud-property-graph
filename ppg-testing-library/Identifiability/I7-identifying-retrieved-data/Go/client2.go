package main

import (
	"net/http"
)

func main() {
    http.Get("http://test.com/data?Name=firstnamelastname")
}
