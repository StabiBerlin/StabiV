# Existierende Software zur Extraktion von Bildmetadaten

## [JHOVE](https://jhove.openpreservation.org/)

Java Bibliothek und CLI

Beispiel: `jhove 00000001.tif`

```
Jhove (Rel. 1.34.0, 2025-07-02)
 Date: 2026-01-27 11:58:34 CET
 RepresentationInformation: ../../work/knopflogik/projects/spk-berlin/documents/2026-01-13_Beispieldaten/Beispieldateien/fehlerhafte Dateien/00000001.tif
  ReportingModule: TIFF-hul, Rel. 1.9.5 (2024-08-22)
  LastModified: 2021-07-07 11:41:54 CEST
  Size: 64448774
  Format: TIFF
  Version: 6.0
  Status: Well-Formed and valid
  SignatureMatches:
   TIFF-hul
  InfoMessage: Unknown TIFF IFD tag: 36868
   ID: TIFF-HUL-12
   InfoLink: https://github.com/openpreserve/jhove/wiki/TIFF-hul-Messages#tiff-hul-12
   Offset: 64448754
  MIMEtype: image/tiff
  Profile: Baseline RGB (Class R), DLF Benchmark for Faithful Digital Reproductions of Monographs and Serials: color
  TIFFMetadata:
   ByteOrder: little-endian
   IFDs:
    Number: 3
    IFD:
     Offset: 64399260
     Type: TIFF
     Entries:
      NisoImageMetadata:
       FormatName: image/tiff
       ByteOrder: little_endian
       CompressionScheme: uncompressed
       ImageWidth: 3861
       ImageHeight: 5557
       ColorSpace: RGB
       ICCProfileName: eciRGB v2
       ReferenceBlackWhite: 0, 255, 0, 255, 0, 255
       DateTimeCreated: 2021-07-07T13:41:16
       ImageProducer: Staatsbibliothek zu Berlin
       ScannerManufacturer: i2S Corp.
       ScannerModelName: SupraScan Quartz A1 [SN: 365002] - CamQuartzHD [SN: 365002]
       ScanningSoftware: Capture v3.4.0
       Orientation: normal
       SamplingFrequencyUnit: inch
       XSamplingFrequency: 600
       YSamplingFrequency: 600
       BitsPerSample: 8, 8, 8
       BitsPerSampleUnit: integer
       SamplesPerPixel: 3
      ImageDescription:
      DateTime: 2021:07:07 13:41:16
      NewSubfileType: 0
      Copyright:
      SampleFormat: 1, 1, 1
      MinSampleValue: 0, 0, 0
      MaxSampleValue: 255, 255, 255
      TransferRange: 0, 255, 0, 255, 0, 255
      Threshholding: 1
      StripOffsets: 8, 11591, 23174, 34757, 46340, 57923, [...........]
      RowsPerStrip: 1
      StripByteCounts: 11583, 11583, 11583, 11583, 11583, [...........]
      PlanarConfiguration: 1
      TIFFITProperties:
       BackgroundColorIndicator: background not defined
       ImageColorIndicator: image not defined
       TransparencyIndicator: no transparency
       PixelIntensityRange: 0, 255
       RasterPadding: 1 byte
       BitsPerRunLength: 8
       BitsPerExtendedRunLength: 16
      TIFFEPProperties:
       ICCProfile: true
    IFD:
     Offset: 64444214
     Type: TIFF
     Entries:
      NisoImageMetadata:
       FormatName: image/tiff
       ByteOrder: little_endian
       CompressionScheme: LZW
       ImageWidth: 83
       ImageHeight: 120
       ColorSpace: RGB
       ReferenceBlackWhite: 0, 255, 0, 255, 0, 255
       Orientation: normal
       SamplingFrequencyUnit: inch
       XSamplingFrequency: 96
       YSamplingFrequency: 96
       BitsPerSample: 8, 8, 8
       BitsPerSampleUnit: integer
       SamplesPerPixel: 3
      NewSubfileType: 0
      SampleFormat: 1, 1, 1
      MinSampleValue: 0, 0, 0
      MaxSampleValue: 255, 255, 255
      TransferRange: 0, 255, 0, 255, 0, 255
      Threshholding: 1
      Predictor: horizontal differencing
      StripOffsets: 64366747, 64379858, 64393297
      RowsPerStrip: 49
      StripByteCounts: 13111, 13439, 5729
      PlanarConfiguration: 1
      TIFFITProperties:
       BackgroundColorIndicator: background not defined
       ImageColorIndicator: image not defined
       TransparencyIndicator: no transparency
       PixelIntensityRange: 0, 255
       RasterPadding: 1 byte
       BitsPerRunLength: 8
       BitsPerExtendedRunLength: 16
    IFD:
     Offset: 64444446
     Type: Exif
     Entries:
      FlashpixVersion: 0100
      UserComment:
       IDCode: UNICODE
       Value: UNICODE
       RawValue: 85, 78, 73, 67, 79, 68, 69, 0
      FocalPlaneResolutionUnit: inches
      Contrast: normal
```

## [LibTiff](https://libtiff.gitlab.io/libtiff/)

C Bibliothek und verschiedene CLI Programme

Beispiel: `tiffdump 00000001.tif`

```
00000001.tif:
Magic: 0x4949 <little-endian> Version: 0x2a <ClassicTIFF>
Directory 0: offset 64399260 (0x3d6a79c) next 0 (0)
SubFileType (254) LONG (4) 1<0>
ImageWidth (256) LONG (4) 1<3861>
ImageLength (257) LONG (4) 1<5557>
BitsPerSample (258) SHORT (3) 3<8 8 8>
Compression (259) SHORT (3) 1<1>
Photometric (262) SHORT (3) 1<2>
ImageDescription (270) ASCII (2) 1<\0>
Make (271) ASCII (2) 10<i2S Corp.\0>
Model (272) ASCII (2) 60<SupraScan Quartz A1 [SN: ...>
StripOffsets (273) LONG (4) 5557<8 11591 23174 34757 46340 57923 69506 81089 92672 104255 115838 127421 139004 150587 162170 173753 185336 196919 208502 220085 231668 243251 254834 266417 ...>
Orientation (274) SHORT (3) 1<1>
SamplesPerPixel (277) SHORT (3) 1<3>
RowsPerStrip (278) LONG (4) 1<1>
StripByteCounts (279) LONG (4) 5557<11583 11583 11583 11583 11583 11583 11583 11583 11583 11583 11583 11583 11583 11583 11583 11583 11583 11583 11583 11583 11583 11583 11583 11583 ...>
XResolution (282) RATIONAL (5) 1<600>
YResolution (283) RATIONAL (5) 1<600>
PlanarConfig (284) SHORT (3) 1<1>
ResolutionUnit (296) SHORT (3) 1<2>
Software (305) ASCII (2) 15<Capture v3.4.0\0>
DateTime (306) ASCII (2) 20<2021:07:07 13:41:16\0>
Artist (315) ASCII (2) 27<Staatsbibliothek zu Berl ...>
HostComputer (316) ASCII (2) 1<\0>
SubIFD (330) LONG (4) 1<64444214>
Copyright (33432) ASCII (2) 1<\0>
34665 (0x8769) LONG (4) 1<64444446>
ICC Profile (34675) UNDEFINED (7) 1992<00 00 0x7 0xc8 0x41 0x44 0x42 0x45 0x2 0x40 00 00 0x6d 0x6e 0x74 0x72 0x52 0x47 0x42 0x20 0x58 0x59 0x5a 0x20 ...>
36867 (0x9003) ASCII (2) 20<2021:07:07 13:41:16\0>
36868 (0x9004) ASCII (2) 20<2021:07:07 13:41:16\0>
```

Beispiel: `tiffinfo 00000001.tif`

```
=== TIFF directory 0 ===
TIFF Directory at offset 0x3d6a79c (64399260)
  Subfile Type: (0 = 0x0)
  Image Width: 3861 Image Length: 5557
  Resolution: 600, 600 pixels/inch
  Bits/Sample: 8
  Compression Scheme: None
  Photometric Interpretation: RGB color
  Orientation: row 0 top, col 0 lhs
  Samples/Pixel: 3
  Rows/Strip: 1
  Planar Configuration: single image plane
  SubIFD Offsets: 64444214
  ImageDescription:
  Make: i2S Corp.
  Model: SupraScan Quartz A1 [SN: 365002] - CamQuartzHD [SN: 365002]
  Software: Capture v3.4.0
  DateTime: 2021:07:07 13:41:16
  Artist: Staatsbibliothek zu Berlin
  HostComputer:
  Copyright:
  EXIFIFDOffset: 0x3d7581e
  ICC Profile: <present>, 1992 bytes
  Tag 36867: 2021:07:07 13:41:16
  Tag 36868: 2021:07:07 13:41:16
--- EXIF directory within directory 0
TIFF Directory at offset 0x3d7581e (64444446)
  Compression Scheme: None
  Tag 269:
  Tag 285:
  Tag 286: 0.000000
  Tag 287: 0.000000
  Tag 297: 0,0
  Tag 34675: 0,0,7,200,65,68,66,69,2,64 [........]
  Tag 37393: 0
  UserComment: 0x55,0x4e,0x49,0x43,0x4f,0x44,0x45,0x0
  Tag 40091:
  Tag 40092:
  Tag 40093: Staatsbibliothek zu Berlin
  Tag 40094:
  Tag 40095:
  Tag 50708: CamQuartzHD
  Tag 50709: CamQuartzHD
  Tag 50735: 365002
  Tag 65001: 365002
--- SubIFD image descriptor tag within TIFF directory 0 with array of 1 SubIFD chains ---
--- SubIFD 0 of chain 0 at offset 0x3d75736 (64444214):
TIFF Directory at offset 0x3d75736 (64444214)
  Subfile Type: (0 = 0x0)
  Image Width: 83 Image Length: 120
  Resolution: 96, 96 pixels/inch
  Bits/Sample: 8
  Compression Scheme: LZW
  Photometric Interpretation: RGB color
  Samples/Pixel: 3
  Rows/Strip: 49
  Planar Configuration: single image plane
  Predictor: horizontal differencing 2 (0x2)

```

## [ImageMagick](https://imagemagick.org/)

Allgemeines

- C Bibliothek
- [Quelltext](https://github.com/ImageMagick/ImageMagick)

Bindings für

- Java mit [JMagick](http://www.jmagick.org/)
- uvm., siehe [Dokumentation](https://imagemagick.org/script/develop.php#gsc.tab=0)

Beispiel: `magick identify -verbose 0000001.tif`

```
Image:
  Filename: 00000001.tif
  Permissions: rw-r--r--
  Format: TIFF (Tagged Image File Format)
  Mime type: image/tiff
  Class: DirectClass
  Geometry: 3861x5557+0+0
  Resolution: 600x600
  Print size: 6.435x9.26167
  Units: PixelsPerInch
  Colorspace: sRGB
  Type: TrueColor
  Endianness: LSB
  Depth: 8-bit
  Channels: 3.0
  Channel depth:
    Red: 8-bit
    Green: 8-bit
    Blue: 8-bit
  Channel statistics:
    Pixels: 21455577
    Red:
      min: 2  (0.00784314)
      max: 255 (1)
      mean: 80.7505 (0.316669)
      median: 78 (0.305882)
      standard deviation: 33.2259 (0.130298)
      kurtosis: -0.261024
      skewness: 0.436635
      entropy: 0.869412
    Green:
      min: 2  (0.00784314)
      max: 253 (0.992157)
      mean: 64.1931 (0.251738)
      median: 62 (0.243137)
      standard deviation: 27.06 (0.106118)
      kurtosis: 1.03041
      skewness: 0.820362
      entropy: 0.831351
    Blue:
      min: 1  (0.00392157)
      max: 195 (0.764706)
      mean: 51.2445 (0.200959)
      median: 45 (0.176471)
      standard deviation: 19.0087 (0.0745438)
      kurtosis: 1.77721
      skewness: 1.35025
      entropy: 0.794249
  Image statistics:
    Overall:
      min: 1  (0.00392157)
      max: 255 (1)
      mean: 65.396 (0.256455)
      median: 61.6667 (0.24183)
      standard deviation: 26.4315 (0.103653)
      kurtosis: 0.848866
      skewness: 0.869081
      entropy: 0.831671
  Rendering intent: Perceptual
  Gamma: 0.454545
  Chromaticity:
    red primary: (0.64,0.33,0.03)
    green primary: (0.3,0.6,0.1)
    blue primary: (0.15,0.06,0.79)
    white point: (0.3127,0.329,0.3583)
  Matte color: grey74
  Background color: white
  Border color: srgb(223,223,223)
  Transparent color: black
  Interlace: None
  Intensity: Undefined
  Compose: Over
  Page geometry: 3861x5557+0+0
  Dispose: Undefined
  Iterations: 0
  Compression: None
  Orientation: TopLeft
  Profiles:
    Profile-icc: 1992 bytes
  Properties:
    comment:
    date:create: 2026-01-13T09:05:24+00:00
    date:modify: 2021-07-07T09:41:54+00:00
    date:timestamp: 2026-01-27T11:01:25+00:00
    exif:Compression Scheme: None
    exif:Tag 269:
    exif:Tag 285:
    exif:Tag 286: 0.000000
    exif:Tag 287: 0.000000
    exif:Tag 297: 0,0
    exif:Tag 34675: 0,0,7,200,65,68,66,69,2,64,0,0,109,110,116,[..........]
    exif:Tag 37393: 0
    exif:Tag 40091:
    exif:Tag 40092:
    exif:Tag 40093: Staatsbibliothek zu Berlin
    exif:Tag 40094:
    exif:Tag 40095:
    exif:Tag 50708: CamQuartzHD
    exif:Tag 50709: CamQuartzHD
    exif:Tag 50735: 365002
    exif:Tag 65001: 365002
    exif:UserComment: 0x55,0x4e,0x49,0x43,0x4f,0x44,0x45,0x0
    icc:copyright: Copyright (C) 2007 by Color Solutions, All Rights Reserved. License details can be found on: http://www.eci.org/eci/en/eciRGB.php
    icc:description: eciRGB v2
    signature: 007170fcff3de2645753a3b59a7bf6ceb90706e533722b7e7f7acc2af7c9fda2
    tiff:artist: Staatsbibliothek zu Berlin
    tiff:copyright:
    tiff:endian: lsb
    tiff:hostcomputer:
    tiff:kodak-36867: 2021:07:07 13:41:16
    tiff:make: i2S Corp.
    tiff:model: SupraScan Quartz A1 [SN: 365002] - CamQuartzHD [SN: 365002]
    tiff:photometric: RGB
    tiff:rows-per-strip: 1
    tiff:software: Capture v3.4.0
    tiff:timestamp: 2021:07:07 13:41:16
  Artifacts:
    verbose: true
  Tainted: False
  Filesize: 61.4631MiB
  Number pixels: 21.4556M
  Pixel cache type: Memory
  Pixels per second: 172.731MP
  User time: 0.110u
  Elapsed time: 0:01.124
  Version: ImageMagick 7.1.1-47 Q16-HDRI x86_64 22763 https://imagemagick.org
```

## [checkit_tiff](https://git.fsfe.org/art1pirat/checkit_tiff)

C Programm

Beispiel: `node checkit_tiff.js node checkit_tiff.js 00000001.tif`

```
asynchronously preparing wasm
run() called, but dependencies remain, so not running
'/home/tlow/tmp/checkit_tiff/checkit_tiff_compiled_to_javascript_2024-05-19/bin/checkit_tiff.js' version: master_v1.5.1-1-gd361b64
	revision: 972
licensed under conditions of libtiff (see http://libtiff.maptools.org/misc.html)
(enabled full ICC profile checking using IccProfLib by color.org, version 2.2.3
 the ICCProflib is licensed under ICC Software License, version 0.2
 copyright (c) 2003 The International Color Consortium. All rights reserved.)
(compiled with flags:  COMPAT_NEED_REALLOCARRAY PCRE2_STATIC HAVE_IccProfLib HAVE_MMAP)
cfg_file=../share/checkit_tiff/example_configs/cit_tiff6_complete.cfg
tiff file/dir=/home/tlow/work/knopflogik/projects/spk-berlin/documents/2026-01-13_Beispieldaten/Beispieldateien/fehlerhafte Dateien/00000001.tif

result IFD/tag 	--> rule description [; expected specific values] [error description] (lineno in config)
----------------------------------------------------------------------------
file: /home/tlow/work/knopflogik/projects/spk-berlin/documents/2026-01-13_Beispieldaten/Beispieldateien/fehlerhafte Dateien/00000001.tif
(./)	tag 256 (ImageWidth)	--> Tag should have a value in a range of (lineno: 17)
(./)	tag 257 (ImageLength)	--> Tag should have a value in a range of (lineno: 19)
(./)	tag 258 (BitsPerSample)	--> One or more conditions needs to be combined in a logical_or operation (open) (lineno: 28)
(./)	tag 259 (Compression)	--> One or more conditions needs to be combined in a logical_or operation (open) (lineno: 34)
(./)	tag 262 (Photometric)	--> One or more conditions needs to be combined in a logical_or operation (open) (lineno: 38)
(./)	tag 273 (StripOffsets)	--> TIFF should contain this tag. (lineno: 41)
(./)	tag 277 (SamplesPerPixel)	--> Tag should have a value in a range of (lineno: 46)
(./)	tag 278 (RowsPerStrip)	--> Tag should have a value in a range of (lineno: 51)
(EE)	tag 279 (StripByteCounts)	--> Tag should have a value in a range of; expected: 1 <= v <=4294967295 Unexpected tag value.; found: 5557 values (lineno: 54)
(./)	tag 282 (XResolution)	--> Tag should have a value in a range of (lineno: 57)
(./)	tag 283 (YResolution)	--> Tag should have a value in a range of (lineno: 60)
(./)	tag 296 (ResolutionUnit)	--> Tag should have a value in a range of (lineno: 63)
(./)	tag 254 (NewSubFileType)	--> Tag should have a value in a range of (lineno: 71)
(EE)	tag 270 (ImageDescription)	--> Tag should have a value that matches the RegEx, Tag has no Regex match.; found: str='�' hex= ---->00<---- (lineno: 97)
(./)	tag 271 (Make)	--> Tag should have a value that matches the RegEx, (lineno: 100)
(./)	tag 272 (Model)	--> Tag should have a value that matches the RegEx, (lineno: 103)
(./)	tag 274 (Orientation)	--> Tag should have a value in a range of (lineno: 107)
(./)	tag 284 (PlanarConfig)	--> One or more conditions needs to be combined in a logical_or operation (open) (lineno: 118)
(./)	tag 305 (Software)	--> Tag should have a value that matches the RegEx, (lineno: 159)
(./)	tag 306 (DateTime)	--> Tag should have a value that matches the RegEx, (lineno: 162)
(./)	tag 315 (Artist)	--> Tag should have a value that matches the RegEx, (lineno: 166)
(EE)	tag 316 (HostComputer)	--> Tag should have a value that matches the RegEx, Tag has no Regex match.; found: str='�' hex= ---->00<---- (lineno: 170)
(EE)	tag 33432 (Copyright)	--> Tag should have a value that matches the RegEx, Tag has no Regex match.; found: str='�' hex= ---->00<---- (lineno: 292)
(./)	tag 34665 (EXIFIFDOffset)	--> TIFF should contain this tag. (lineno: 304)
(./)	tag 34675 (ICC Profile)	--> TIFF should contain this tag. (lineno: 307)
(EE)	tag 330 (SubIFD)	--> Tag needs to be whitelisted in configuration.; expected: Tag should not exist. Tag exists and is not whitelisted. (lineno: -1)
(EE)	tag 36867 (EXIF DateTimeOriginal)	--> Tag needs to be whitelisted in configuration.; expected: Tag should not exist. Tag exists and is not whitelisted. (lineno: -1)
(EE)	tag 36868 (EXIF DateTimeDigitized)	--> Tag needs to be whitelisted in configuration.; expected: Tag should not exist. Tag exists and is not whitelisted. (lineno: -1)
Found 21 valid results
Found 7 errors

(EE)No, the given tif is not valid :(

```

## [ExifTool](https://exiftool.org)

Perl

Beispiel: `exiftool 00000001.tif`

```
ExifTool Version Number         : 13.10
File Name                       : 00000001.tif
Directory                       : .
File Size                       : 64 MB
File Modification Date/Time     : 2021:07:07 11:41:54+02:00
File Access Date/Time           : 2026:01:27 11:40:23+01:00
File Inode Change Date/Time     : 2026:01:13 10:05:24+01:00
File Permissions                : -rw-r--r--
File Type                       : TIFF
File Type Extension             : tif
MIME Type                       : image/tiff
Exif Byte Order                 : Little-endian (Intel, II)
Image Width                     : 3861
Image Height                    : 5557
Bits Per Sample                 : 8 8 8
Compression                     : Uncompressed
Photometric Interpretation      : RGB
Image Description               :
Make                            : i2S Corp.
Camera Model Name               : SupraScan Quartz A1 [SN: 365002] - CamQuartzHD [SN: 365002]
Orientation                     : Horizontal (normal)
Samples Per Pixel               : 3
Rows Per Strip                  : 1
X Resolution                    : 600
Y Resolution                    : 600
Planar Configuration            : Chunky
Resolution Unit                 : inches
Software                        : Capture v3.4.0
Modify Date                     : 2021:07:07 13:41:16
Artist                          : Staatsbibliothek zu Berlin
Host Computer                   :
Subfile Type                    : Full-resolution image
Strip Offsets                   : 64366747 64379858 64393297
Strip Byte Counts               : 13111 13439 5729
Predictor                       : Horizontal differencing
Copyright                       :
Document Name                   :
Page Name                       :
X Position                      : 0
Y Position                      : 0
Page Number                     : 0 0
Image Number                    : 0
User Comment                    :
XP Title                        :
XP Comment                      :
XP Author                       : 瑓慡獴楢汢潩桴步稠⁵敂汲湩
XP Keywords                     :
XP Subject                      :
Unique Camera Model             : CamQuartzHD
Localized Camera Model          : CamQuartzHD
Camera Serial Number            : 365002
Serial Number                   : 365002
Profile CMM Type                : Adobe Systems Inc.
Profile Version                 : 2.4.0
Profile Class                   : Display Device Profile
Color Space Data                : RGB
Profile Connection Space        : XYZ
Profile Date Time               : 2007:03:02 10:07:41
Profile File Signature          : acsp
Primary Platform                : Unknown ()
CMM Flags                       : Not Embedded, Independent
Device Manufacturer             :
Device Model                    :
Device Attributes               : Reflective, Glossy, Positive, Color
Rendering Intent                : Perceptual
Connection Space Illuminant     : 0.9642 1 0.82491
Profile Creator                 : basICColor GmbH
Profile ID                      : 9c6d34a5ada445f6146d98b0510c126d
Profile Copyright               : Copyright (C) 2007 by Color Solutions, All Rights Reserved. License details can be found on: http://www.eci.org/eci/en/eciRGB.php
Profile Description             : eciRGB v2
Media White Point               : 0.9642 1 0.82491
Red Tone Reproduction Curve     : (Binary data 1412 bytes, use -b option to extract)
Green Tone Reproduction Curve   : (Binary data 1412 bytes, use -b option to extract)
Blue Tone Reproduction Curve    : (Binary data 1412 bytes, use -b option to extract)
Red Matrix Column               : 0.65027 0.32028 0
Green Matrix Column             : 0.17804 0.60205 0.06783
Blue Matrix Column              : 0.13588 0.07767 0.75708
Date/Time Original              : 2021:07:07 13:41:16
Create Date                     : 2021:07:07 13:41:16
Page Count                      : 2
Image Size                      : 3861x5557
Megapixels                      : 21.5
```

Beispiel: `exiftool -X 0000001.tif`

```
<?xml version='1.0' encoding='UTF-8'?>
<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>

<rdf:Description rdf:about='00000001.tif'
  xmlns:et='http://ns.exiftool.org/1.0/' et:toolkit='Image::ExifTool 13.10'
  xmlns:ExifTool='http://ns.exiftool.org/ExifTool/1.0/'
  xmlns:System='http://ns.exiftool.org/File/System/1.0/'
  xmlns:File='http://ns.exiftool.org/File/1.0/'
  xmlns:IFD0='http://ns.exiftool.org/EXIF/IFD0/1.0/'
  xmlns:SubIFD='http://ns.exiftool.org/EXIF/SubIFD/1.0/'
  xmlns:ExifIFD='http://ns.exiftool.org/EXIF/ExifIFD/1.0/'
  xmlns:ICC-header='http://ns.exiftool.org/ICC_Profile/ICC-header/1.0/'
  xmlns:ICC_Profile='http://ns.exiftool.org/ICC_Profile/ICC_Profile/1.0/'
  xmlns:ICC-header2='http://ns.exiftool.org/ICC_Profile/ICC-header2/1.0/'
  xmlns:ICC_Profile2='http://ns.exiftool.org/ICC_Profile/ICC_Profile2/1.0/'
  xmlns:Composite='http://ns.exiftool.org/Composite/1.0/'>
 <ExifTool:ExifToolVersion>13.10</ExifTool:ExifToolVersion>
 <System:FileName>00000001.tif</System:FileName>
 <System:Directory>.</System:Directory>
 <System:FileSize>64 MB</System:FileSize>
 <System:FileModifyDate>2021:07:07 11:41:54+02:00</System:FileModifyDate>
 <System:FileAccessDate>2026:01:27 11:40:23+01:00</System:FileAccessDate>
 <System:FileInodeChangeDate>2026:01:13 10:05:24+01:00</System:FileInodeChangeDate>
 <System:FilePermissions>-rw-r--r--</System:FilePermissions>
 <File:FileType>TIFF</File:FileType>
 <File:FileTypeExtension>tif</File:FileTypeExtension>
 <File:MIMEType>image/tiff</File:MIMEType>
 <File:ExifByteOrder>Little-endian (Intel, II)</File:ExifByteOrder>
 <File:PageCount>2</File:PageCount>
 <IFD0:SubfileType>Full-resolution image</IFD0:SubfileType>
 <IFD0:ImageWidth>3861</IFD0:ImageWidth>
 <IFD0:ImageHeight>5557</IFD0:ImageHeight>
 <IFD0:BitsPerSample>8 8 8</IFD0:BitsPerSample>
 <IFD0:Compression>Uncompressed</IFD0:Compression>
 <IFD0:PhotometricInterpretation>RGB</IFD0:PhotometricInterpretation>
 <IFD0:ImageDescription></IFD0:ImageDescription>
 <IFD0:Make>i2S Corp.</IFD0:Make>
 <IFD0:Model>SupraScan Quartz A1 [SN: 365002] - CamQuartzHD [SN: 365002]</IFD0:Model>
 <IFD0:StripOffsets>(Binary data 49048 bytes, use -b option to extract)</IFD0:StripOffsets>
 <IFD0:Orientation>Horizontal (normal)</IFD0:Orientation>
 <IFD0:SamplesPerPixel>3</IFD0:SamplesPerPixel>
 <IFD0:RowsPerStrip>1</IFD0:RowsPerStrip>
 <IFD0:StripByteCounts>(Binary data 33341 bytes, use -b option to extract)</IFD0:StripByteCounts>
 <IFD0:XResolution>600</IFD0:XResolution>
 <IFD0:YResolution>600</IFD0:YResolution>
 <IFD0:PlanarConfiguration>Chunky</IFD0:PlanarConfiguration>
 <IFD0:ResolutionUnit>inches</IFD0:ResolutionUnit>
 <IFD0:Software>Capture v3.4.0</IFD0:Software>
 <IFD0:ModifyDate>2021:07:07 13:41:16</IFD0:ModifyDate>
 <IFD0:Artist>Staatsbibliothek zu Berlin</IFD0:Artist>
 <IFD0:HostComputer></IFD0:HostComputer>
 <IFD0:Copyright></IFD0:Copyright>
 <IFD0:DateTimeOriginal>2021:07:07 13:41:16</IFD0:DateTimeOriginal>
 <IFD0:CreateDate>2021:07:07 13:41:16</IFD0:CreateDate>
 <SubIFD:SubfileType>Full-resolution image</SubIFD:SubfileType>
 <SubIFD:ImageWidth>83</SubIFD:ImageWidth>
 <SubIFD:ImageHeight>120</SubIFD:ImageHeight>
 <SubIFD:BitsPerSample>8 8 8</SubIFD:BitsPerSample>
 <SubIFD:Compression>LZW</SubIFD:Compression>
 <SubIFD:PhotometricInterpretation>RGB</SubIFD:PhotometricInterpretation>
 <SubIFD:StripOffsets>64366747 64379858 64393297</SubIFD:StripOffsets>
 <SubIFD:SamplesPerPixel>3</SubIFD:SamplesPerPixel>
 <SubIFD:RowsPerStrip>49</SubIFD:RowsPerStrip>
 <SubIFD:StripByteCounts>13111 13439 5729</SubIFD:StripByteCounts>
 <SubIFD:XResolution>96</SubIFD:XResolution>
 <SubIFD:YResolution>96</SubIFD:YResolution>
 <SubIFD:PlanarConfiguration>Chunky</SubIFD:PlanarConfiguration>
 <SubIFD:ResolutionUnit>inches</SubIFD:ResolutionUnit>
 <SubIFD:Predictor>Horizontal differencing</SubIFD:Predictor>
 <ExifIFD:DocumentName></ExifIFD:DocumentName>
 <ExifIFD:PageName></ExifIFD:PageName>
 <ExifIFD:XPosition>0</ExifIFD:XPosition>
 <ExifIFD:YPosition>0</ExifIFD:YPosition>
 <ExifIFD:PageNumber>0 0</ExifIFD:PageNumber>
 <ExifIFD:ImageNumber>0</ExifIFD:ImageNumber>
 <ExifIFD:UserComment></ExifIFD:UserComment>
 <ExifIFD:XPTitle></ExifIFD:XPTitle>
 <ExifIFD:XPComment></ExifIFD:XPComment>
 <ExifIFD:XPAuthor>瑓慡獴楢汢潩桴步稠⁵敂汲湩</ExifIFD:XPAuthor>
 <ExifIFD:XPKeywords></ExifIFD:XPKeywords>
 <ExifIFD:XPSubject></ExifIFD:XPSubject>
 <ExifIFD:UniqueCameraModel>CamQuartzHD</ExifIFD:UniqueCameraModel>
 <ExifIFD:LocalizedCameraModel>CamQuartzHD</ExifIFD:LocalizedCameraModel>
 <ExifIFD:CameraSerialNumber>365002</ExifIFD:CameraSerialNumber>
 <ExifIFD:SerialNumber>365002</ExifIFD:SerialNumber>
 <ICC-header:ProfileCMMType>Adobe Systems Inc.</ICC-header:ProfileCMMType>
 <ICC-header:ProfileVersion>2.4.0</ICC-header:ProfileVersion>
 <ICC-header:ProfileClass>Display Device Profile</ICC-header:ProfileClass>
 <ICC-header:ColorSpaceData>RGB </ICC-header:ColorSpaceData>
 <ICC-header:ProfileConnectionSpace>XYZ </ICC-header:ProfileConnectionSpace>
 <ICC-header:ProfileDateTime>2007:03:02 10:07:41</ICC-header:ProfileDateTime>
 <ICC-header:ProfileFileSignature>acsp</ICC-header:ProfileFileSignature>
 <ICC-header:PrimaryPlatform>Unknown ()</ICC-header:PrimaryPlatform>
 <ICC-header:CMMFlags>Not Embedded, Independent</ICC-header:CMMFlags>
 <ICC-header:DeviceManufacturer></ICC-header:DeviceManufacturer>
 <ICC-header:DeviceModel></ICC-header:DeviceModel>
 <ICC-header:DeviceAttributes>Reflective, Glossy, Positive, Color</ICC-header:DeviceAttributes>
 <ICC-header:RenderingIntent>Perceptual</ICC-header:RenderingIntent>
 <ICC-header:ConnectionSpaceIlluminant>0.9642 1 0.82491</ICC-header:ConnectionSpaceIlluminant>
 <ICC-header:ProfileCreator>basICColor GmbH</ICC-header:ProfileCreator>
 <ICC-header:ProfileID>9c6d34a5ada445f6146d98b0510c126d</ICC-header:ProfileID>
 <ICC_Profile:ProfileCopyright>Copyright (C) 2007 by Color Solutions, All Rights Reserved. License details can be found on: http://www.eci.org/eci/en/eciRGB.php</ICC_Profile:ProfileCopyright>
 <ICC_Profile:ProfileDescription>eciRGB v2</ICC_Profile:ProfileDescription>
 <ICC_Profile:MediaWhitePoint>0.9642 1 0.82491</ICC_Profile:MediaWhitePoint>
 <ICC_Profile:RedTRC>(Binary data 1412 bytes, use -b option to extract)</ICC_Profile:RedTRC>
 <ICC_Profile:GreenTRC>(Binary data 1412 bytes, use -b option to extract)</ICC_Profile:GreenTRC>
 <ICC_Profile:BlueTRC>(Binary data 1412 bytes, use -b option to extract)</ICC_Profile:BlueTRC>
 <ICC_Profile:RedMatrixColumn>0.65027 0.32028 0</ICC_Profile:RedMatrixColumn>
 <ICC_Profile:GreenMatrixColumn>0.17804 0.60205 0.06783</ICC_Profile:GreenMatrixColumn>
 <ICC_Profile:BlueMatrixColumn>0.13588 0.07767 0.75708</ICC_Profile:BlueMatrixColumn>
 <ICC-header2:ProfileCMMType>Adobe Systems Inc.</ICC-header2:ProfileCMMType>
 <ICC-header2:ProfileVersion>2.4.0</ICC-header2:ProfileVersion>
 <ICC-header2:ProfileClass>Display Device Profile</ICC-header2:ProfileClass>
 <ICC-header2:ColorSpaceData>RGB </ICC-header2:ColorSpaceData>
 <ICC-header2:ProfileConnectionSpace>XYZ </ICC-header2:ProfileConnectionSpace>
 <ICC-header2:ProfileDateTime>2007:03:02 10:07:41</ICC-header2:ProfileDateTime>
 <ICC-header2:ProfileFileSignature>acsp</ICC-header2:ProfileFileSignature>
 <ICC-header2:PrimaryPlatform>Unknown ()</ICC-header2:PrimaryPlatform>
 <ICC-header2:CMMFlags>Not Embedded, Independent</ICC-header2:CMMFlags>
 <ICC-header2:DeviceManufacturer></ICC-header2:DeviceManufacturer>
 <ICC-header2:DeviceModel></ICC-header2:DeviceModel>
 <ICC-header2:DeviceAttributes>Reflective, Glossy, Positive, Color</ICC-header2:DeviceAttributes>
 <ICC-header2:RenderingIntent>Perceptual</ICC-header2:RenderingIntent>
 <ICC-header2:ConnectionSpaceIlluminant>0.9642 1 0.82491</ICC-header2:ConnectionSpaceIlluminant>
 <ICC-header2:ProfileCreator>basICColor GmbH</ICC-header2:ProfileCreator>
 <ICC-header2:ProfileID>9c6d34a5ada445f6146d98b0510c126d</ICC-header2:ProfileID>
 <ICC_Profile2:ProfileCopyright>Copyright (C) 2007 by Color Solutions, All Rights Reserved. License details can be found on: http://www.eci.org/eci/en/eciRGB.php</ICC_Profile2:ProfileCopyright>
 <ICC_Profile2:ProfileDescription>eciRGB v2</ICC_Profile2:ProfileDescription>
 <ICC_Profile2:MediaWhitePoint>0.9642 1 0.82491</ICC_Profile2:MediaWhitePoint>
 <ICC_Profile2:RedTRC>(Binary data 1412 bytes, use -b option to extract)</ICC_Profile2:RedTRC>
 <ICC_Profile2:GreenTRC>(Binary data 1412 bytes, use -b option to extract)</ICC_Profile2:GreenTRC>
 <ICC_Profile2:BlueTRC>(Binary data 1412 bytes, use -b option to extract)</ICC_Profile2:BlueTRC>
 <ICC_Profile2:RedMatrixColumn>0.65027 0.32028 0</ICC_Profile2:RedMatrixColumn>
 <ICC_Profile2:GreenMatrixColumn>0.17804 0.60205 0.06783</ICC_Profile2:GreenMatrixColumn>
 <ICC_Profile2:BlueMatrixColumn>0.13588 0.07767 0.75708</ICC_Profile2:BlueMatrixColumn>
 <Composite:ImageSize>3861x5557</Composite:ImageSize>
 <Composite:Megapixels>21.5</Composite:Megapixels>
</rdf:Description>
</rdf:RDF>
```

## [tifffile](https://pypi.org/project/tifffile/)

Python

## [metadata-extractor](https://github.com/drewnoakes/metadata-extractor)

Java, used by Apache Tika
