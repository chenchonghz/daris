#!/bin/bash
REPO=$1
ROOT=$2
GROUP_ID=au.edu.unimelb.daris

if [[ -z $(which mvn) ]]; then
    echo "Error: mvn command is not available. Make sure maven is installed." 2>&1
    exit 1
fi

if [[ -z $REPO ]]; then
    echo "Error: the path to the maven repository is missing." 2>&1
    echo "Usage: $(basename $0) <repo-path> [daris-path]"
    exit 2
fi

if [[ ! -d $REPO ]]; then
    echo "Error: directory does not exists: ${REPO}" 2>&1
    echo "Usage: $(basename $0) <repo-path> [daris-path]"
    exit 3
fi

CWD=$(pwd)
if [[ -z $ROOT ]]; then
    ROOT=$(cd $(dirname $0); cd ../../../; pwd; cd $CWD)
else
    if [[ ! -d $ROOT ]]; then
        echo "Error: directory does not exists: ${ROOT}" 2>&1
        exit 4
    fi 
    ROOT=$(cd $ROOT; pwd; cd $CWD)
fi

## daris-commons
ARTIFACT_ID=daris-commons
DIR=$ROOT/${ARTIFACT_ID}
if [[ ! -d ${DIR} ]]; then
    echo "Directory does not exist: ${DIR}" 2>&1
    exit 3
fi

# compile
cd $DIR; mvn clean install

FILE=$(cd $DIR/target; ls ${ARTIFACT_ID}-*.jar)
VERSION=$(echo "${FILE%.*}" | cut -f3 -d \-)

mvn install:install-file -Dfile=${DIR}/target/${FILE} -DgroupId=${GROUP_ID} -DartifactId=${ARTIFACT_ID} -Dname=${ARTIFACT_ID} -Dversion=${VERSION} -Dpackaging=jar -DperformRelease=true -DcreateChecksum=true -DgeneratePom=true -DlocalRepositoryPath=${REPO}

## daris-dcmtools
ARTIFACT_ID=daris-dcmtools
DIR=$ROOT/${ARTIFACT_ID}
if [[ ! -d ${DIR} ]]; then
    echo "Directory does not exist: ${DIR}" 2>&1
    exit 3
fi

# compile
cd $DIR; mvn clean install

FILE=$(cd $DIR/target; ls ${ARTIFACT_ID}-*.jar)
VERSION=$(echo "${FILE%.*}" | cut -f3 -d \-)

mvn install:install-file -Dfile=${DIR}/target/${FILE} -DgroupId=${GROUP_ID} -DartifactId=${ARTIFACT_ID} -Dname=${ARTIFACT_ID} -Dversion=${VERSION} -Dpackaging=jar -DperformRelease=true -DcreateChecksum=true -DgeneratePom=true -DlocalRepositoryPath=${REPO}

cd $CWD
