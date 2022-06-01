# Test Case Description: Disclosure -- Unencrypted Transmission -- Go
- Threat description: A POST request with personal data is sent from client to server without transport encryption.
- Expected test outcome:
    1. The taint is detected 
    2. The flow of the tainted datum to the HTTP endpoint of the server is detected
    3. The transport protocol is recognized as unencrypted