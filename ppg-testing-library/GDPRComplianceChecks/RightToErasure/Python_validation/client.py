#!/usr/bin/env python3

import requests

def delete_own_data():
    url = 'test-online-notepad.com/data'
    #@PseudoIdentifier
    personal_data = {
        "username": "testuser",
        "notes": ["note1", "note2", "note3"]
    }
    requests.delete(url, json = personal_data)

if __name__ == '__main__':
    delete_own_data()
