#!/usr/bin/env python3

import requests

def query():
    url = 'test.com/data'

    # TODO label doesn't work: @PseudoIdentifier
    personal_data = {'name': 'firstname lastname'}
    response = requests.post(url, data = personal_data)

if __name__ == '__main__':
    query()
