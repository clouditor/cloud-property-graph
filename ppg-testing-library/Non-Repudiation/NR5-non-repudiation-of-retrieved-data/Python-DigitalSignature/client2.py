#!/usr/bin/env python3

import requests

def query():
    url = 'test.com/data'
    requests.get(url, params={"data": "data"})

if __name__ == '__main__':
    query()

