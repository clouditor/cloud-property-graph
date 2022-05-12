#!/usr/bin/env python3

import requests

def query():
    url = 'test.com/account'
    # @Identifier
    personal_data = {'name': 'name', 'password': 'password'}
    response = requests.post(url, data = personal_data)

if __name__ == '__main__':
    query()
