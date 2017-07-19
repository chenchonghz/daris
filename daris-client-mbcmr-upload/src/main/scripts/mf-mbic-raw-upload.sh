#!/bin/bash

# check java
JAVA=$(which java)
[[ -z $JAVA ]] && echo "Error: Java is not found." >&2 && exit 1

# check jar file.
JAR=$(dirname $0)/mrupload.jar
[[ ! -f $JAR ]] && echo "Error: ${JAR} does not exist." >&2 && exit 2

MF_HOST=localhost
MF_PORT=8443
MF_TRANSPORT=HTTPS
#MF_TOKEN="TBA123"
MF_DOMAIN=system
MF_USER=manager
MF_PASSWORD=


#
echo "Uploading data to ${MF_TRANSPORT}://${MF_HOST}:${MF_PORT}..."
$JAVA -Dmf.host=$MF_HOST -Dmf.port=$MF_PORT -Dmf.transport=$MF_TRANSPORT -Dmf.domain=$MF_DOMAIN -Dmf.user=$MF_USER -Dmf.password=$MF_PASSWORD -Dmf.token=$MF_TOKEN  -cp $JAR nig.mf.mr.client.upload.MBCMRUpload "$@"
RETVAL=$?
exit $RETVAL
