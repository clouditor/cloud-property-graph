#!/usr/bin/env python3

import json
import requests

def login():
    url = 'http://test.com/login'
    # @Identifier
    credentials_form = {'email': 'firstname.lastname@test.com', 'password': '123456', 'first_name': 'firstname', 'last_name': 'lastname'}
    response = requests.post(url, data = credentials_form)
    if response.status_code == 200:
        print("Login successful!")
    else:
        print ("Login failed!")

if __name__ == '__main__':
    login()