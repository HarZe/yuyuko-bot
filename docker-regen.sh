sudo docker stop yuyuko-bot
sudo docker rm yuyuko-bot
sudo docker rmi yuyuko-bot
mvn clean package
sudo docker build -t yuyuko-bot .
sudo docker run -d --name yuyuko-bot yuyuko-bot
