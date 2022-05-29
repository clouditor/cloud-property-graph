# Test Case Description: L5 Linkability of Shared Data -- Go
- Threat description: A pseudo-identifier is sent to a server where it can be linked to other pseudo-identifiable data (which may be submitted via the same request). The server furthermore shares the data with a third party (where it also may be linked to other pseudo-identifiable data).
- Expected test outcome:
  1. The taint is detected
  2. The flow of the tainted datum to the HTTP endpoint of the server is detected
  3. The flow of the tainted datum from the server to the third party is detected