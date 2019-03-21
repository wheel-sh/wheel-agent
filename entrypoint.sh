#!/bin/sh
exec java ${JAVA_OPTS} \
-XX:+UnlockExperimentalVMOptions \
-XX:+UseCGroupMemoryLimitForHeap \
-XX:MaxRAMFraction=1 \
-Djava.security.egd=file:/dev/./urandom \
-jar "/opt/app.jar" "$@"
