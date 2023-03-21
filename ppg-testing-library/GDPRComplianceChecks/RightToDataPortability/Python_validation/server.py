#!/usr/bin/env python3

from flask import Flask, request
from pymongo import MongoClient, database
import requests

user_db_client = MongoClient("mongodb://mongo:27017/")
user_db = user_db_client.userdata
user_db_collection = user_db.records

app = Flask(__name__)

@app.route("/data", methods=['GET'])
def get_data_in_csv_format():
    req = request.json
    data = {
        "username": req['username']
    }
    if user_db_collection.find( { "username": data['username'] } ).count() > 0:
        return "Conflict", 409
    else:
        # get the data from the database (mongodb)
        user_data = user_db_collection.find_one({"username": data['username']})
        # send the data to the client
        return user_data, 200

@app.route("/transfer", methods=['GET'])
def transfer_data_to_another_service():
    req = request.json
    data = {
        "receiver_url": req['receiver_url'],
        "personal_data": req['personal_data']
    }
    if user_db_collection.find( { "username": data['personal_data']['username'] } ).count() > 0:
        return "Conflict", 409
    else:
        # get the data from the database (mongodb)
        user_data = user_db_collection.find_one({"username": data['personal_data']['username']})
        # VALIDATION: Personal data is not transferred to a third party
        return "OK", 200

@app.route("/store_data", methods=['PUT'])
def parse_data():
    req = request.json
    data = {
        "username": req['username'],
        "notes": req['notes']
    }
    if user_db_collection.find( { "username": data['username'] } ).count() > 0:
        return "Conflict", 409
    else:
        # save data to database
        user_db_collection.insert_one(data)
        return "OK", 200


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True, threaded=True)