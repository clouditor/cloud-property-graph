#!/usr/bin/env python3

import requests

def query():
    url = 'test.com/data'

    personal_data = {'name': 'firstname lastname'}
    response = requests.post(url, data = personal_data)

if __name__ == '__main__':
    query()
