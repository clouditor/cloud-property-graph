package main

import (
	"net/http"
	"os"
	"time"

	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"
)

func main() {
	var err error

	zerolog.TimeFieldFormat = zerolog.TimeFormatUnix

	log.Logger = log.Output(
		zerolog.ConsoleWriter{
			TimeFormat: time.RFC822,
			Out:        os.Stderr,
			NoColor:    false,
		},
	)

	mux := http.NewServeMux()
	mux.HandleFunc("/data", parse_data)

	err := http.ListenAndServe(":8080", mux)
	log.Fatal(err)
}

// TODO: CloudPG extension for mapping r to the http request body
func parse_data(w http.ResponseWriter, r *http.Request) {
	// non-repudiation threat results from logging the tainted personal data
	log.Info().Msg(r.Body)
}
