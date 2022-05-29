# Test Case Description: NR4 Non-Reputable Storage -- Go Digital Signature
- Threat description: A signed, i.e. non-reputable, message including personal data is sent from client to server where it is stored in a database.
- Expected test outcome:
  1. The taint is detected
  2. The usage of the cryptography library's signature method on the tainted datum is detected
  3. The data flow of the tainted datum together with the signature to the server's database is detected
  