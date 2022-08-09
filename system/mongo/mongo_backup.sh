#!/bin/sh
DIR=`date +%d%m%y_%H%M`
DEST=/opt/chatwords/mongo/backup/$DIR
mkdir $DEST
FILE=$DEST.gz
echo $FILE > last_mongo_backup_filename
mongodump --db chatwords --archive=$FILE --gzip
echo "ChatWords database backup $DIR" | mail -s "ChatWords database backup $DIR" vadim@axelrod.co -A $FILE