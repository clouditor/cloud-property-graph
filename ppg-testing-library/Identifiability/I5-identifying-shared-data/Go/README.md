# Test Case Description: I5 Identifying Shared Data -- Go
- Threat description: A POST request with personal data is sent from client to server which shares it with a third party.
- Expected test outcome:
  1. The taint is detected
  2. The flow of the tainted datum to the HTTP endpoint of the server is detected
  3. The flow of the tainted datum from the server to the third party is detected