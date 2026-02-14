#!/bin/bash
# exit on error
set -e

# these variables can be overwritten in pom.xml or settings.xml
# NEVER write the real password in this file!!!
: ${BFABRIC_STOREPASS:=neverWriteItHere}
: ${BFABRIC_KEYSTORE:=src/misc/BFABRICDEV.p12}
: ${BFABRIC_KSALIAS:=bfabricdev}
: ${BFABRIC_TSAURL:=http://timestamp.digicert.com}
: ${BFABRIC_FRAGMENTS:=../application/src/main/webapp/fragments/}
: ${BFABRIC_VERIFY:=true}

# add manifest to jar
jar uvfm target/downloadmanager.jar src/misc/MANIFEST.MF

# sign the jar file with the FGCZ certificate
# the tsa is optional, but without a warning is shown
jarsigner -storetype pkcs12 \
    -keystore "${BFABRIC_KEYSTORE}" \
    -storepass "${BFABRIC_STOREPASS}" \
    -tsa "${BFABRIC_TSAURL}" \
    -signedjar target/downloadmanager.jar target/downloadmanager.jar "${BFABRIC_KSALIAS}"

# verify the signed jar
[ "${BFABRIC_VERIFY,,}" = "true" ] && jarsigner -verify -certs target/downloadmanager.jar

#copy signed jar to fragments folder
cp -vf target/downloadmanager.jar "${BFABRIC_FRAGMENTS}"

