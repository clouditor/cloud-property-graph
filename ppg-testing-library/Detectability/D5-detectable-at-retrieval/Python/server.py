#!/usr/bin/env python3

from flask import Flask, request
from pymongo import MongoClient, database

# phr_db client (MongoDB)
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
    if user_db_collection.find( { "name": data['name'] } ).count() > 0:
        return "Conflict", 409
    else:
        user_db_collection.insert_one({"name": data['name']})
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
    app.run(host='0.0.0.0', port=8080, debug=True, threaded=True)