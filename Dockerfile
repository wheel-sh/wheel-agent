FROM fabric8/java-centos-openjdk8-jdk
ADD entrypoint.sh entrypoint.sh
ADD target/*.jar /opt/app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "/entrypoint.sh"]