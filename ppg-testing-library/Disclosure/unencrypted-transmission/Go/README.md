# Test Case Description: Disclosure -- Go
- Threat description: A POST request with personal data is sent from client to server without encryption its content. Therefore the information being sent can disclosed.
- Expected test outcome:
- 1. The identifieable data which is being sent is detected on the client side
- 2. Non-enryption sending of the data is detected