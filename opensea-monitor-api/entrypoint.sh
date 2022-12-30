#!/bin/sh

cd /opensea-monitor-api
./gradlew clean build --no-daemon
java -jar ./build/libs/opensea-monitor-api-0.1.0.jar