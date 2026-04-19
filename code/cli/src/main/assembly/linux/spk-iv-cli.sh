#!/bin/bash

# directory of this script
SCRIPT_DIR="$(dirname "$0")"

# path to bundled java
export JAVA_HOME="$SCRIPT_DIR/tools/java"

# path to static exiftool binary
export EXIFTOOL_HOME="$SCRIPT_DIR/tools/exiftool"

# add exiftool to path
export PATH="$EXIFTOOL_HOME:$PATH"

# run image validation cli
$JAVA_HOME/bin/java -cp "$SCRIPT_DIR/tools/spk-iv-cli/*" de.spkberlin.imagevalidation.cli.entrypoint.Main "$@"