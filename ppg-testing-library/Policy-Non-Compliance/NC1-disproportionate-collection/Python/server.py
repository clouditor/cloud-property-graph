#!/usr/bin/env python3

from flask import Flask, request
from pymongo import MongoClient, database

mongo_host = "mongo"
phr_db_client = MongoClient("mongodb://mongo:27017/")
phr_db = phr_db_client.phr
phr_db_collection = phr_db.records

app = Flask(__name__)

@app.route("/data", methods=['POST'])
def collect_data():
    # Threat results from personal data being collected, but not processed
    # TODO: which other cases present a NON-processing?
    # TODO e.g. a simple assignment
    # content = request.json
    # phr = content
    # TODO Konrad?
    return "OK", 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True, threaded=True)