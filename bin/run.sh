#!/bin/bash

PARENT_POM=$(dirname "$0")/../code/parent/pom.xml
ARGS=$(printf "\"%s\" " "$@")

mvn -f ${PARENT_POM} -pl ../cli -am compile exec:java -P !checkstyle,!spotless -Dexec.mainClass="de.spkberlin.imagevalidation.cli.entrypoint.Main" -Dexec.args="${ARGS[*]}"