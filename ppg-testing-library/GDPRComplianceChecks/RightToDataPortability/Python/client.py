#!/usr/bin/env python3

import requests
import os

def get_own_data_in_machine_readable_format():
    url = 'test-online-notepad.com/data'
    #@PseudoIdentifier
    personal_data = {
        "username": "testuser",
        "name": "",
        "notes": ""
    }
    # get the data from the server
    personal_data = requests.get(url, json = personal_data)
    f = open("personal_data.json", "w")
    f.write(personal_data)
    f.close()

if __name__ == '__main__':
    get_own_data_in_machine_readable_format()
