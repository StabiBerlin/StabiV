#!/bin/bash

# see https://exiftool.org/TagNames/EXIF.html

# set metadata values according to sbk-berlin specification

tiffset -s Make "scanner manufacturer" $1
tiffset -s Model "scanner model" $1
tiffset -s Software "scanner software" $1

# --- Artist
exiftool -IFD0:Artist="image artist" $1
# exiftool -IFD0:Artist=" " $1 # invalid
# exiftool -IFD0:Artist= $1 # invalid

# --- BodySerialNumber, required
exiftool -EXIF:SerialNumber=1234567890 $1 # valid
# exiftool -EXIF:SerialNumber= $1 # invalid

# --- SubfileType, required
exiftool -IFD0:SubfileType="Full-resolution image" $1 # valid
# exiftool -IFD0:OldSubfileType="Full-resolution image" $1 # invalid

# --- FillOrder, optional, if present = Normal
exiftool -IFD0:FillOrder="Normal" $1 # valid
# exiftool -IFD0:FillOrder= $1 # valid
# exiftool -IFD0:FillOrder="Reversed" $1 # invalid

# --- ImageDescription, optional, if present = not empty
exiftool -IFD0:ImageDescription="image description" $1 # valid
# exiftool -IFD0:ImageDescription=" " $1 # invalid
# exiftool -IFD0:ImageDescription= $1 # valid

echo "show metadata"
exiftool -a -u -s -G5 $1
