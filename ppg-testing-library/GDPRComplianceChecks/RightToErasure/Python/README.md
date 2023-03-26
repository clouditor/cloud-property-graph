# Test Case: Article 17 - Right to Erasure
- Test case description: The client sends personal data to the server. The server processes the data, saves it in a Mongo database and sends it to third parties. The client offers a function for the deletion of his personal data. The server performs this request, deletes the personal data and informs other data recipients about the deletion request.
- Expected outcome:
    - No data flow is detected which does not fulfill the code properties of GDPR article 17.