# Test Case Description: ID6 Identifying Stored Data -- Python
- Threat description: Personal data is stored which can be used to identify a person.
- Expected test outcome:
  1. The data flow from the client to the server is detected (l.10 at the client)
  2. The saving of the identifieable data (identifiers or quasi-identifiers) to a database is detected (l.19 at the server)