# Test Case Description: U3 No Access Or Portability -- Go
- Threat description: A (pseudo-)identifier is sent from client to server where it is stored in a database and cannot be accessed by the client again.
- Expected test outcome:
  1. The taint is detected
  2. The flow of the tainted datum from the client to the server's database is detected
  3. No GET access by the client is detected