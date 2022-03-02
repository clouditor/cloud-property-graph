#!/usr/bin/env python3

import json
import requests
from pymongo import MongoClient, database

# import psycopg2
# user_db_con = psycopg2.connect(database="postgres", user="postgres", password="password", host="postgres")
# user_db = user_db_con.cursor()    

# phr_db client (MongoDB)
mongo_host = "mongo"
user_db_client = MongoClient("mongodb://mongo:27017/")
user_db = user_db_client.userdata
user_db_collection = user_db.records

def query():
    url = 'test.com/data'
    # @Pseudoidentifier
    personal_data = {'name': 'firstname lastname'}
    response = requests.post(url, data = personal_data)    
    # mongo
    records = user_db_collection.find()

    # postgres
    # user_db.execute("""SELECT group_id FROM group_members WHERE user_id=(%s)""", (user_id))
    # rows = user_db.fetchall()


if __name__ == '__main__':
    query()
