# StabiV (Linux)

This tool provides a command line interface (CLI) that validates images and their metadata against a set of validation rules.

## Installation

On Linux, download and unpack the latest release from [GitHub](https://github.com/StabiBerlin/StabiV/releases). The release file includes both OpenJDK and ExifTool binaries such that StabiV works out-of-the-box on many Linux distributions. However, you may also use the OpenJDK runtime and ExifTool that is packaged with your operating system. In this case, make sure that `java` and `exiftool` commands are available, and adapt `stabiv.sh` accordingly.

On Ubuntu 22.04, 24.04 and 26.04 no additional packages need to be installed. On Debian 12 and 13 no additional packages need to be installed. However, on Rocky Linux 9, Fedora 43 and Fedora 44, you need to install the package `libxcrypt-compat`:

```
dnf install -y libxcrypt-compat
```

Ubuntu 20.04, Debian 11 and Rocky Linux 8 are not supported. You could try to install Java and ExifTool manually, and then adapt the `stabiv.sh` accordingly.

Finally, you can start the tool from your console with the following command:

```
bash stabiv.sh --help
```

## System Requirements

StabiV may run both on powerful and small systems. However, depending on the number of images that need to be processed and the amount of information that is extracted and collected from these images, more or less free RAM is needed. The most amount of RAM is needed when generating an Excel report:

| Required Free RAM | 10.000 Images | 20.000 Images |
| ----------------- | ------------- | ------------- |
| 10 Properties     | ~1.6 GB       | ~2.2 GB       |
| 20 Properties     | ~1.9 GB       | ~3.7 GB       |
| 30 Properties     | ~2.3 GB       | ~4.4 GB       |

Please note that your individual RAM requirements may be higher depending on your images and configuration. These statistics have been estimated using a simplified dataset consisting of tiny TIF images. For large TIF images that are validated in parallel by multiple worker threads (see `--threads` option) more RAM might be required.

## First Steps

In order to get familiar with this tool, you should start taking a look at the `extract` command. The `extract` command parses image
properties with [JHove](https://jhove.openpreservation.org/) and [Exiftool](https://exiftool.org/) and visualizes the extracted information:

```
bash stabiv.sh extract examples/simple/wellformed/simple_rgb.tif
```

The extracted properties are visualized as a hierarchical JSON file:

```JSON
{
  "jhove" : {
    "TIFFMetadata" : {
      "ByteOrder" : "little-endian",
      "IFDs" : {
        "IFD" : [ {
          "Type" : "TIFF",
          "Entries" : {
            "RowsPerStrip" : "128",
            "PlanarConfiguration" : "1",
            "Threshholding" : "1",
            "TransferRange" : [ 0, 255, 0, 255, 0, 255 ],
            ...
          }
        }]
      }
    }
  },
  "exiftool" : {
    "TIFF" : {
      "ExifByteOrder" : "Little-endian (Intel, II)",
      "MIMEType" : "image/tiff",
      "FileType" : "TIFF",
      "IFD" : [ {
        "RowsPerStrip" : "128",
        "Compression" : "Uncompressed",
        "SamplesPerPixel" : "3",
        ...
      }]
    }
  }
}
```

Images will be validated based on this information. Validation rules are defined on specific properties extracted from this
hierarchy via a JSONPath ([RFC 9535](https://datatracker.ietf.org/doc/html/rfc9535)).

In order to extract the TIF tag `PhotometricInterpretation`, you can use the following command:

```
bash stabiv.sh extract -p "$.exiftool..PhotometricInterpretation" examples/simple/wellformed/simple_rgb.tif
```

Its output should be:

```JSON
[ "RGB" ]
```

There are many other possible path variants. A comprehensive list of examples can be found in the documentation of the Java library [Json-Path](https://github.com/json-path/JsonPath).

## Examples

You can test validating images from the two examples provided in the `examples` directory.

```
bash stabiv.sh validate -v --config=examples/simple/config.yaml examples/simple
```

or

```
bash stabiv.sh validate -v --config=examples/spk-berlin/config.yaml  examples/spk-berlin
```

Both commands will generate an CSV file `output.csv` in their respective directories.

## User Guide

More information can be found in the separate document "User Guide".
