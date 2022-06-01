#!/usr/bin/env python3

import requests

def post_query():
    url = 'test.com/data'
    #@PseudoIdentifier
    personal_data = {'name': 'firstname lastname'}
    requests.post(url, json = personal_data)

def get_query():
    url = 'test.com/data'
    #@PseudoIdentifier
    personal_data = {'name': 'firstname lastname'}
    requests.get(url, json = personal_data)

def put_query():
    url = 'test.com/data'
    #@PseudoIdentifier
    personal_data = {'name': 'firstname lastname'}
    requests.put(url, json = personal_data)


if __name__ == '__main__':
    post_query()
    get_query()
    put_query()
