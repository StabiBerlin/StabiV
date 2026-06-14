#!/bin/bash

# change to script directory
cd "$(dirname "$0")"

# go to root directory
cd ../../

# build image
podman build -f deployment/production/Dockerfile -t stabiv_production:latest .