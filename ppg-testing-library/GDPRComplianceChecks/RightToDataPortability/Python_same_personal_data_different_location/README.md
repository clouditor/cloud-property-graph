# Test Case: Article 20 - Right to Data Portability - Same Personal Data, Different Location
- Test case description: A user is registered in the "client_signup" page and his personal data is sent to a server. The server processes the data, saves it in a Mongo database. On another page ("client_edit") the user can request retrieval of his stored personal data (via signup) in a machine-readable format, as well as the transfer of his personal data to another data controller.
- Expected outcome:
    - No data flow is detected which does not fulfill the code properties of GDPR article 20.