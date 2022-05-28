#!/usr/bin/env python3

from flask import Flask, request
import json
import requests
import logging
import os, sys
from pymongo import MongoClient, database 

# phr_db client (MongoDB)
mongo_host = "mongo"
user_db_client = MongoClient("mongodb://mongo:27017/")
user_db = user_db_client.userdata
user_db_collection = user_db.records

app = Flask(__name__)

@app.route("/account", methods=['POST'])
def account():
    content = request.json
    if user_db_collection.find( { "name": content['name'] } ).count() > 0:
        return "Conflict", 409
    else:
        user_db_collection.insert_one({"name": content['name']})
        return "Created", 201

@app.route("/getdata", methods=['GET'])
def collect_data():
    content = request.json
    if user_db_collection.find( { "name": content['name'] } ).count() > 0:
        return "Not Found", 404
    else:
        records = user_db_collection.find({"name": content['name']})
    return records, 200

if __name__ == '__main__':
    logging.info("start at port 8080")
    app.run(host='0.0.0.0', port=8080, debug=True, threaded=True)