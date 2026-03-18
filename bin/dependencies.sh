#!/bin/bash

cd "$(dirname "$0")"
cd ../code/parent

# mvn package
mvn dependency:tree