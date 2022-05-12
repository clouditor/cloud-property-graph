# Test Case Description: NC1 Disproportionate Collection -- Python
- Threat description: A tainted datum is transferred from client to server. At the server, however, it is not processed, indicating a disproportionate collection.
- Expected test outcome: 
  1. The tainted datum in client.py (TODO) is detected
  2. The data flow of the tainted datum to the server (TODO) is detected 
  3. No processing is detected at server-side after the tainted datum is received
  