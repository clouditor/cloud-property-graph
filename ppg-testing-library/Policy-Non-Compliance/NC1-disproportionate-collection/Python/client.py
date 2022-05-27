#!/usr/bin/env python3

import requests

def query():
    url = 'test.com/data'
    #@PseudoIdentifier
    name = 'firstname lastname'
    data = 'helloworld'
    message = {
        'name': name,
        'data': data
    }
    requests.post(url, json = message)

if __name__ == '__main__':
    query()
