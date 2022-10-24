# Test Case Description: L3 Linkability Of Inbound Data -- Go
- Threat description: A pseudo-identifier is sent to a server where they can be linked to other pseudo-identifiable data (which may be submitted via the same request).
- Expected test outcome: 
  1. The taint is detected
  2. The flow of the tainted datum from the client to the server is detected