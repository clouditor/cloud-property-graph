# Test Case: Article 16 Validation - Right to Rectification
- Test case description: The client sends personal data to the server. The server processes the data, saves it in a Mongo database. The client offers a function for the rectification of his personal data. The server does not perform rectification of the personal data and therefore does not update the personal data record in the database.
- Expected outcome:
    - The non-rectification of the personal data (the update call to the database of the personal data) is detected.
