# Test Case Description: ID3 Identifying Inbound Data -- Go
- Threat description: An identifier is sent from client to server.
- Expected test outcome:
  1. The tainted datum in client.py (l.17) is detected
  2. The taint's flow to the server's API in server.py (l.21) is detected