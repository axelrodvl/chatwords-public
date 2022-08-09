#!/bin/bash

FOLDER=mongo
N_DAYS=10

if [ ! -d "$FOLDER" ]
then
echo "$FOLDER is not a directory"
exit 2
fi

# Remove
echo "Deleting files in $FOLDER older than $N_DAYS days"
find $FOLDER/* -mtime +$N_DAYS -exec rm {} \;