# Test Case Description: D5 Detectable at Retrieval -- Python
- Threat description: The server offers an API to a database which leaks information about personal data it holds: When the client tries to access data, the server may respond by a _not found_ message.
- Expected test outcome:
  1. The taint is detected
  2. The flow of the taint from the client to the server's database is detected
  3. A client request targeted at the same storage that the taint was stored in is detected
  4. A server response is detected that indicates that a datum was not found
  