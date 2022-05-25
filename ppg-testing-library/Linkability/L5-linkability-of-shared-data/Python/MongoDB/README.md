# Test Case Description: ID5 - Identifying Shared Data
- Threat description: Quasi identifiers and actions are forwarded to other party which can be set together in order to obtain identity of user.
- Expected test outcome:
  1. The data flow of quasi identifiers from the client (l.10) to the server and from the server (l.20) to the external party are detected
  2. The storage of quasi identifiable data at the party's side is detected (l.19)