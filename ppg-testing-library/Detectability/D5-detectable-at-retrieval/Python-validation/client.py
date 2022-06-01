#!/usr/bin/env python3

import requests

def query():
    requests.get("http://test.com/getdata?name=firstnamelastname")

if __name__ == '__main__':
    query()
