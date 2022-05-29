# Test Case Description: D4 Detectable at Storage -- Python
- Threat description: The server offers an API to a database which leaks information about existing data.
- Expected test outcome:
  1. The taint is detected
  2. The flow of the taint from the client to the server's database is detected
  3. A server response is detected that indicates a conflict because the datum already exists in the database