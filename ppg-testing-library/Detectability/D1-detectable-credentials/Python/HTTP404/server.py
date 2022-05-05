#!/usr/bin/env python3

from flask import Flask, request

app = Flask(__name__)

# TODO validation query with multiple different HTTP responses
# TODO the login is recognized only by name in the query
# Detectability threat results from leaking information about (non-)existing accounts
@app.route("/login", methods=['POST'])
def login():
    content = request.json
    # TODO add database request?
    return "Not Found", 404

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True, threaded=True)