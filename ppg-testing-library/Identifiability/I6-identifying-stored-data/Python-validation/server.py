#!/usr/bin/env python3

from flask import Flask, request
import json
import requests
from pymongo import MongoClient, database 

mongo_host = "mongo"
user_db_client = MongoClient("mongodb://mongo:27017/")
user_db = user_db_client.userdata
user_db_collection = user_db.records

app = Flask(__name__)

@app.route("/data", methods=['POST'])
def parse_data():
    req = request.json
    data = {
        "Name": req['name'],
        "Message": req['message']
    }
    return "OK", 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True, threaded=True)