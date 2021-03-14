FROM adoptopenjdk/openjdk11
WORKDIR /
ADD target/yuyuko-bot.jar yuyuko-bot.jar
ADD token token
CMD java -Xms128M -Xmx512M -jar yuyuko-bot.jar
