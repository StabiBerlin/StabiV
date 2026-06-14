#!/bin/bash

# change to script directory
cd "$(dirname "$0")"

# go to root directory
cd ../../

podman run --rm -it -v $(pwd):/root/workspace:z stabiv_production:latest $@