#!/bin/bash

[[ -z $(which java) ]] && echo "Java is not found." 2>&1 && exit 1

if [[ -z $DARIS_CLIENT_JAR_PATH ]]; then
    # current work directory
    CWD=$(pwd)
    # script directory
    SD=$(cd $(dirname $0); pwd; cd $CWD)
    # daris-client.jar
    DARIS_CLIENT_JAR_PATH=$SD/daris-client.jar
fi
if [[ ! -f ${DARIS_CLIENT_JAR_PATH} ]]; then
    echo "${DARIS_CLIENT_JAR_PATH} does not exist. try 'export DARIS_CLIENT_JAR_PATH=/path/to/daris-client.jar'" 2>&1
    exit 2
fi

java -Ddc.prefix=$(basename $0) -jar $DARIS_CLIENT_JAR_PATH "$@"