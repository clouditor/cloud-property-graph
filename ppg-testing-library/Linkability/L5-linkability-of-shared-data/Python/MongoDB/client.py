#!/usr/bin/env python3

import json
import requests

def transfer_data():
    url = 'http://test.com/data'
    # @Pseudoidentifier
    user_data = {'user_email': 'firstname.lastname@test.com', 'request_geo_location': 'munich'}
    response = requests.post(url, data = user_data)
    if response.status_code == 200:
        print("Data transfer successful!")
    else:
        print("Data transfer failed!")

if __name__ == '__main__':
    transfer_data()