#!/bin/bash

# read script arguments
SOURCE_IMAGE=$1
NUMBER_OF_DIRECTORIES=$2
NUMBER_OF_FILES_PER_DIRECTORY=$3

# check script arguments
if [ -z "$SOURCE_IMAGE" ]; then
    echo "you need to specify the path to the source image as first argument"
    exit 1
fi

if [ -z "$NUMBER_OF_DIRECTORIES" ]; then
    echo "you need to specify the number of directories that are generated as second argument"
    exit 1
fi

if [ -z "$NUMBER_OF_FILES_PER_DIRECTORY" ]; then
    echo "you need to specify the number of files that are generated for each directory as third argument"
    exit 1
fi

# clean generated directory
rm -rf generated
mkdir -p generated
echo -e "**\n!.gitignore" > generated/.gitignore

# generate files
for i in $(seq -f "%05g" 1 $NUMBER_OF_DIRECTORIES); do
    echo "create directory $i"
    mkdir -p generated/$i
    for j in $(seq -f "%08g" 1 $NUMBER_OF_FILES_PER_DIRECTORY); do
        cp $SOURCE_IMAGE generated/$i/$j.tif
    done
done

