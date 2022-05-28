#!/usr/bin/env python3

import requests

def query():
    url = 'http://test.com/account'
    # @Identifier
    personal_data = {'name': 'name', 'password': 'password'}
    requests.post(url, json = personal_data)

def get_query():
    requests.get("http://test.com/getdata?name=firstnamelastname")

if __name__ == '__main__':
    query()
    get_query()
