#!/bin/bash

# change to script directory
cd "$(dirname "$0")"

# build image
podman build -t localhost/spk-iv-cli_exiftool:latest .

# create container
podman create --name spk-iv-cli_exiftool_container localhost/spk-iv-cli_exiftool:latest

# copy binary
podman cp spk-iv-cli_exiftool_container:/usr/bin/exiftool ./exiftool

# delete container
podman rm spk-iv-cli_exiftool_container

ls -lah exiftool
./exiftool -ver