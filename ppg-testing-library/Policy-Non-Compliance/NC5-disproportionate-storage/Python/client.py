#!/usr/bin/env python3

import requests

@PseudoIdentifier
def query():
    url = 'test.com/data'
    # doesnt work: @PseudoIdentifier
    personal_data = {
        "name": "John",
    }
    requests.post(url, data = personal_data)

if __name__ == '__main__':
    query()

