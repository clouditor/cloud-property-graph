#!/usr/bin/env python3

import json
import requests
from cryptography.exceptions import InvalidSignature
from cryptography.exceptions import UnsupportedAlgorithm
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.hazmat.primitives.asymmetric import rsa

# Non-Repudiation threat results from signing a personal datum with the private key, and sending it to the server
def query():
    plain_text = 'non-repudiation of sending threat'
    url = 'http://test.com/data'

    private_key = rsa.generate_private_key(
        public_exponent=65537,
        key_size=4096,
        backend=default_backend()
    )
    public_key = private_key.public_key()
    signature = private_key.sign(
        data=plain_text.encode('utf-8'),
        padding=padding.PSS(
            mgf=padding.MGF1(hashes.SHA256()),
            salt_length=padding.PSS.MAX_LENGTH
        ),
        algorithm=hashes.SHA256()

    )
    # @Identifier
    personal_data = {'name': 'firstname lastname', 'signature': signature}
    response = requests.post(url, data = personal_data)    

if __name__ == '__main__':
    query()
