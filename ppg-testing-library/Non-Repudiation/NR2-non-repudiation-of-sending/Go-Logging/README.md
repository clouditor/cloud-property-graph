# Test Case Description: NR2 Non-Repudiation of Sending -- Go Logging
- Threat description: A message including personal data is sent from client to server where it is logged.
- Expected test outcome:
  1. The taint is detected
  2. The data flow of the tainted datum to the server is detected
  3. The server's log operation of the tainted datum is detected 