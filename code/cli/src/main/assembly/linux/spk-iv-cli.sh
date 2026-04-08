#!/bin/bash

SCRIPT_DIR="$(dirname "$0")"

java -jar $SCRIPT_DIR/tools/spk-iv-cli/imagevalidation-cli-0.3.0-SNAPSHOT.jar "$@"