#!/bin/sh
#
# This wrapper calls the MBC PET DICOM push Java client. 
#
JAVA=`which java`
if [ -z "${JAVA}" ]; then
        echo "Error: could not find java." >&2
        exit 1
fi

JAR=`dirname $0`/pet-dicomretrieve.jar
if [ ! -f "${JAR}" ]; then
        echo "Error: could not find file pet-dicomretrieve.jar." >&2
        exit 1
fi

# MF_HOST specifies the host name or IP address of the Mediaflux
# server.
#MF_HOST=172.23.65.3
MF_HOST=localhost

# MF_PORT specifies the port number for the Mediaflux server.
MF_PORT=8443

# MF_TRANSPORT specifies the type of tranport to use. Transport is
# one of:
#
#   HTTP
#   HTTPS
#   TCPIP
#
MF_TRANSPORT=HTTPS

# The authentication domain.
MF_DOMAIN=

# The authentication user.
MF_USER=

# The obfuscated authentication password. Fill this is on deployment of script to secure area
MF_PASSWORD=

# Configuration [END]:
# =========================

# Do the upload
$JAVA -Dmf.host=$MF_HOST -Dmf.port=$MF_PORT -Dmf.transport=$MF_TRANSPORT -Dmf.domain=$MF_DOMAIN -Dmf.user=$MF_USER -Dmf.password=$MF_PASSWORD -cp  $JAR nig.mf.petct.client.dicom.retrieve.MBCPETCTDICOMRetrieve "${@}"


#
RETVAL=$?
exit $RETVAL
