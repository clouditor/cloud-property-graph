# Test Case Description: D2 Detectable Communication -- Go
- Threat description: An identifier is sent from client to server. This can be observed by other network participants, which then know that the person is using the service provided by the server.
- Expected test outcome:
  1. The taint is detected
  2. The flow of the tainted datum to the HTTP endpoint of the server is detected