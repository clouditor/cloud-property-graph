# Test Case: Article 16 - Right to Rectification - Same Personal Data, Different Location
- Test case description: A user is registered in the "client_signup" page and his personal data is sent to a server. The server processes the data, saves it in a Mongo database.  On another page ("client_edit") the user can request rectification of his personal data, which was initially stored via signup. The server performs this request and rectifies the personal data and stores the updated data in the database.
- Expected outcome:
    - No data flow is detected which does not fulfill the code properties of GDPR article 16.
