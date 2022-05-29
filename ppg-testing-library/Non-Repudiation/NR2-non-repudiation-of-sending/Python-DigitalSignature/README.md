# Test Case Description: NR2 Non-Repudiation of Sending -- Python Digital Signature
- Threat description: A signed, i.e. non-reputable, message including personal data is sent from client to server.
- Expected test outcome:
  1. The taint is detected
  2. The usage of the cryptography library's signature method on the tainted datum is detected
  3. The data flow of the tainted datum together with the signature to the server is detected
  