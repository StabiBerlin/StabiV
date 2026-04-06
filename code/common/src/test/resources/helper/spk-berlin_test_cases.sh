#!/bin/bash

# set metadata values according to sbk-berlin specification
# see https://exiftool -overwrite_original.org/TagNames/EXIF.html

rm -rf valid
rm -rf invalid
rm -rf inconsistent
mkdir valid
mkdir invalid
mkdir inconsistent

RGB_SOURCE=base_rgb.tif
GRAYSCALE_SOURCE=base_grayscale.tif

echo "update base files to be valid"

set_base_tags() {
    echo "update base file $1"
    exiftool -overwrite_original -IFD0:Make="scanner manufacturer" $1
    exiftool -overwrite_original -IFD0:Model="scanner model" $1
    exiftool -overwrite_original -IFD0:Software="scanner software" $1
    exiftool -overwrite_original -IFD0:Artist="image artist" $1
    exiftool -overwrite_original -EXIF:SerialNumber="1234567890" $1
    exiftool -overwrite_original -IFD0:SubfileType="Full-resolution image" $1
    exiftool -overwrite_original -IFD0:FillOrder="Normal" $1
    exiftool -overwrite_original -IFD0:ImageDescription="image description" $1
    exiftool -overwrite_original -IFD0:Orientation="Horizontal (normal)" $1
    exiftool -overwrite_original -IFD0:PlanarConfiguration="Chunky" $1
    exiftool -overwrite_original -IFD0:ModifyDate="2026:03:22 15:11:22" $1
    exiftool -overwrite_original -IFD0:WhiteBalance="Auto" $1
}

set_base_tags $RGB_SOURCE
exiftool -overwrite_original -IFD0:SamplesPerPixel=3 $RGB_SOURCE

set_base_tags $GRAYSCALE_SOURCE
exiftool -overwrite_original -IFD0:SamplesPerPixel=1 $GRAYSCALE_SOURCE


echo "show metadata"
exiftool -overwrite_original -a -u -s -G5 $RGB_SOURCE

cp $RGB_SOURCE valid/rgb_base.tif

echo "create valid alternatives"

cp $RGB_SOURCE valid/no_subfiletype.tif
exiftool -overwrite_original -IFD0:SubfileType= valid/no_subfiletype.tif

cp $RGB_SOURCE valid/no_fill_order.tif
exiftool -overwrite_original -IFD0:FillOrder= valid/no_fill_order.tif

cp $RGB_SOURCE valid/no_image_description.tif
exiftool -overwrite_original -IFD0:ImageDescription= valid/no_image_description.tif

cp $RGB_SOURCE valid/no_orientation.tif
exiftool -overwrite_original -IFD0:Orientation= valid/no_orientation.tif

# JHove reports image without SamplesPerPixel as invalid
# cp $RGB_SOURCE valid/no_samples_per_pixel.tif
# exiftool -overwrite_original -IFD0:SamplesPerPixel= valid/no_samples_per_pixel.tif

cp $RGB_SOURCE valid/no_planar_configuration.tif
exiftool -overwrite_original -IFD0:PlanarConfiguration= valid/no_planar_configuration.tif

cp $RGB_SOURCE valid/white_balance_manual.tif
exiftool -overwrite_original -IFD0:WhiteBalance="Manual" valid/white_balance_manual.tif

cp $RGB_SOURCE valid/xmp_serial_number.tif
exiftool -overwrite_original -EXIF:SerialNumber= -XMP:SerialNumber="1234567890" valid/xmp_serial_number.tif


echo "create invalid variants"

cp $RGB_SOURCE invalid/no_make.tif
exiftool -overwrite_original -IFD0:Make= invalid/no_make.tif

cp $RGB_SOURCE invalid/make_is_single_space.tif
exiftool -overwrite_original -IFD0:Make=" " invalid/make_is_single_space.tif

cp $RGB_SOURCE invalid/no_model.tif
exiftool -overwrite_original -IFD0:Model= invalid/no_model.tif

cp $RGB_SOURCE invalid/model_is_single_space.tif
exiftool -overwrite_original -IFD0:Model=" " invalid/model_is_single_space.tif

cp $RGB_SOURCE invalid/no_software.tif
exiftool -overwrite_original -IFD0:Software= invalid/no_software.tif

cp $RGB_SOURCE invalid/software_is_single_space.tif
exiftool -overwrite_original -IFD0:Software=" " invalid/software_is_single_space.tif

cp $RGB_SOURCE invalid/no_artist.tif
exiftool -overwrite_original -IFD0:Artist= invalid/no_artist.tif

cp $RGB_SOURCE invalid/artist_is_single_space.tif
exiftool -overwrite_original -IFD0:Artist=" " invalid/artist_is_single_space.tif

cp $RGB_SOURCE invalid/no_serial_number.tif
exiftool -overwrite_original -EXIF:SerialNumber= invalid/no_serial_number.tif

cp $RGB_SOURCE invalid/subfiletype_reduced_resolution_image.tif
exiftool -overwrite_original -IFD0:SubfileType="Reduced-resolution image" invalid/subfiletype_reduced_resolution_image.tif

cp $RGB_SOURCE invalid/fillorder_reversed.tif
exiftool -overwrite_original -IFD0:FillOrder="Reversed" invalid/fillorder_reversed.tif

cp $RGB_SOURCE invalid/image_description_single_space.tif
exiftool -overwrite_original -IFD0:ImageDescription=" " invalid/image_description_single_space.tif

cp $RGB_SOURCE invalid/orientation_mirror.tif
exiftool -overwrite_original -IFD0:Orientation="Mirror horizontal " invalid/orientation_mirror.tif

cp $RGB_SOURCE invalid/planar_configuration_planar.tif
exiftool -overwrite_original -IFD0:PlanarConfiguration="Planar" invalid/planar_configuration_planar.tif

# Exiftool only accepts valid dates
# cp $RGB_SOURCE invalid/modify_date_wrong_format.tif
# exiftool -overwrite_original -IFD0:ModifyDate="2026-03-22 15:11:22.50" invalid/modify_date_wrong_format.tif

cp $RGB_SOURCE invalid/copyright.tif
exiftool -overwrite_original -IFD0:Copyright="my copyright" invalid/copyright.tif

cp $RGB_SOURCE invalid/xmp_rights.tif
exiftool -overwrite_original -XMP:Rights="my copyright" invalid/xmp_rights.tif

cp $RGB_SOURCE invalid/old_subfiletype_defined.tif
exiftool -overwrite_original -IFD0:OldSubfileType="Full-resolution image" invalid/old_subfiletype_defined.tif

cp $RGB_SOURCE invalid/thresholding.tif
exiftool -overwrite_original -IFD0:Thresholding="No dithering or halftoning" invalid/thresholding.tif

cp $RGB_SOURCE invalid/cell_width.tif
exiftool -overwrite_original -IFD0:CellWidth="2" invalid/cell_width.tif

cp $RGB_SOURCE invalid/cell_length.tif
exiftool -overwrite_original -IFD0:CellLength="2" invalid/cell_length.tif

cp $RGB_SOURCE invalid/host_computer.tif
exiftool -overwrite_original -IFD0:HostComputer="my host computer" invalid/host_computer.tif

cp $RGB_SOURCE invalid/no_icc_profile.tif
exiftool -overwrite_original -icc_profile:all= invalid/no_icc_profile.tif

tiffcp $RGB_SOURCE $RGB_SOURCE invalid/multipage.tif

magick $RGB_SOURCE -compress LZW -type TrueColor invalid/compressed.tif

# dnf install icc-profiles-openicc
# magick base.tif -profile /usr/share/color/icc/lcms/Lab.icc invalid/color_space_cielab.tif

magick $RGB_SOURCE -depth 24 -type TrueColor invalid/bit_depth_24.tif
magick $RGB_SOURCE -density 100 -type TrueColor invalid/resolution_100.tif
magick $RGB_SOURCE -units PixelsPerCentimeter -density 300 -type TrueColor invalid/resolution_unit_centimeter.tif

echo "create inconsistent examples"

cp $RGB_SOURCE inconsistent/rgb.tif
cp $GRAYSCALE_SOURCE inconsistent/grayscale.tif