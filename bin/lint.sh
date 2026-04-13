#!/bin/bash

cd "$(dirname "$0")"
cd ../code/parent

mvn -am compile checkstyle:checkstyle