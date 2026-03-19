#!/bin/bash

SCRIPT_DIR="$(dirname "$0")"

java -jar $SCRIPT_DIR/imagevalidation-cli-1.0.0-SNAPSHOT.jar "$@"