package main

import (
	"os/exec"
	"testing"

	"github.com/neo4j/neo4j-go-driver/neo4j"
)

// 1) yml is consumed by testing pass
func TestExecute(t *testing.T) {
	// 2) Execute PPG with correct directory
	// JAVA_HOME=/opt/homebrew/Cellar/openjdk@11/11.0.12/libexec/openjdk.jdk/Contents/Home /bin/bash /Users/kunz/cloud-property-graph/cloudpg/build/install/cloudpg/bin/cloudpg --enable-labels --local-mode --root=/Users/kunz/cloud-property-graph/ ppg-testing-library/
	// cmd := exec.Command("/bin/sh", "/Users/kunz/")
	cmd := &exec.Cmd{
		Path: "/Users/kunz/cloud-property-graph/cloudpg/build/install/cloudpg/bin/cloudpg",
		Args: []string{"--enable-labels", "--local-mode", "--root=/Users/kunz/cloud-property-graph/ ppg-testing-library/"},
	}
	cmd.Run()

	// 3) Query the graph
	// 4) Assert expected outcome
	// t.Log("error should be nil", err)
	// t.Fail()
}

func helloWorld(uri, username, password string, encrypted bool) (string, error) {
	driver, err := neo4j.NewDriver(uri, neo4j.BasicAuth(username, password, ""), func(c *neo4j.Config) {
		c.Encrypted = encrypted
	})
	if err != nil {
		return "", err
	}
	defer driver.Close()

	session, err := driver.Session(neo4j.AccessModeWrite)
	if err != nil {
		return "", err
	}
	defer session.Close()

	greeting, err := session.WriteTransaction(func(transaction neo4j.Transaction) (interface{}, error) {
		result, err := transaction.Run(
			"CREATE (a:Greeting) SET a.message = $message RETURN a.message + ', from node ' + id(a)",
			map[string]interface{}{"message": "hello, world"})
		if err != nil {
			return nil, err
		}

		if result.Next() {
			return result.Record().GetByIndex(0), nil
		}

		return nil, result.Err()
	})
	if err != nil {
		return "", err
	}

	return greeting.(string), nil
}
