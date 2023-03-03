#!/usr/bin/env python3

import requests
import os

def get_own_data_in_machine_readable_format(transfer = False):
    url = 'test-online-notepad.com/data'
    #@PseudoIdentifier
    personal_data = {
        "username": "testuser",
        "name": "",
        "notes": ""
    }
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
        f.write(personal_data)
        f.close()

#def transfer_personal_data_to_another_service():
#    url = 'test-online-notepad.com/transfer'
#    #@PseudoIdentifier
#    personal_data = {
#        "username": "testuser",
#        "name": "",
#        "notes": ""
#    }
#    data = {
#        "receiver_url": "other-test-online-notepad.com/data",
#        "personal_data": personal_data
#    }
#    # send get request to the server, which will then send the data to the specified url
#    response = requests.get(url, json = data)

if __name__ == '__main__':
    get_own_data_in_machine_readable_format()
