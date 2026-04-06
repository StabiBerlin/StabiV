#!/bin/bash

# set metadata values according to sbk-berlin specification
# see https://exiftool -overwrite_original.org/TagNames/EXIF.html

rm -rf valid
rm -rf invalid
mkdir valid
mkdir invalid

SOURCE=base.tif

echo "create $SOURCE as base valid file"

exiftool -overwrite_original -IFD0:Make="scanner manufacturer" $SOURCE
exiftool -overwrite_original -IFD0:Model="scanner model" $SOURCE
exiftool -overwrite_original -IFD0:Software="scanner software" $SOURCE
exiftool -overwrite_original -IFD0:Artist="image artist" $SOURCE
exiftool -overwrite_original -EXIF:SerialNumber="1234567890" $SOURCE
exiftool -overwrite_original -IFD0:SubfileType="Full-resolution image" $SOURCE
exiftool -overwrite_original -IFD0:FillOrder="Normal" $SOURCE
exiftool -overwrite_original -IFD0:ImageDescription="image description" $SOURCE
exiftool -overwrite_original -IFD0:Orientation="Horizontal (normal)" $SOURCE
exiftool -overwrite_original -IFD0:SamplesPerPixel=3 $SOURCE
exiftool -overwrite_original -IFD0:PlanarConfiguration="Chunky" $SOURCE
exiftool -overwrite_original -IFD0:ModifyDate="2026:03:22 15:11:22" $SOURCE
exiftool -overwrite_original -IFD0:WhiteBalance="Auto" $SOURCE

echo "show metadata"
exiftool -overwrite_original -a -u -s -G5 $SOURCE

cp $SOURCE valid/base.tif


echo "create valid alternatives"

cp $SOURCE valid/no_subfiletype.tif
exiftool -overwrite_original -IFD0:SubfileType= valid/no_subfiletype.tif

cp $SOURCE valid/no_fill_order.tif
exiftool -overwrite_original -IFD0:FillOrder= valid/no_fill_order.tif

cp $SOURCE valid/no_image_description.tif
exiftool -overwrite_original -IFD0:ImageDescription= valid/no_image_description.tif

cp $SOURCE valid/no_orientation.tif
exiftool -overwrite_original -IFD0:Orientation= valid/no_orientation.tif

# JHove reports image without SamplesPerPixel as invalid
# cp $SOURCE valid/no_samples_per_pixel.tif
# exiftool -overwrite_original -IFD0:SamplesPerPixel= valid/no_samples_per_pixel.tif

cp $SOURCE valid/no_planar_configuration.tif
exiftool -overwrite_original -IFD0:PlanarConfiguration= valid/no_planar_configuration.tif

cp $SOURCE valid/white_balance_manual.tif
exiftool -overwrite_original -IFD0:WhiteBalance="Manual" valid/white_balance_manual.tif

cp $SOURCE valid/xmp_serial_number.tif
exiftool -overwrite_original -EXIF:SerialNumber= -XMP:SerialNumber="1234567890" valid/xmp_serial_number.tif


echo "create invalid variants"

cp $SOURCE invalid/no_make.tif
exiftool -overwrite_original -IFD0:Make= invalid/no_make.tif

cp $SOURCE invalid/make_is_single_space.tif
exiftool -overwrite_original -IFD0:Make=" " invalid/make_is_single_space.tif

cp $SOURCE invalid/no_model.tif
exiftool -overwrite_original -IFD0:Model= invalid/no_model.tif

cp $SOURCE invalid/model_is_single_space.tif
exiftool -overwrite_original -IFD0:Model=" " invalid/model_is_single_space.tif

cp $SOURCE invalid/no_software.tif
exiftool -overwrite_original -IFD0:Software= invalid/no_software.tif

cp $SOURCE invalid/software_is_single_space.tif
exiftool -overwrite_original -IFD0:Software=" " invalid/software_is_single_space.tif

cp $SOURCE invalid/no_artist.tif
exiftool -overwrite_original -IFD0:Artist= invalid/no_artist.tif

cp $SOURCE invalid/artist_is_single_space.tif
exiftool -overwrite_original -IFD0:Artist=" " invalid/artist_is_single_space.tif

cp $SOURCE invalid/no_serial_number.tif
exiftool -overwrite_original -EXIF:SerialNumber= invalid/no_serial_number.tif

cp $SOURCE invalid/subfiletype_reduced_resolution_image.tif
exiftool -overwrite_original -IFD0:SubfileType="Reduced-resolution image" invalid/subfiletype_reduced_resolution_image.tif

cp $SOURCE invalid/fillorder_reversed.tif
exiftool -overwrite_original -IFD0:FillOrder="Reversed" invalid/fillorder_reversed.tif

cp $SOURCE invalid/image_description_single_space.tif
exiftool -overwrite_original -IFD0:ImageDescription=" " invalid/image_description_single_space.tif

cp $SOURCE invalid/orientation_mirror.tif
exiftool -overwrite_original -IFD0:Orientation="Mirror horizontal " invalid/orientation_mirror.tif

cp $SOURCE invalid/planar_configuration_planar.tif
exiftool -overwrite_original -IFD0:PlanarConfiguration="Planar" invalid/planar_configuration_planar.tif

# Exiftool only accepts valid dates
# cp $SOURCE invalid/modify_date_wrong_format.tif
# exiftool -overwrite_original -IFD0:ModifyDate="2026-03-22 15:11:22.50" invalid/modify_date_wrong_format.tif

cp $SOURCE invalid/copyright.tif
exiftool -overwrite_original -IFD0:Copyright="my copyright" invalid/copyright.tif

cp $SOURCE invalid/xmp_rights.tif
exiftool -overwrite_original -XMP:Rights="my copyright" invalid/xmp_rights.tif

cp $SOURCE invalid/old_subfiletype_defined.tif
exiftool -overwrite_original -IFD0:OldSubfileType="Full-resolution image" invalid/old_subfiletype_defined.tif

cp $SOURCE invalid/thresholding.tif
exiftool -overwrite_original -IFD0:Thresholding="No dithering or halftoning" invalid/thresholding.tif

cp $SOURCE invalid/cell_width.tif
exiftool -overwrite_original -IFD0:CellWidth="2" invalid/cell_width.tif

cp $SOURCE invalid/cell_length.tif
exiftool -overwrite_original -IFD0:CellLength="2" invalid/cell_length.tif

cp $SOURCE invalid/host_computer.tif
exiftool -overwrite_original -IFD0:HostComputer="my host computer" invalid/host_computer.tif

cp $SOURCE invalid/no_icc_profile.tif
exiftool -overwrite_original -icc_profile:all= invalid/no_icc_profile.tif

tiffcp base.tif base.tif invalid/multipage.tif

magick base.tif -compress LZW invalid/compressed.tif

# dnf install icc-profiles-openicc
# magick base.tif -profile /usr/share/color/icc/lcms/Lab.icc invalid/color_space_cielab.tif

magick base.tif -depth 24 invalid/bit_depth_24.tif
magick base.tif -density 100 invalid/resolution_100.tif
magick base.tif -density 100 invalid/resolution_100.tif
magick base.tif -units PixelsPerCentimeter -density 300 invalid/resolution_unit_centimeter.tif

