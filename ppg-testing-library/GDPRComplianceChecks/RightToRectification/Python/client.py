#!/usr/bin/env python3

import requests

def rectify(changed_name):
    url = 'test-online-notepad.com/data'
    #@PseudoIdentifier
    personal_data = {
        "username": "testuser",
        "name": "firstname lastname",
        "notes": ["note1", "note2", "note3"]
    }
    personal_data["name"] = changed_name
    requests.put(url, json = personal_data)

if __name__ == '__main__':
    rectify("New Name")
