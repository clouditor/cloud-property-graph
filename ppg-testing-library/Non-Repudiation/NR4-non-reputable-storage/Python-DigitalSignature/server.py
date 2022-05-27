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
    content = request.json
    phr_db_collection.insert_one(content)
    return "OK", 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True, threaded=True)