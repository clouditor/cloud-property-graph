#!/usr/bin/env python3

import requests
import os

def get_personal_data_in_machine_readable_format(personal_data):
    url = 'test-online-notepad.com/data'
    # get the data from the server
    personal_data_received = requests.get(url, json = personal_data)
    f = open("personal_data.json", "w")
    f.write(personal_data_received)
    f.close()

def transfer_personal_data_to_another_service(personal_data):
    url = 'test-online-notepad.com/transfer'
    data = {
        "receiver_url": "other-test-online-notepad.com/data",
        "personal_data": personal_data
    }
    requests.get(url, json = data)

def store_personal_data_on_server(personal_data):
    url = 'test-online-notepad.com/store_data'
    requests.put(url, json = personal_data)

if __name__ == '__main__':
    #@PseudoIdentifier
    personal_data_of_client = {
        "username": "testuser",
        "name": "",
        "notes": ""
    }
    store_personal_data_on_server(personal_data_of_client)
    get_personal_data_in_machine_readable_format(personal_data_of_client)
    transfer_personal_data_to_another_service(personal_data_of_client)
