#!/usr/bin/env python3

import requests

def register():
    url = 'http://test.com/user-profile'
    response = requests.get(url)

if __name__ == '__main__':
    register()