# Test Case Description: NR1 Credential Non-Repudiation -- Python
- Threat description: An identifier (credential) is sent from client to server, where it is used in a logging operation---implying that the identifier is stored in a central logging service.
- Expected test outcome:
  1. The tainted datum in client.go (TODO) is detected
  2. The logging operation in server.go (TODO) is detected
  3. The taint's data flow to the logging operation (TODO) is detected
  