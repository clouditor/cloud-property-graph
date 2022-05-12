# Test Case Description: NC5 Disproportionate Storage -- Python
- Threat description: A tainted datum is transferred from client to server where it is stored in a database. There is, however, no read access foreseen to that datum, indicating a disproportionate storage.
- Expected test outcome:
    1. The tainted datum in client.py (TODO) is detected
    2. The data flow of the tainted datum to the server (TODO) is detected
    3. The storage of the tainted datum is detected
    4. No retrieval query is detected at server-side