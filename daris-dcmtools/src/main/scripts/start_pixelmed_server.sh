#!/bin/sh
# Script to start the pixelmed server wrapped up by class nig.dicom.server.DICOMServer
# You must set lib which must hold dcmtools.jar
#
lib=$HOME/lib

# Parse
args="${@}"  
copy="copy"
pack="yes"
res="/tmp/pixelmed"
if [ "${1}" ]; then
  res="${1}"
fi


# Final Received directory
echo "Received parent directory = " ${res}
if [ ! -d "${res}" ]; then
   mkdir -v "${res}"
fi
if [ ! -d "${res}" ]; then
   echo "*** Failed to create/find output received directory " + "${res}"
   exit
fi

# Temporary directory under this parent
tmp="${res}"/temp
echo "Received temporary directory = " ${tmp}
if [ ! -d "${tmp}" ]; then
   mkdir -v "${tmp}"
fi
if [ ! -d "${tmp}" ]; then
   echo "*** Failed to create/find output temporary directory " + "${tmp}"
   exit
fi

# port AET tmp_dir, result_dir
AET=PIXELMED
PORT=11111
echo "AET = " $AET
echo "port = " $PORT
java -cp $lib/dcmtools.jar nig.dicom.server.DICOMServer ${PORT} ${AET} "${tmp}" "${res}"