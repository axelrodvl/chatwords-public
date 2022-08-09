#!/bin/sh
LAST_BACKUP=$(cat last_mongo_backup_filename)
echo "Restoring mongo from backup: $LAST_BACKUP"
mongorestore --gzip --archive=$LAST_BACKUP --db chatwords --drop