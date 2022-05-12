#!/usr/bin/env python3

from flask import Flask, request
import json
import requests
from pymongo import MongoClient, database 

# phr_db client (MongoDB)
mongo_host = "mongo"
user_db_client = MongoClient("mongodb://mongo:27017/")
user_db = user_db_client.userdata
user_db_collection = user_db.records

app = Flask(__name__)

def notify_server_user_authentified(user):
    url = 'http://test.com/login'
    return requests.post(url, data = user)


@app.route("/login", methods=['POST'])
def perform_ttp_auth():
    login_credentials = request.json
    user = user_db_collection.find_one(login_credentials)
    if user:
        return notify_server_user_authentified(user)
    return "Forbidden", 403

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True, threaded=True)