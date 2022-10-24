# Test Case Description: I6 Identifying Stored Data -- Go
- Threat description: A POST request with personal data is sent from client to server which stored it in a database.
- Expected test outcome:
    1. The taint is detected
    2. The flow of the tainted datum to the server's database is detected
