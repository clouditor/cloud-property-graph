package main

import (
	"bytes"
	"fmt"
	"io"
	"os"
	"os/exec"

	"github.com/neo4j/neo4j-go-driver/v4/neo4j"
)

// 1) yml is consumed by testing pass automatically

func main() {
	// Neo4j 4.0, defaults to no TLS therefore use bolt:// or neo4j://
	// Neo4j 3.5, defaults to self-signed certificates, TLS on, therefore use bolt+ssc:// or neo4j+ssc://
	dbUri := "neo4j://localhost:7687"
	driver, err := neo4j.NewDriver(dbUri, neo4j.BasicAuth("neo4j", "password", ""))
	if err != nil {
		panic(err)
	}
	// Handle driver lifetime based on your application lifetime requirements  driver's lifetime is usually
	// bound by the application lifetime, which usually implies one driver instance per application
	defer driver.Close()

	// 2) Execute PPG
	// PPGExecute()

	// 3) Query the graph
	res, err := queryGraph(driver)
	if err != nil {
		panic(err)
	}
	fmt.Println("Result", res)

	// 4) Assert expected outcome
	// t.Log("error should be nil", err)
	// t.Fail()
}

func PPGExecute() {
	cmd := &exec.Cmd{
		// requires Java 11, e.g. with JAVA_HOME=/opt/homebrew/Cellar/openjdk@11/11.0.12/libexec/openjdk.jdk/Contents/Home
		Path: "/Users/kunz/cloud-property-graph/cloudpg/build/install/cloudpg/bin/cloudpg",
		Args: []string{"--enable-labels", "--local-mode", "--root=/Users/kunz/cloud-property-graph/", "ppg-testing-library/Linkability/L3-linkability-of-inbound-data/Go"},
	}
	fmt.Println(cmd)

	var stdBuffer bytes.Buffer
	mw := io.MultiWriter(os.Stdout, &stdBuffer)

	cmd.Stdout = mw
	cmd.Stderr = mw

	if err := cmd.Run(); err != nil {
		fmt.Println("Error", err)
	}

	fmt.Println(stdBuffer.String())
}

func queryGraph(driver neo4j.Driver) (*Item, error) {
	session := driver.NewSession(neo4j.SessionConfig{})
	defer session.Close()

	_, err := session.WriteTransaction(func(transaction neo4j.Transaction) (interface{}, error) {
		result, err := transaction.Run(
			// "MATCH (l:LogOutput) return l",
			"MATCH (n:HttpEndpoint) RETURN n",
			map[string]interface{}{})
		if err != nil {
			return nil, err
		}

		if result.Next() {
			fmt.Println("Results:", result.Record())
			fmt.Println("Keys:", result.Record().Keys)
			fmt.Println("Values:", result.Record().Values)
			val, found := result.Record().Get("n")
			result.Record()
			if found {
				fmt.Println("Value:", val)
				fmt.Println("Value:", val.identity)
			} else {
				fmt.Println("Not Found")
			}

			return result.Record().Values[0], nil
		}

		return nil, result.Err()
	})
	if err != nil {
		return nil, err
	}

	return nil, nil
}

func insertItem(driver neo4j.Driver) (*Item, error) {
	// Sessions are short-lived, cheap to create and NOT thread safe. Typically create one or more sessions
	// per request in your web application. Make sure to call Close on the session when done.
	// For multi-database support, set sessionConfig.DatabaseName to requested database
	// Session config will default to write mode, if only reads are to be used configure session for
	// read mode.
	session := driver.NewSession(neo4j.SessionConfig{})
	defer session.Close()
	result, err := session.WriteTransaction(createItemFn)
	if err != nil {
		return nil, err
	}
	return result.(*Item), nil
}

func createItemFn(tx neo4j.Transaction) (interface{}, error) {
	records, err := tx.Run("CREATE (n:Item { id: $id, name: $name }) RETURN n.id, n.name", map[string]interface{}{
		"id":   1,
		"name": "Item 1",
	})
	// In face of driver native errors, make sure to return them directly.
	// Depending on the error, the driver may try to execute the function again.
	if err != nil {
		return nil, err
	}
	record, err := records.Single()
	if err != nil {
		return nil, err
	}
	// You can also retrieve values by name, with e.g. `id, found := record.Get("n.id")`
	return &Item{
		Id:   record.Values[0].(int64),
		Name: record.Values[1].(string),
	}, nil
}

type Item struct {
	Id   int64
	Name string
}
