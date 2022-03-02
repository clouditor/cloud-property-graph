#!/usr/bin/env python3

from flask import Flask, request
import json
import requests
import logging
import os, sys

app = Flask(__name__)

# Detectability threat results from leaking information about existing accounts
@app.route("/account", methods=['POST'])
def collect_data():
    content = request.json
    return "Conflict", 409

if __name__ == '__main__':
    logging.info("start at port 8080")
    app.run(host='0.0.0.0', port=8080, debug=True, threaded=True)