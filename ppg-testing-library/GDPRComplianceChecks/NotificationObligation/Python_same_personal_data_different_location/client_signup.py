#!/usr/bin/env python3

import requests

def send_data_to_server(personal_data):
    url = 'test-online-notepad.com/data'
    requests.post(url, json = personal_data)

if __name__ == '__main__':
    #@PseudoIdentifier
    personal_data = {
        "username": "testuser",
        "notes": ["note1", "note2", "note3"],
        "auth_token": "1234567890"
    }
    send_data_to_server(personal_data)
