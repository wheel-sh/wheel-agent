FROM registry.cloud.nikio.io/base/oc-java-8
ADD entrypoint.sh entrypoint.sh
ADD target/*.jar /opt/app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "/entrypoint.sh"]