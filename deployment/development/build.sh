#!/bin/bash

PROJECT_NAME=image_validation_cli_development

podman-compose pull
podman-compose -p ${PROJECT_NAME} build