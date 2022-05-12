# Test Case Description: ID5 - Identifying Shared Data
- Threat description: Identifier such as firstname, lastname or email are used for authorization. The data is first being sent to a third party which then sends identifiable data to the requested server.
- Expected test outcome:
  1. The auth request with identifiable data from client to third party is detected