#!/usr/bin/env python3

import requests

def query():
    url = 'http://test.com/data'
    #@Identifier
    personal_data = {'name': 'firstname lastname'}
    requests.post(url, json = personal_data)

if __name__ == '__main__':
    query()
