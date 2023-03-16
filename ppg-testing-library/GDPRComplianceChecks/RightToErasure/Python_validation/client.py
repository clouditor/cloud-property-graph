#!/usr/bin/env python3

import requests

def delete_own_data(personal_data):
    url = 'test-online-notepad.com/data'
    requests.delete(url, json = personal_data)

def send_data_to_external_advertising_server(personal_data):
    url = 'test-online-notepad.com/data'
    requests.post(url, json = personal_data)


if __name__ == '__main__':
    #@PseudoIdentifier
    personal_data = {
        "username": "testuser",
        "notes": ["note1", "note2", "note3"]
    }
    send_data_to_external_advertising_server(personal_data)
    delete_own_data(personal_data)

