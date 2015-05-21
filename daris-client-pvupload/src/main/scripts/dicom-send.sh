#!/bin/bash
# This script wraps the command-line DICOM client interface available from ParaVision 5.1
# It allows one to send Bruker data to a DICOM server.
#
# Input argument is the absolute path to data.  The Bruker structure is
#	 Bruker Directory Hierarchy is
#	 nmr
#	   <Study (Session) Directory >
#	     Subject
#	     <Acquisition Directories> 1:N
#	        imnd
#	        acqp
#	        fid
#	           pdata 
#	             <Reconstruction Directories> 1:N
#	                2dseq
#	                reco
#	                meta
#	
# You can supply data at the <Study> or <Reconstruction> layer
# Does not send the fid (raw data) file !

if [ -z $1 ]; then
    echo "Usage send-dicom.sh <absolute path of the data>. This path can start with anything, but must end with /data/<user>/nmr/<Study>"
    exit 1
fi



# Application root path 
pv_root=/opt/PV5.1
app=$pv_root/prog/bin/scripts/pvDcmExport

# to send to a file instead of DICOM server use e.g.
#$app  -protocol DCMFILE -destPath /tmp/neil -dcmtype MRExport -dset $1

# DICOM configuration - modify to suit
server=daris-1.melbourne.nectar.org.au
port=6667
client_aet=HFI-DICOM-TEST
server_aet=UM-DaRIS-1

# Send
$app -protocol STORESCU -dcmtype MRExport -host $server -port $port -scuAE $client_aet -scpAE $server_aet -dset $1
