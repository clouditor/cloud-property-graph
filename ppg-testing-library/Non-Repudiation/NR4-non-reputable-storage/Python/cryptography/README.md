# Test Case Description: NR4 Non-Reputable Storage -- Python
- Threat description: A signed, i.e. non-reputable, message including personal data is sent from client to server.
- Expected test outcome:
    1. The tainted datum in client (TODO) is detected
    2. The usage of the cryptography library's signature method on the tainted datum (TODO) is detected
    3. The data flow of the tainted datum together with the signature to the server (TODO) is detected
    4. The storage (TODO) of the tainted datum together with the signature is detected

TODO: write validation test where tainted datum is stored without signature 
  