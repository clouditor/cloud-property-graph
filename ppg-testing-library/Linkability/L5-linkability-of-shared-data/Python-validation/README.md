# Test Case Description: ID5 - Identifying Shared Data
- Threat description: Identifier such as first name, last name, username are sent to a service which then sends it to another party
- Expected test outcome:
  1. The identifiable data which is sent to the server is detected at the client side in line 10
  2. The data flow from the server to the party in line 20 is detected
  3. The incoming data flow from the server to the party in line 18 is detected