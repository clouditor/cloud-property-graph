#!/usr/bin/env python3

import json
import requests

def query():
    url = 'test.com/data'
    # @Pseudoidentifier
    personal_data = {'name': 'firstname lastname'}
    response = requests.post(url, data = personal_data)

if __name__ == '__main__':
    query()
