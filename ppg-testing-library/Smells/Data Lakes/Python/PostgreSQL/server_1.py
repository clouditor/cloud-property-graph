#!/usr/bin/env python3

from flask import Flask, request
import json
import requests
import logging
import os, sys
import psycopg2

user_db_con = psycopg2.connect(database="postgres", user="postgres", password="password", host="postgres")
user_db = user_db_con.cursor()    

app = Flask(__name__)

@app.route("/data", methods=['POST'])
def collect_data():
    content = request.json
    user_db.execute("""SELECT group_id FROM group_members WHERE user_id=(%s)""", (content))
    rows = user_db.fetchall()
    return "OK", 200

if __name__ == '__main__':
    logging.info("start at port 8080")
    app.run(host='0.0.0.0', port=8080, debug=True, threaded=True)