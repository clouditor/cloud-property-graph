# Test Case Description: D1 Detectable Credentials -- Python -- HTTP 404
- Threat description: A login request is sent from client to server (which is detected by the function name "login"), which may respond with a HTTP 404 Not Found message. 
- Expected test outcome: 
  1. The tainted datum in client.py (TODO) is detected
  2. The data flow of the tainted datum to the login function is detected
  3. The HTTP Not Found message is detected in the login function as a possible response
  