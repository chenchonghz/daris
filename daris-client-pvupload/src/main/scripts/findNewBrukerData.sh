#!/bin/bash

PV_ROOT=/opt/PV5.1/data
DATA_ROOT=$PV_ROOT/dwright/nmr
LOG_DIR=$PV_ROOT/neil/logFiles

usage ()
{
   echo "findNewData <name>"
   echo "   where <name> is an optional Study name string (can be wild-carded) to restrict found Study directories with"
}


initLastFindDateFile ()
{

# If the file does not exist, create it  in the past

   file="${1}"
   if [ ! -e "${file}" ]; then
      touch -t 200001010000 "${file}"
   fi
}


# Output directory for log files
if [ ! -e "${LOG_DIR}" ]; then
   echo "Creating output log directory " $LOG_DIR
   mkdir "${LOG_DIR}"
fi


# Set up output file names
of=""
mr="0"
pib="0"
fdg="0"
d=`date +%F`
t=`date +%T`

# This is the output file that holds the found list of data
of="${LOG_DIR}"/newData_"${d}"-"${t}"

# This is the time-stamp file
tf="${LOG_DIR}"/lastFindNewData
initLastFindDateFile "${tf}"

# We only want to find the Study directories
# We want to find all the data new since we last ran this according to the
# date on the 'lastFindNewData' file.  However, we only want files
# older than 2 days from the time we run this to make sure we don't
# pick up files still being created via scanning - we use -mtime -2 for this.
echo "Looking for new data in " $DATA_ROOT
if [ $1 ]; then
   find $DATA_ROOT -mtime +2 -maxdepth 1 -name "${1}" -type d -newer "${tf}" > "${of}"
else 
   find $DATA_ROOT -mtime +2 -maxdepth 1 -type d -newer "${tf}" > "${of}"
fi

# Now set the date of the lastFindNewData file to 2 days in the past so that
# when we next run this, all new data since then will be considered
# This boundary condition will need some tuning to avoid always uploading
# data twice (although that is better than not uploading it)
touch -d '-2 days' "${tf}"

# Edit upload file with upload script
sed -i 's%^%/opt/mediaflux/mfpvload.sh -id %' ${of}
#
d=`date`
sed -i 1i"# Run on ${d}" ${of}
sed -i 1i"#/bin/sh" ${of}
#
echo "Output file = " "${of}"
more "${of}"
