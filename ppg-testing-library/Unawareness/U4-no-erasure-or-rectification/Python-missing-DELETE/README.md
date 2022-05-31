# Test Case Description: U4 No Erasure Or Rectification -- Python -- Missing DELETE
- Threat description: A (pseudo-)identifier is sent from client to server where it is stored in a database and cannot be deleted again by the client.
- Expected test outcome:
  1. The taint is detected
  2. The flow of the tainted datum from the client to the server's database is detected
  3. No DELETE access by the client is detected  