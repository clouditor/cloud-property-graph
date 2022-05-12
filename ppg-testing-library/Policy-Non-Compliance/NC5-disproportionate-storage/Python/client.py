#!/usr/bin/env python3

import requests

def query():
    url = 'test.com/data'
    #@PseudoIdentifier
    personal_data = {
        "name": "John",
    }
    requests.post(url, json = personal_data)

def query2():
    url = 'test.com/data2'
    #@Identifier
    personal_data = 'firstname lastname'
    non_personal_data = 'My grandpa always used to say “as one door closes, another one opens." A lovely man. A terrible cabinet maker.'
    requests.post(url, json = {'name': personal_data, 'joke': non_personal_data})

if __name__ == '__main__':
    query()

