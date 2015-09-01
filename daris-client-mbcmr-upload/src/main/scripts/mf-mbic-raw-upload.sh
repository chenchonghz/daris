#!/bin/bash

# check java
JAVA=$(which java)
[[ -z $JAVA ]] && echo "Error: Java is not found." >&2 && exit 1

# check jar file.
JAR=$(dirname $0)/mrupload.jar
[[ ! -f $JAR ]] && echo "Error: ${JAR} does not exist." >&2 && exit 2

# this script is for MBC 7T MR to send data to daris-1.cloud.unimelb.edu.au only.
MF_HOST=103.6.255.171
MF_PORT=9443
MF_TRANSPORT=HTTP
MF_TOKEN="TBA123"

#
echo "Uploading data to ${MF_TRANSPORT}://${MF_HOST}:${MF_PORT}..."
$JAVA -Dmf.host=$MF_HOST -Dmf.port=$MF_PORT -Dmf.transport=$MF_TRANSPORT -Dmf.token=$MF_TOKEN -cp $JAR nig.mf.mr.client.upload.MBCMRUpload "$@"
RETVAL=$?
exit $RETVAL
