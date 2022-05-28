# Test Case Description: D1 Detectable Credentials -- Go -- HTTP 409
- Threat description: A registration request is sent from client to server (which is detected by the function name "login"), which may respond with a HTTP 409 Conflict message.
- Expected test outcome:
  1. The tainted datum in client.go (l.13) is detected
  2. The data flow of the tainted datum to the registration function is detected
  3. The HTTP Conflict message is detected in the registration function as a possible response
  