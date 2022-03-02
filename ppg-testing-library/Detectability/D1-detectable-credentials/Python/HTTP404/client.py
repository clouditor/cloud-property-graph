#!/usr/bin/env python3

import json
import requests

def query():
    url = 'test.com/login'
    # @Identifier
    personal_data = {'name': 'firstname lastname', 'password': 'password'}
    response = requests.post(url, data = personal_data)    

if __name__ == '__main__':
    query()
