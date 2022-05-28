#!/usr/bin/env python3

import requests

def transfer_data():
    url = 'http://test.com/data'
    #@PseudoIdentifier
    user_data = {'email': 'firstname.lastname@test.com', 'password': '123456', 'first_name': 'firstname', 'last_name': 'lastname'}
    requests.post(url, json = user_data)

if __name__ == '__main__':
    transfer_data()