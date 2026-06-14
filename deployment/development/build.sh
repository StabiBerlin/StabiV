#!/bin/bash

PROJECT_NAME=stabiv_development

podman-compose pull
podman-compose -p ${PROJECT_NAME} build