# Test Case Description: L6 Linkability of Stored Data -- Go
- Threat description: A client sends a pseudo-identifier to a server where it is stored and can be linked to other pseudo-identifiable data (which may be submitted via the same request).
- Expected test outcome:
  1. The taint is detected
  2. The flow of the tainted datum to the HTTP endpoint of the server's database is detected