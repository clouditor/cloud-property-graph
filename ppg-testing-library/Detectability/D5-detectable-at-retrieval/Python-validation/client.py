#!/usr/bin/env python3

import requests

def query():
    url = 'test.com/getdata'
    #@PseudoIdentifier
    personal_data = {'name': 'firstnamelastname'}
    response = requests.post(url, data = personal_data)
    #requests.get("http://test.com/getdata?name=firstnamelastname")

if __name__ == '__main__':
    query()
