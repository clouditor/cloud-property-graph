#!/usr/bin/env python3

import requests

def query():
    url = 'test.com/account'
    #@PseudoIdentifier
    name = "name"
    personal_data = {
        "name": name,
        "password": "password"
    }
    requests.post(url, json = personal_data)

if __name__ == '__main__':
    query()
