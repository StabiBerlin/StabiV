#!/bin/bash

PROJECT_NAME=image_validation_cli_development

PS_OUTPUT=$(podman-compose -p ${PROJECT_NAME} ps -f json)

if [ "${PS_OUTPUT}" != "[]" ]; then
    # stop running containers before restarting them
    podman-compose -p ${PROJECT_NAME} down
fi

podman-compose -p ${PROJECT_NAME} up -d
podman exec -it ${PROJECT_NAME}_server_1 /bin/bash
podman-compose -p ${PROJECT_NAME} down