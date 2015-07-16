#!/bin/bash

# current work directory
CWD=$(pwd)

# script directory
SD=$(cd $(dirname $0); pwd; cd $CWD)

# daris-client.jar
DARIS_CLIENT_JAR=$SD/daris-client.jar

[[ ! -f $DARIS_CLIENT_JAR ]] && echo "${DARIS_CLIENT_JAR} does not exist." 2>&1 && exit 1

[[ -z $(which java) ]] && echo "Java is not found." 2>&1 && exit 2

java -jar $DARIS_CLIENT_JAR "$@"