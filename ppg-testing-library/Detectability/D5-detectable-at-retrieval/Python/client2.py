#!/usr/bin/env python3

import requests

def get_query():
    requests.get("http://test.com/getdata?name=firstnamelastname")

if __name__ == '__main__':
    get_query()
