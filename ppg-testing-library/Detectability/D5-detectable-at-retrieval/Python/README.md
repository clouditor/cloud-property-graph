# Test Case Description: D5 Detectable at Retrieval -- Python -- HTTP 404 - MongoDB
- Threat description: A GET request is sent from client to server where the data are retrieved from a MongoDB. The requested data may be personal data (as detected in the test cases I3/L3). If the data do not exist, a HTTP 404 Not Found message is sent back, leaking information about a (non-)existing personal datum in the database.
- Expected test outcome:
  1. The storage of personal data in the MongoDB is detected (see L3/I3)
  2. The tainted datum in client.py (l.9) is detected
  3. The GET request to the API function is detected
  4. The HTTP Not Found message is detected in the API function as a possible response
  