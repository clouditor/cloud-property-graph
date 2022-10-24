#!/usr/bin/env python3

from flask import Flask, request
import logging

app = Flask(__name__)

@app.route("/data", methods=['POST'])
def collect_data():
    # Threat results from personal data being collected, but not processed
    content = request.json
    process(content['name'])
    return "OK", 200

def process(name):
    print(name)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True, threaded=True)