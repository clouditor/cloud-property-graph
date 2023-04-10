#!/usr/bin/env python3

import requests
import os

def store_personal_data_on_server(personal_data):
    url = 'test-online-notepad.com/store_data'
    requests.post(url, json = personal_data)

if __name__ == '__main__':
    #@PseudoIdentifier
    personal_data_of_client = {
        "username": "testuser",
        "name": "",
        "notes": ""
    }
    store_personal_data_on_server(personal_data_of_client)
