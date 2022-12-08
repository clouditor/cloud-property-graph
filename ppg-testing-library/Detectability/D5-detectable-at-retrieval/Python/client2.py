#!/usr/bin/env python3

import requests

def get_query():
    #url = 'http://test.com/getdata'
    #requests.get(url, params={"name": "firstnamelastname"})
    requests.get("http://test.com/getdata?name=firstnamelastname")

if __name__ == '__main__':
    get_query()
