#!/usr/bin/env python3

import requests
import os

def get_own_data_in_machine_readable_format(transfer = False, personal_data=None):
    url = 'test-online-notepad.com/data'
    if transfer:
        url = 'test-online-notepad.com/transfer'
        data = {
            "receiver_url": "other-test-online-notepad.com/data",
            "personal_data": personal_data
        }
        requests.get(url, json = data)
    else:
        # get the data from the server
        personal_data = requests.get(url, json = personal_data)
        f = open("personal_data.json", "w")
        # VALIDATION: Personal data is not stored on the client (write call is missing)
        f.close()

def store_personal_data_on_server(personal_data):
    url = 'test-online-notepad.com/data'
    requests.put(url, json = personal_data)

if __name__ == '__main__':
    #@PseudoIdentifier
    personal_data = {
        "username": "testuser",
        "name": "",
        "notes": ""
    }
    store_personal_data_on_server(personal_data)
    get_own_data_in_machine_readable_format(personal_data=personal_data)
