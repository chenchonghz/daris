#!/bin/bash


# Extract versions for commons from build files
findCommonsVersion ()
{
  cd $SRC_DIR/NIGTK/commons
  local t=`grep app.version build.properties`
  local v=${t#*=}
  echo $v
}

findCommonsMFVersion ()
{
  cd $SRC_DIR/NIGTK/commons
  local t=`grep mf.server.version build.properties`
  local v="Unknown"
  if [ ! -z "$t" ]; then
     v=${t#*=}
  fi
  echo $v
}


#
# Ant settings
#
ANT_HOME=/usr/bin

export PATH=$PATH:$ANT_HOME

#
# Mediaflux settings
#
export MFLUX_HOME=/opt/mflux

export MFLUX_HOST=$(hostname)

export MFLUX_PORT=8443

export MFLUX_TRANSPORT=HTTPS

export MFLUX_DOMAIN=system

export MFLUX_USER=manager

export MFLUX_PASSWORD_ENC=R3Vlc3NfbWU4Cg==

export MFLUX_UMASK=0007

MFCOMMAND=/data/local/mediaflux/bin/mfcommand

#
# Settings inside Mediaflux server
#
APP_NAME=daris-downloads

APP_NAMESPACE=/www/${APP_NAME}

APP_LABEL=${APP_NAME}

APP_URL=/${APP_NAME}


#
# GIT settings
#
GIT_ROOT=/data/daris/services/git

#
# Local directory settings
#
ROOT_DIR=/data/daris/users/mflux/daily-build

SRC_DIR=$ROOT_DIR/src

BUILD_DIR=$ROOT_DIR/build

DIST_DIR=$ROOT_DIR/dist

WWW_DIR=$ROOT_DIR/www

CWD=`pwd`

if [[ ! -d $SRC_DIR ]]; then
	mkdir -p $SRC_DIR
fi

if [[ -z `which git` ]]; then
	echo "Cannot find git." 2>&1
	exit 1
fi

if [[ -d $SRC_DIR/NIGTK ]]; then
	rm -fr $SRC_DIR/NIGTK
fi

# clone current NIGTK, DaRIS  and build 
cd $SRC_DIR; git clone $GIT_ROOT/NIGTK.git; cd $SRC_DIR/NIGTK; ant

if [[ -d $SRC_DIR/DaRIS ]]; then
	rm -fr $SRC_DIR/DaRIS
fi
cd $SRC_DIR; git clone $GIT_ROOT/DaRIS.git; cd $SRC_DIR/DaRIS; ant

if [[ -d $WWW_DIR ]]; then
	rm -fr $WWW_DIR
fi

mkdir -p $WWW_DIR; mkdir -p $WWW_DIR/latest; mkdir -p $WWW_DIR/stable

# Copy the packages into the 'latest' directory
find $DIST_DIR -type f -name "*.zip" -print0 | xargs -0 -I {} cp {} $WWW_DIR/latest/
for f in $(ls $WWW_DIR/latest/)
do
	nf="$(echo $f | sed 's/\(.*\)\..*/\1/')-latest.zip"
	mv $WWW_DIR/latest/$f $WWW_DIR/latest/$nf
done 
# Copy  nig-commons.jar into 'latest'
# Have to fish the versions out of the source tree properties files
# and remember as gets overwritten for stable
find $DIST_DIR -type f -name nig-commons.jar -exec cp  \{\} $WWW_DIR/latest/nig-commons-latest.jar \;
commonsLatestVersion=`findCommonsVersion`
commonsLatestMFVersion=`findCommonsMFVersion`

# Now  clone latest stable NIGTK and DaRIS and build
#
# FInd the tags and sort them into numerical order.
# The tags are of the form stable-N-M e.g. stable-2-10
# so we need to sort on both the N and the M
cd $SRC_DIR/NIGTK
TAG=`git tag -l | sort -n -k 1.8 -k 1.10 | tail -n 1`
echo "*********************************************"
echo "*********************************************"
echo "TAG="$TAG
echo "*********************************************"
echo "*********************************************"
cd $SRC_DIR/NIGTK; git checkout tags/$TAG -f; ant

cd $SRC_DIR/DaRIS; git checkout tags/$TAG -f; ant

# Copy the packages into the 'stable' directory
find $DIST_DIR -type f -name "*.zip" -print0 | xargs -0 -I {} cp {} $WWW_DIR/stable/
for f in $(ls $WWW_DIR/stable/)
do
	nf="$(echo $f | sed 's/\(.*\)\..*/\1/')-stable.zip"
	mv $WWW_DIR/stable/$f $WWW_DIR/stable/$nf
done 
# Copy  nig-commons.jar into 'stable'
find $DIST_DIR -type f -name nig-commons.jar -exec cp  \{\} $WWW_DIR/stable/nig-commons-stable.jar \;
commonsStableVersion=`findCommonsVersion`
commonsStableMFVersion=`findCommonsMFVersion`


# generate index.html
INDEX_HTML=$WWW_DIR/index.html

PKG_DIR=latest
echo "<html><head><title>Neuroimaging Software - Download</title></head><body><h2 align=\"center\">Neuroimaging Software Download</h2>" > $INDEX_HTML
echo "<table width=\"80%\" align=\"center\" border=\"1\" padding=\"2\">" >> $INDEX_HTML
echo "<tr bgcolor=#f0f0f0><th align=\"center\" colspan=4><h3>Latest Builds ($(date '+%H:%M:%S %a,%d/%b/%Y'))</h3></tr>" >> $INDEX_HTML
echo "<tr bgcolor=#c0c0c0><td align=\"center\">Name</td><td  align=\"center\">Version</td><td align=\"center\">Type</td><td align=\"center\">Required <br/> Mediaflux Version</td></tr>" >> $INDEX_HTML

# nig-commons 
PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/nig-commons*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=${commonsLatestVersion}
PKG_MF_VER=${commonsLatestMFVersion}
echo "<tr><td><a href=\"${PKG_PATH}\">nig-commons</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">Jar file</td><td  align=\"center\">${PKG_MF_VER}</td></tr>" >> $INDEX_HTML

PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/mfpkg-nig_essentials*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $3}')
PKG_MF_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $4}' | sed 's/^mf//')
echo "<tr><td><a href=\"${PKG_PATH}\">NIG-Essentials</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">Plugin Package</td><td align=\"center\">${PKG_MF_VER}</td></tr>" >> $INDEX_HTML

PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/mfpkg-nig_transcode*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $3}')
PKG_MF_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $4}' | sed 's/^mf//')
echo "<tr><td><a href=\"${PKG_PATH}\">NIG-Transcode</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">Plugin Package</td><td  align=\"center\">${PKG_MF_VER}</td></tr>" >> $INDEX_HTML

PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/mfpkg-pssd*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $3}')
PKG_MF_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $4}' | sed 's/^mf//')
echo "<tr><td><a href=\"${PKG_PATH}\">PSSD</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">Plugin Package</td><td  align=\"center\">${PKG_MF_VER}</td></tr>" >> $INDEX_HTML

PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/mfpkg-nig_pssd*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $3}')
PKG_MF_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $4}' | sed 's/^mf//')
echo "<tr><td><a href=\"${PKG_PATH}\">NIG-PSSD</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">Plugin Package</td><td  align=\"center\">${PKG_MF_VER}</td></tr>" >> $INDEX_HTML

PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/mfpkg-daris*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $3}')
PKG_MF_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $4}' | sed 's/^mf//')
echo "<tr><td><a href=\"${PKG_PATH}\">DaRIS Portal</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">Plugin Package</td><td  align=\"center\">${PKG_MF_VER}</td></tr>" >> $INDEX_HTML

PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/server-config*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $3}')
PKG_MF_VER=""
echo "<tr><td><a href=\"${PKG_PATH}\">NIG-ServerConfig</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">Client Application</td><td  align=\"center\">&nbsp;</td></tr>" >> $INDEX_HTML

PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/pvupload*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $2}')
PKG_MF_VER=""
echo "<tr><td><a href=\"${PKG_PATH}\">NIG-PVUpload</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">Client Application</td><td  align=\"center\">&nbsp;</td></tr>" >> $INDEX_HTML

PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/dicom-client*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $3}')
PKG_MF_VER=""
echo "<tr><td><a href=\"${PKG_PATH}\">NIG-DicomClient</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">Client Application</td><td  align=\"center\">&nbsp;</td></tr>" >> $INDEX_HTML

PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/dcmtools*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $2}')
PKG_MF_VER=""
echo "<tr><td><a href=\"${PKG_PATH}\">NIG-DCMTools</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">DICOM Tools</td><td  align=\"center\">&nbsp;</td></tr>" >> $INDEX_HTML

echo "</table>" >> $INDEX_HTML

echo "<br><br>" >> $INDEX_HTML

PKG_DIR=stable

echo "<table width=\"80%\" align=\"center\" border=\"1\" padding=\"2\">" >> $INDEX_HTML

echo "<tr bgcolor=#f0f0f0><th align=\"center\" colspan=4><h3>Stable Builds (${TAG})</h3></tr>" >> $INDEX_HTML

echo "<tr bgcolor=#c0c0c0><td align=\"center\">Name</td><td  align=\"center\">Version</td><td align=\"center\">Type</td><td align=\"center\">Required <br/> Mediaflux Version</td></tr>" >> $INDEX_HTML

# nig-commons 
PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/nig-commons*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=${commonsStableVersion}
PKG_MF_VER=${commonsStableMFVersion}
echo "<tr><td><a href=\"${PKG_PATH}\">nig-commons</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">Jar file</td><td  align=\"center\">${PKG_MF_VER}</td></tr>" >> $INDEX_HTML

PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/mfpkg-nig_essentials*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $3}')
PKG_MF_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $4}' | sed 's/^mf//')
echo "<tr><td><a href=\"${PKG_PATH}\">NIG-Essentials</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">Plugin Package</td><td align=\"center\">${PKG_MF_VER}</td></tr>" >> $INDEX_HTML

PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/mfpkg-nig_transcode*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $3}')
PKG_MF_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $4}' | sed 's/^mf//')
echo "<tr><td><a href=\"${PKG_PATH}\">NIG-Transcode</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">Plugin Package</td><td  align=\"center\">${PKG_MF_VER}</td></tr>" >> $INDEX_HTML

PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/mfpkg-pssd*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $3}')
PKG_MF_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $4}' | sed 's/^mf//')
echo "<tr><td><a href=\"${PKG_PATH}\">PSSD</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">Plugin Package</td><td  align=\"center\">${PKG_MF_VER}</td></tr>" >> $INDEX_HTML

PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/mfpkg-nig_pssd*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $3}')
PKG_MF_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $4}' | sed 's/^mf//')
echo "<tr><td><a href=\"${PKG_PATH}\">NIG-PSSD</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">Plugin Package</td><td  align=\"center\">${PKG_MF_VER}</td></tr>" >> $INDEX_HTML

PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/mfpkg-daris*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $3}')
PKG_MF_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $4}' | sed 's/^mf//')
echo "<tr><td><a href=\"${PKG_PATH}\">DaRIS Portal</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">Plugin Package</td><td  align=\"center\">${PKG_MF_VER}</td></tr>" >> $INDEX_HTML

PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/server-config*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $3}')
PKG_MF_VER=""
echo "<tr><td><a href=\"${PKG_PATH}\">NIG-ServerConfig</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">Client Application</td><td  align=\"center\">&nbsp;</td></tr>" >> $INDEX_HTML

PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/pvupload*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $2}')
PKG_MF_VER=""
echo "<tr><td><a href=\"${PKG_PATH}\">NIG-PVUpload</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">Client Application</td><td  align=\"center\">&nbsp;</td></tr>" >> $INDEX_HTML

PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/dicom-client*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $3}')
PKG_MF_VER=""
echo "<tr><td><a href=\"${PKG_PATH}\">NIG-DicomClient</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">Client Application</td><td  align=\"center\">&nbsp;</td></tr>" >> $INDEX_HTML

PKG_NAME=$(basename $(ls $WWW_DIR/$PKG_DIR/dcmtools*))
PKG_PATH=$PKG_DIR/$PKG_NAME
PKG_VER=$(echo ${PKG_NAME} | tr "-" " " | awk '{print $2}')
PKG_MF_VER=""
echo "<tr><td><a href=\"${PKG_PATH}\">NIG-DCMTools</a></td><td  align=\"center\">${PKG_VER}</td><td align=\"center\">DICOM Tools</td><td  align=\"center\">&nbsp;</td></tr>" >> $INDEX_HTML

echo "</table>" >> $INDEX_HTML

echo "<center><p align=\"center\"><b>Note:</b> Some software require specific (or higher) Mediaflux server version. Please see the <b>Required Mediaflux Version</b> column.</p></center>" >> $INDEX_HTML

echo "</body></html>" >> $INDEX_HTML

# import the directory into MF
$MFCOMMAND logon $MFLUX_DOMAIN $MFLUX_USER $(echo $MFLUX_PASSWORD_ENC | base64 -d)
$MFCOMMAND "asset.namespace.destroy :force true :namespace $APP_NAMESPACE"
$MFCOMMAND "asset.namespace.create :namespace $APP_NAMESPACE :store data"
$MFCOMMAND asset.import :namespace $APP_NAMESPACE :update true :label -create true $APP_LABEL :label PUBLISHED :url file:${WWW_DIR}
$MFCOMMAND http.processor.destroy :app ${APP_NAME} :url ${APP_URL}
$MFCOMMAND "http.processor.create :app ${APP_NAME} :url ${APP_URL} :type asset :translate ${APP_NAMESPACE} :entry-point index.html"
$MFCOMMAND logoff


# clean up the directories
if [ $? -eq 0 ]; then
    echo "cleaning up..."
    rm -fr $WWW_DIR
    rm -fr $SRC_DIR
    rm -fr $BUILD_DIR
    rm -fr $DIST_DIR
fi

cd $CWD
