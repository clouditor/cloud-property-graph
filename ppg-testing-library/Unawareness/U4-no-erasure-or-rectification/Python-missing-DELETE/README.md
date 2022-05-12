# Test Case Description: U4 No Erasure Or Rectification -- Python
- Threat description: Personal data is transmitted from client to server where it is stored in a database. The user, however, has no way of downloading the data again. 
- Expected test outcome:
  1. The tainted datum in client.py (TODO) is detected
  2. The data flow of the tainted datum to the server (TODO) is detected
  3. The storage of the tainted datum is detected
  4. No PUT method is detected that allows to update the data that was previously stored in the database
  