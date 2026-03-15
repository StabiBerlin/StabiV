#!/bin/bash


POM_PATH="$(dirname "$0")/../code/cli/pom.xml"
ARGS=$(printf "\"%s\" " "$@")

mvn -q --file ${POM_PATH}  exec:java -Dexec.mainClass="de.spkberlin.imagevalidation.cli.entrypoint.Main" -Dexec.args="${ARGS[*]}"