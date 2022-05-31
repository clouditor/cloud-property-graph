# Test Case Description: NR5 Non-Repudiation of Retrieved Data -- Go
- Threat description: A cryptographically signed, i.e. non-reputable, message including personal data is sent from client to server where it is stored in a database and is retrieved by another client.
- Expected test outcome:
  1. The taint is detected
  2. The usage of the cryptography library's signature method on the tainted datum is detected
  3. The data flow of the tainted datum together with the signature to the server's database is detected
  4. The second client's GET request to access the database storage which holds the tainted datum is detected
  