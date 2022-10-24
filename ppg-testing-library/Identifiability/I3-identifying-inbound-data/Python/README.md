# Test Case Description: I3 Identifying Inbound Data -- Python
- Threat description: A POST request with personal data is sent from client to server.
- Expected test outcome:
  1. The taint is detected
  2. The flow of the tainted datum to the HTTP endpoint of the server is detected