# Test Case Description: NC1 Disproportionate Storage -- Python (field-sensitive)
- Threat description: A (pseudo-)identifier is sent from client to server where it is stored but not retrieved afterwards.
- Expected test outcome:
  1. The taint is detected
  2. The data flow of the tainted datum to the server's database is detected
  3. No retrieval is detected from the respective database storage  