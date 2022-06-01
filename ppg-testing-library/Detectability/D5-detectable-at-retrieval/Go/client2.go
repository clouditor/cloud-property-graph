package main

import (
	"net/http"
)

func main() {
	http.Get("http://test.com/getdata?name=client2name")
}
