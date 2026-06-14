#!/bin/bash

# change to script directory
cd "$(dirname "$0")"

# build image
podman build -t localhost/stabiv_exiftool:latest .

# create container
podman create --name stabiv_exiftool_container localhost/stabiv_exiftool:latest

# copy binary
podman cp stabiv_exiftool_container:/usr/bin/exiftool ./exiftool

# delete container
podman rm stabiv_exiftool_container

ls -lah exiftool
./exiftool -ver