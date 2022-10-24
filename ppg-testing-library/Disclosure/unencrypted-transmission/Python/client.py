#!/usr/bin/env python3

import json
import requests

def query():
    # Disclosure threat results from sending personal data to an http address
    url = 'http://test.com/data'
    # @PseudoIdentifier
    personal_data = {'name': 'firstname lastname'}
    requests.post(url, json = personal_data)

if __name__ == '__main__':
    query()
