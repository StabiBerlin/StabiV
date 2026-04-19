#!/bin/bash

# set metadata values according to sbk-berlin specification
# see https://exiftool -overwrite_original.org/TagNames/EXIF.html

rm -rf image_rules
rm -rf consistency
rm -rf size_deviations
rm -rf consecutive_numbering
mkdir image_rules
mkdir consistency
mkdir size_deviations
mkdir consecutive_numbering

RGB_SMALL_SOURCE=base_rgb_small.tif
RGB_MEDIUM_SOURCE=base_rgb_medium.tif
RGB_LARGE_SOURCE=base_rgb_large.tif
GRAYSCALE_SOURCE=base_grayscale.tif

# --------------------------------------------------------------------
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

set_base_tags $RGB_SMALL_SOURCE
exiftool -overwrite_original -IFD0:SamplesPerPixel=3 $RGB_SMALL_SOURCE

set_base_tags $RGB_MEDIUM_SOURCE
exiftool -overwrite_original -IFD0:SamplesPerPixel=3 $RGB_MEDIUM_SOURCE

set_base_tags $RGB_LARGE_SOURCE
exiftool -overwrite_original -IFD0:SamplesPerPixel=3 $RGB_LARGE_SOURCE

set_base_tags $GRAYSCALE_SOURCE
exiftool -overwrite_original -IFD0:SamplesPerPixel=1 $GRAYSCALE_SOURCE

# --------------------------------------------------------------------
echo "create valid alternatives based on image rules"
mkdir -p image_rules/valid
cp $RGB_SMALL_SOURCE image_rules/valid/rgb_base.tif

cp $RGB_SMALL_SOURCE image_rules/valid/no_subfiletype.tif
exiftool -overwrite_original -IFD0:SubfileType= image_rules/valid/no_subfiletype.tif

cp $RGB_SMALL_SOURCE image_rules/valid/no_fill_order.tif
exiftool -overwrite_original -IFD0:FillOrder= image_rules/valid/no_fill_order.tif

cp $RGB_SMALL_SOURCE image_rules/valid/no_image_description.tif
exiftool -overwrite_original -IFD0:ImageDescription= image_rules/valid/no_image_description.tif

cp $RGB_SMALL_SOURCE image_rules/valid/no_orientation.tif
exiftool -overwrite_original -IFD0:Orientation= image_rules/valid/no_orientation.tif

# JHove reports image without SamplesPerPixel as invalid
# cp $RGB_SMALL_SOURCE image_rules/valid/no_samples_per_pixel.tif
# exiftool -overwrite_original -IFD0:SamplesPerPixel= image_rules/valid/no_samples_per_pixel.tif

cp $RGB_SMALL_SOURCE image_rules/valid/no_planar_configuration.tif
exiftool -overwrite_original -IFD0:PlanarConfiguration= image_rules/valid/no_planar_configuration.tif

cp $RGB_SMALL_SOURCE image_rules/valid/white_balance_manual.tif
exiftool -overwrite_original -IFD0:WhiteBalance="Manual" image_rules/valid/white_balance_manual.tif

cp $RGB_SMALL_SOURCE image_rules/valid/xmp_serial_number.tif
exiftool -overwrite_original -EXIF:SerialNumber= -XMP:SerialNumber="1234567890" image_rules/valid/xmp_serial_number.tif

# --------------------------------------------------------------------
echo "create invalid variants based on image rules"
mkdir -p image_rules/invalid
cp $RGB_SMALL_SOURCE image_rules/invalid/no_make.tif
exiftool -overwrite_original -IFD0:Make= image_rules/invalid/no_make.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/make_is_single_space.tif
exiftool -overwrite_original -IFD0:Make=" " image_rules/invalid/make_is_single_space.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/no_model.tif
exiftool -overwrite_original -IFD0:Model= image_rules/invalid/no_model.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/model_is_single_space.tif
exiftool -overwrite_original -IFD0:Model=" " image_rules/invalid/model_is_single_space.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/no_software.tif
exiftool -overwrite_original -IFD0:Software= image_rules/invalid/no_software.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/software_is_single_space.tif
exiftool -overwrite_original -IFD0:Software=" " image_rules/invalid/software_is_single_space.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/no_artist.tif
exiftool -overwrite_original -IFD0:Artist= image_rules/invalid/no_artist.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/artist_is_single_space.tif
exiftool -overwrite_original -IFD0:Artist=" " image_rules/invalid/artist_is_single_space.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/no_serial_number.tif
exiftool -overwrite_original -EXIF:SerialNumber= image_rules/invalid/no_serial_number.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/subfiletype_reduced_resolution_image.tif
exiftool -overwrite_original -IFD0:SubfileType="Reduced-resolution image" image_rules/invalid/subfiletype_reduced_resolution_image.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/fillorder_reversed.tif
exiftool -overwrite_original -IFD0:FillOrder="Reversed" image_rules/invalid/fillorder_reversed.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/image_description_single_space.tif
exiftool -overwrite_original -IFD0:ImageDescription=" " image_rules/invalid/image_description_single_space.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/orientation_mirror.tif
exiftool -overwrite_original -IFD0:Orientation="Mirror horizontal " image_rules/invalid/orientation_mirror.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/planar_configuration_planar.tif
exiftool -overwrite_original -IFD0:PlanarConfiguration="Planar" image_rules/invalid/planar_configuration_planar.tif

# Exiftool only accepts valid dates
# cp $RGB_SMALL_SOURCE image_rules/invalid/modify_date_wrong_format.tif
# exiftool -overwrite_original -IFD0:ModifyDate="2026-03-22 15:11:22.50" image_rules/invalid/modify_date_wrong_format.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/copyright.tif
exiftool -overwrite_original -IFD0:Copyright="my copyright" image_rules/invalid/copyright.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/xmp_rights.tif
exiftool -overwrite_original -XMP:Rights="my copyright" image_rules/invalid/xmp_rights.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/old_subfiletype_defined.tif
exiftool -overwrite_original -IFD0:OldSubfileType="Full-resolution image" image_rules/invalid/old_subfiletype_defined.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/thresholding.tif
exiftool -overwrite_original -IFD0:Thresholding="No dithering or halftoning" image_rules/invalid/thresholding.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/cell_width.tif
exiftool -overwrite_original -IFD0:CellWidth="2" image_rules/invalid/cell_width.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/cell_length.tif
exiftool -overwrite_original -IFD0:CellLength="2" image_rules/invalid/cell_length.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/host_computer.tif
exiftool -overwrite_original -IFD0:HostComputer="my host computer" image_rules/invalid/host_computer.tif

cp $RGB_SMALL_SOURCE image_rules/invalid/no_icc_profile.tif
exiftool -overwrite_original -icc_profile:all= image_rules/invalid/no_icc_profile.tif

tiffcp $RGB_SMALL_SOURCE $RGB_SMALL_SOURCE image_rules/invalid/multipage.tif

magick $RGB_SMALL_SOURCE -compress LZW -type TrueColor image_rules/invalid/compressed.tif

# dnf install icc-profiles-openicc
# magick base.tif -profile /usr/share/color/icc/lcms/Lab.icc image_rules/invalid/color_space_cielab.tif

magick $RGB_SMALL_SOURCE -depth 24 -type TrueColor image_rules/invalid/bit_depth_24.tif
magick $RGB_SMALL_SOURCE -density 100 -type TrueColor image_rules/invalid/resolution_100.tif
magick $RGB_SMALL_SOURCE -density 300x400 -type TrueColor image_rules/invalid/resolution_xy_different.tif
magick $RGB_SMALL_SOURCE -units PixelsPerCentimeter -density 300 -type TrueColor image_rules/invalid/resolution_unit_centimeter.tif


# --------------------------------------------------------------------
echo "create inconsistent examples"
mkdir -p consistency/invalid
cp $RGB_SMALL_SOURCE consistency/invalid/rgb.tif
cp $GRAYSCALE_SOURCE consistency/invalid/grayscale.tif

# --------------------------------------------------------------------
echo "create consecutive numbering examples"
mkdir -p consecutive_numbering/valid
cp $RGB_SMALL_SOURCE consecutive_numbering/valid/00000001.tif
cp $RGB_SMALL_SOURCE consecutive_numbering/valid/00000002.tif
cp $RGB_SMALL_SOURCE consecutive_numbering/valid/00000003.tif

mkdir -p consecutive_numbering/invalid
cp $RGB_SMALL_SOURCE consecutive_numbering/invalid/00000001.tif
cp $RGB_SMALL_SOURCE consecutive_numbering/invalid/00000003.tif
cp $RGB_SMALL_SOURCE consecutive_numbering/invalid/00000004.tif

# --------------------------------------------------------------------
echo "create size deviations example examples"

mkdir -p size_deviations/invalid_first_slice
cp $RGB_MEDIUM_SOURCE size_deviations/invalid_first_slice/00000001.tif
cp $RGB_MEDIUM_SOURCE size_deviations/invalid_first_slice/00000002.tif
cp $RGB_LARGE_SOURCE size_deviations/invalid_first_slice/00000003.tif
cp $RGB_SMALL_SOURCE size_deviations/invalid_first_slice/00000004.tif
cp $RGB_SMALL_SOURCE size_deviations/invalid_first_slice/00000005.tif
cp $RGB_SMALL_SOURCE size_deviations/invalid_first_slice/00000006.tif
cp $RGB_MEDIUM_SOURCE size_deviations/invalid_first_slice/00000007.tif
cp $RGB_MEDIUM_SOURCE size_deviations/invalid_first_slice/00000008.tif
cp $RGB_MEDIUM_SOURCE size_deviations/invalid_first_slice/00000009.tif

mkdir -p size_deviations/invalid_last_slice
cp $RGB_MEDIUM_SOURCE size_deviations/invalid_last_slice/00000001.tif
cp $RGB_MEDIUM_SOURCE size_deviations/invalid_last_slice/00000002.tif
cp $RGB_MEDIUM_SOURCE size_deviations/invalid_last_slice/00000003.tif
cp $RGB_SMALL_SOURCE size_deviations/invalid_last_slice/00000004.tif
cp $RGB_SMALL_SOURCE size_deviations/invalid_last_slice/00000005.tif
cp $RGB_SMALL_SOURCE size_deviations/invalid_last_slice/00000006.tif
cp $RGB_MEDIUM_SOURCE size_deviations/invalid_last_slice/00000007.tif
cp $RGB_LARGE_SOURCE size_deviations/invalid_last_slice/00000008.tif
cp $RGB_MEDIUM_SOURCE size_deviations/invalid_last_slice/00000009.tif

mkdir -p size_deviations/invalid_center_slice
cp $RGB_MEDIUM_SOURCE size_deviations/invalid_center_slice/00000001.tif
cp $RGB_MEDIUM_SOURCE size_deviations/invalid_center_slice/00000002.tif
cp $RGB_MEDIUM_SOURCE size_deviations/invalid_center_slice/00000003.tif
cp $RGB_SMALL_SOURCE size_deviations/invalid_center_slice/00000004.tif
cp $RGB_LARGE_SOURCE size_deviations/invalid_center_slice/00000005.tif
cp $RGB_SMALL_SOURCE size_deviations/invalid_center_slice/00000006.tif
cp $RGB_MEDIUM_SOURCE size_deviations/invalid_center_slice/00000007.tif
cp $RGB_MEDIUM_SOURCE size_deviations/invalid_center_slice/00000008.tif
cp $RGB_MEDIUM_SOURCE size_deviations/invalid_center_slice/00000009.tif

mkdir -p size_deviations/valid_deviations
cp $RGB_MEDIUM_SOURCE size_deviations/valid/00000001.tif
cp $RGB_SMALL_SOURCE size_deviations/valid/00000002.tif
cp $RGB_MEDIUM_SOURCE size_deviations/valid/00000003.tif
cp $RGB_SMALL_SOURCE size_deviations/valid/00000004.tif
cp $RGB_MEDIUM_SOURCE size_deviations/valid/00000005.tif
cp $RGB_SMALL_SOURCE size_deviations/valid/00000006.tif
cp $RGB_MEDIUM_SOURCE size_deviations/valid/00000007.tif
cp $RGB_MEDIUM_SOURCE size_deviations/valid/00000008.tif
cp $RGB_SMALL_SOURCE size_deviations/valid/00000009.tif