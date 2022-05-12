#!/usr/bin/env python3

from flask import Flask, request
import logging

@app.route("/data", methods=['POST'])
def collect_data():
    # content has tainted data
    content = request.json
    # the logging library represents a non-repudiation threat, since the sending action and the personal datum is persisted
    logging.info("Received datum %s" % content)
    return "OK", 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True, threaded=True)