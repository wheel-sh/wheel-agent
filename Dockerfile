FROM openjdk:8-jre-alpine
ADD entrypoint.sh entrypoint.sh
ADD target/*.jar /opt/app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "/entrypoint.sh"]
