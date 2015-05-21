#!/bin/sh
# Script to export DICOM data from Bruker scanner with ParaVision software
#
# args are 
# <input study path>  absolute path to study which must include the trailing path structure .../data/<user>/nmr/<study>
# <output directory>
#


nArgs=$#
if [ "${nArgs}" != "2" ]; then  
  echo "Usage :  dicom-export <absolute Study Path> <DaRIS CID>"
  exit
fi

app=/opt/PV5.1/prog/bin/scripts/pvDcmExport
$app  -protocol DCMFILE -destPath "${2}" -dcmtype MRExport -dset "${1}"