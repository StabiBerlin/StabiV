#!/bin/bash

PARENT_POM=$(dirname "$0")/../code/parent/pom.xml
ARGS=$(printf "\"%s\" " "$@")

mvn -q -f ${PARENT_POM} -pl ../cli -am clean compile exec:java -Dexec.mainClass="de.spkberlin.imagevalidation.cli.entrypoint.Main" -Dexec.args="${ARGS[*]}"