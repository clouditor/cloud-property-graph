#!/usr/bin/env python3

from flask import Flask, request

app = Flask(__name__)


@app.route("/data", methods=['POST'])
def collect_data():
    # Threat results from personal data being collected, but not processed
    content = request.json
    return "OK", 200


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True, threaded=True)
