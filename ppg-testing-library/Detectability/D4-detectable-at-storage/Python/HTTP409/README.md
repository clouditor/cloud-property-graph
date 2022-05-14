# Test Case Description: D4 Detectable at Storage -- Python -- HTTP 409 - MongoDB
- Threat description: A POST request with personal data is sent from client to server where the data are stored in a MongoDB. If the data already exist, a HTTP 409 Conflict message is sent back, leaking information about a existing personal datum in the database.  
- Expected test outcome:
  2. The tainted datum in client.py (TODO) is detected
  3. The POST request to the API function is detected
  4. The HTTP Conflict message is detected in the API function as a possible response
  