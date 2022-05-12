#!/usr/bin/env python3

from flask import Flask, request
from pymongo import MongoClient, database
import logging

mongo_host = "mongo"
phr_db_client = MongoClient("mongodb://mongo:27017/")
phr_db = phr_db_client.phr
phr_db_collection = phr_db.records

app = Flask(__name__)

@app.route("/data", methods=['POST'])
def collect_data():
    # Threat results from personal data being collected, but not processed
    content = request.json
    process(content)
    logging.info('A funny joke has been submitted: %s', content)
    return "OK", 200

# another test case is to simply assign the data to a variable but not use it, i.e. the data is still not processed
@app.route("/data2", methods=['POST'])
def collect_data2():
    content = request.json
    logging.info('A funny joke has been submitted: %s', content)
    return "OK", 200

# another test case is to send a larger message containing tainted and non-tainted data (see client.py query2)
@app.route("/data3", methods=['POST'])
def collect_data3():
    content = request.json
    name = content['name']
    joke = content['joke']
    # joke is processed, but name is not which is a disproportionate collection threat
    logging.info('A funny joke has been submitted: %s', joke)
    return "Haha funny", 200

def process(content):
    print(content)

# @Konrad: any other ideas?

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True, threaded=True)