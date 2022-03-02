#!/usr/bin/env python3

from flask import Flask, request
import json
import requests
import logging
import os, sys

@app.route("/data", methods=['POST'])
def collect_data():
    # content has tainted data
    content = request.json
    # the logging library represents a non-repudiation threat, since the sending action and the personal datum is persisted
    logging.info("Received datum %s" % content)
    return "OK", 200

if __name__ == '__main__':
    logging.info("start at port 8080")
    app.run(host='0.0.0.0', port=8080, debug=True, threaded=True)