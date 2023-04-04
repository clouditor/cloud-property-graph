#!/usr/bin/env python3

import requests

def delete_data(personal_data):
    url = 'test-online-notepad.com/data'
    requests.delete(url, json = personal_data)

def rectify_data(personal_data):
    url = 'test-online-notepad.com/data'
    requests.put(url, json = personal_data)

def get_information_about_data_recipients(personal_data):
    data_recipients_information = "receiver of your personal data: ext-ad-server.com/data (external advertising server)\nIt is used for the following purposes: advertising"
    # create file containing the information
    data_recipients_server = requests.get("test-online-notepad.com/data_recipients", params = {"auth_token": personal_data["auth_token"]})
    f = open("data_recipients_information.txt", "w")
    f.write(data_recipients_server)
    f.close()

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
    rectify_data(personal_data)
    get_information_about_data_recipients(personal_data)
    delete_data(personal_data)
