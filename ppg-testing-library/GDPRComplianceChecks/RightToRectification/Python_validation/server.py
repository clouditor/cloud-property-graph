#!/usr/bin/env python3

from flask import Flask, request
from pymongo import MongoClient, database

user_db_client = MongoClient("mongodb://mongo:27017/")
user_db = user_db_client.userdata
user_db_collection = user_db.records

app = Flask(__name__)

@app.route("/data", methods=['PUT'])
def parse_data():
    req = request.json
    data = {
        "username": req['username'],
        "name": req['name'],
        "notes": req['notes']
    }
    if user_db_collection.find( { "name": data['name'] } ).count() > 0:
        return "Conflict", 409
    else:
        # VALIDATION: no rectification is performed => No update call to the database
        return "Created", 201

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True, threaded=True)