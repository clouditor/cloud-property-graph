# Test Case Description: NC1 Disproportionate Storage -- Python
- Threat description: A (pseudo-)identifier is sent from client to server where it is stored and retrieved, but not processed afterwards.
- Expected test outcome:
  1. The taint is detected
  2. The data flow of the tainted datum to the server's database is detected
  3. The retrieval is detected from the respective database storage
  4. No processing step is detected after the retrieval