#mvn clean package -DskipTests
# scp system/systemd/chatwords.service root@axelrod.co:/etc/systemd/system

ssh name@server "sudo systemctl stop chatwords.service"
scp target/chatwords*.jar name@server:/opt/chatwords/chatwords.jar
scp run.sh name@server:/opt/chatwords/run.sh
scp -r config/* name@server:/opt/chatwords/config
scp -r system/mongo/* name@server:/opt/chatwords/mongo
ssh name@server "sudo systemctl start chatwords.service"
