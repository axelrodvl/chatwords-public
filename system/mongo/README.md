On your server, simply open the crontab like this:

sudo crontab -e

In that file, underneath all the comments at the top, enter the following line:

0 1 * * * /opt/tou/mongo_backup.sh
0 */6 * * * /opt/tou/mongo_analytics.sh