#!/bin/bash
#
# This wrapper calls the MBC MR raw data upload Java client.  
#
# Remember all inputs
ZZZ=$@

JAVA=`which java`
if [ -z "${JAVA}" ]; then
        echo "Error: could not find java." >&2
        exit 1
fi

JAR=`dirname $0`/mrupload.jar
if [ ! -f "${JAR}" ]; then
        echo "Error: could not find file mrupload.jar." >&2
        exit 1
fi


# Parse inputs for -dest only
DEST="local"
while [ "$1" != "" ]; do
    case $1 in
      "-dest")
        shift
        DEST=$1
        ;;
      *)
       ;;
    esac
    shift
done

# The server parameters to be supplied are:
# MF_HOST : IP or host name

# MF_TRANSPORT specifies the type of transport to use (HTTP,HTTPS,TCPIP)
# MF_PORT : port
#
# Authentication
# MF_TOKEN : secure identity token
#    or
# MF_DOMAIN, MF_USER, MF_PASSWORD (user cred)

MF_HOST=localhost
MF_PORT=8443
MF_TRANSPORT=HTTPS
MF_TOKEN=""
MF_DOMAIN="system"
MF_USER="manager"
MF_PASSWORD=

echo "DEST=" $DEST
if [ "${DEST}" = "mbciu" ]; then
   echo "Setting mbic"
#  MF_HOST=daris-1.melbourne.nectar.org.au
# Non NATed faster interface to DaRIS-1
   MF_HOST=103.6.255.171
   MF_PORT=9443
   MF_TRANSPORT=HTTP
   MF_TOKEN=123
elif [ "${DEST}" = "mbi" ]; then 
   echo "Setting mbi"
   MF_HOST=
   MF_PORT=8443
   MF_TRANSPORT=HTTPS
elif [ "${DEST}" = "local" ]; then 
   echo "Setting local"
   MF_HOST=localhost
   MF_PORT=8443
   MF_TRANSPORT=HTTPS
else
   echo "Unrecognized target destination" $DEST
   echo "Known targets are local, mbciu, mbi"
   exit
fi

# Configuration [END]:
# =========================

# Do the upload. Hand on all original inputs and ignore -dest
$JAVA -Dmf.host=$MF_HOST -Dmf.port=$MF_PORT -Dmf.transport=$MF_TRANSPORT -Dmf.domain=$MF_DOMAIN -Dmf.user=$MF_USER -Dmf.password=$MF_PASSWORD -Dmf.token=$MF_TOKEN -cp $JAR nig.mf.mr.client.upload.MBCMRUpload $ZZZ
#
RETVAL=$?
exit $RETVAL
