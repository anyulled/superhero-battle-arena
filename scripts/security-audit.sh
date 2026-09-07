#!/bin/sh
set -eu

: "${GUIDE_API_TOKEN:?Set GUIDE_API_TOKEN to a Sonatype Guide personal access token}"
: "${NVD_API_KEY:?Set NVD_API_KEY to an NVD API key}"
: "${DEPENDENCY_CHECK_DATA_DIRECTORY:?Set DEPENDENCY_CHECK_DATA_DIRECTORY to a writable Dependency-Check data directory}"

repository=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$repository"

exec ./mvnw -B --settings .mvn/security-settings.xml \
  org.owasp:dependency-check-maven:13.0.0:check \
  -DfailOnError=true \
  -DfailBuildOnCVSS=7 \
  -DossIndexAnalyzerEnabled=true \
  -DossIndexWarnOnlyOnRemoteErrors=false \
  -DossIndexAnalyzerUrl=https://api.guide.sonatype.com \
  -DossIndexAnalyzerUseCache=false \
  -DossIndexServerId=sonatype-guide \
  -DnvdApiKeyEnvironmentVariable=NVD_API_KEY \
  -DdataDirectory="$DEPENDENCY_CHECK_DATA_DIRECTORY" \
  -Dformats=HTML,JSON
