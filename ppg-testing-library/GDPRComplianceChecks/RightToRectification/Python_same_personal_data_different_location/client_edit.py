#!/usr/bin/env python3

import requests

def rectify(personal_data):
    url = 'test-online-notepad.com/data'
    personal_data["name"] = "new name"
    requests.put(url, json = personal_data)

if __name__ == '__main__':
    #@PseudoIdentifier
    personal_data_1 = {
        "username": "testuser",
        "name": "firstname lastname"
    }
    rectify(personal_data_1)
