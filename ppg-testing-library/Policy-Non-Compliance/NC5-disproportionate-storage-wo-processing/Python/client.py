#!/usr/bin/env python3

import requests

def query():
    url = 'test.com/data'
    #@PseudoIdentifier
    name = "name"
    personal_data = {
        "Name": name,
        "Message": "hello world"
    }
    requests.post(url, json = personal_data)

if __name__ == '__main__':
    query()

