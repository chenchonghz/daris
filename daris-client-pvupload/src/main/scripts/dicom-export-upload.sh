#!/bin/sh
#
# Script to export data from Bruker scanner as DICOM and then send to a DaRIS DICOM server
# Args are
# <input study path> absolute path to study which must include the trailing path structure .../data/<user>/nmr/<study>
# <DaRIS CID>
#
#
# We use a static temporary directory to export to, so only one process can run at a time

nArgs=$#
if [ "${nArgs}" != "2" ]; then  
  echo "Usage :  dicom-export-upload <absolute Study Path> <DaRIS CID>"
  exit
fi


# First clean up
tmpDir=/tmp/DICOM-Export
if [ -d ${tmpDir} ]; then
   echo "Cleaning up " $tmpDir
   rm -fr ${tmpDir}/*
else
  echo "Creating " $tmpDir
  mkdir ${tmpDir}
fi

# Export the data.  Must supply absolute path.
dicom-export "${1}" "${tmpDir}"
#cp -r /home/nkilleen/DICOM/* $tmpDir

# Now edit in-situ with the CID and upload with the standard DaRIS SCU client
host=daris-1.melbourne.nectar.org.au
port=6667
callingAET=HFI-DICOM-TEST
calledAET=UM-DaRIS-1
echo "*********************"
echo "     Sending data"
echo "*********************"
dicom-scu.sh -insitu -dir "${tmpDir}" -nochk -id "${2}" -host $host -port $port  -calledAET $calledAET -callingAET $callingAET



