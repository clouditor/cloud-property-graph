# Test Case Description: I7 Linkability of Retrieved Data -- Go
- Threat description: A POST request with personal data is sent from client to server which stores it in a database. Another client can access the datum via a GET request.
- Expected test outcome:
  1. The taint is detected
  2. The flow of the tainted datum to the server's database is detected
  3. The second client's GET request to access the database storage which holds the tainted datum is detected
