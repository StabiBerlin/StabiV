#!/bin/bash

cd "$(dirname "$0")"
cd ..

NEW_VERSION=${1:-}
CONSTANTS_FILE=code/cli/src/main/java/de/spkberlin/imagevalidation/cli/common/Constants.java
DOCKER_FILE=deployment/production/Dockerfile

if [[ -z "$NEW_VERSION" ]]; then
    echo "You need to provide a version string as first and only argument to this script-"
    exit 1
fi

echo "Bumping version to $NEW_VERSION"

# check changelog contains version entry
if ! grep -qE "^## v$NEW_VERSION" CHANGELOG.md; then
    echo "WARNING: CHANGELOG.md does not contain an entry for version $NEW_VERSION"
fi

# update pom.xml files
(cd code/parent && mvn versions:set -q -DnewVersion="$NEW_VERSION" -DgenerateBackupPoms=false)

# update Constants.java
sed -i -E "s/(VERSION = \")[^\"]+(\")/\1$NEW_VERSION\2/" "$CONSTANTS_FILE"

# update Dockerfile
sed -i -E "s/imagevalidation-cli-[^/ ]+\.jar/imagevalidation-cli-${NEW_VERSION}.jar/g" $DOCKER_FILE