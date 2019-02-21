FROM java:openjdk-8-jdk

ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Djava.net.preferIPv4Stack=true"

COPY target/wheel-gitops-agent-thorntail.jar /opt/app.jar

EXPOSE 8080
ENTRYPOINT exec java -jar /opt/app.jar $JAVA_OPTS