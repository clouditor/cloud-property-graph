# Test Case Description: NC1 Disproportionate Collection -- Python
- Threat description: A (pseudo-)identifier is sent from client to server where it is not further processed in a meaningful way.
- Expected test outcome:
  1. The taint is detected
  2. The data flow of the tainted datum to the server is detected
  3. No processing step of the tainted datum is detected at the server 