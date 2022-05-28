#!/usr/bin/env python3

from flask import Flask, request
from pymongo import MongoClient, database

mongo_host = "mongo"
user_db_client = MongoClient("mongodb://mongo:27017/")
user_db = user_db_client.data
user_db_collection = user_db.records

app = Flask(__name__)

# Personal data can be submitted (POST) and read (GET), but it cannot be updated (PUT)
@app.route("/data", methods=['POST'])
def collect_data():
    content = request.json
    user_db_collection.insert_one(content)
    return "OK", 200

@app.route("/data", methods=['GET'])
def collect_data():
    content = request.json
    user_db_collection.find_one(content["name"])
    return "OK", 200

@app.route("/data", methods=['DELETE'])
def collect_data():
    content = request.json
    user_db_collection.delete_one(content["user_id"])
    return "OK", 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True, threaded=True)