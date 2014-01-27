#!/bin/sh

DBDECRYPT=dbdecrypt.py
KEY=justdoit

for i in `ls /tmp/*.db`
do
 python $DBDECRYPT -p $KEY $i
 sqlite3 -line $i 'select * from data;'
done
