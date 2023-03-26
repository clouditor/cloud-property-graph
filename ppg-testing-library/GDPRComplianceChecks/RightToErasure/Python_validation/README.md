# Test Case: Article 17 Validation - Right to Erasure
- Test case description: The client sends personal data to the server. The server processes the data, saves it in a Mongo database and communicates it to third parties. The client offers a function for the deletion of his personal data. The server does not delete the personal data and does not inform other data recipients about the deletion request.
- Expected outcome:
    - The server does not delete the personal data and does not inform other data recipients about the deletion request is detected.