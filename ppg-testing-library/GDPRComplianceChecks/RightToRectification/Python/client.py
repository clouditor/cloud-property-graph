#!/usr/bin/env python3

import requests

def rectify(personal_data):
    url = 'test-online-notepad.com/data'

    personal_data["name"] = "new name"
    requests.put(url, json = personal_data)

def store_personal_data_on_server(personal_data):
    url = 'test-online-notepad.com/store_data'
    requests.put(url, json = personal_data)

if __name__ == '__main__':
    #@PseudoIdentifier
    personal_data = {
        "username": "testuser",
        "name": "firstname lastname",
        "notes": ["note1", "note2", "note3"]
    }
    store_personal_data_on_server(personal_data)
    rectify(personal_data)
