#!/usr/bin/env python3

import json
import requests

def register():
    url = 'http://test.com/register'
    # @Identifier
    credentials_form = {'email': 'firstname.lastname@test.com', 'password': '123456', 'first_name': 'firstname', 'last_name': 'lastname'}
    requests.post(url, data = credentials_form)

if __name__ == '__main__':
    register()