#!/usr/bin/env python3

import requests

def query():
    url = 'test.com/data'
    #@Pseudoidentifier
    personal_data = {'name': 'firstname lastname'}
    requests.post(url, json = personal_data)

def query2():
    url = 'test.com/data2'
    #@Identifier
    personal_data1 = 'firstname lastname'
    non_personal_data1 = "My grandpa always used to say 'as one door closes, another one opens.' A lovely man. A terrible cabinet maker."
    requests.post(url, json = {'name': personal_data1, 'joke': non_personal_data1})

if __name__ == '__main__':
    query()
    query2()
