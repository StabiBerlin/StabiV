# SPK-Berlin Image Validation (Windows)

This tool provides a command line interface (CLI) that validates images and their metadata against a set of validation rules.

## Installation

On Windows, no additional installation is required. Double-click on `prompt.bat` to open a command prompt in this directory.

You can now start the tool from your console with the following command:

```
spk-iv-cli.bat --help
```

## First Steps

In order to get familiar with this tool, you should start taking a look at the `extract` command. The `extract` command parses image
properties with [JHove](https://jhove.openpreservation.org/) and [Exiftool](https://exiftool.org/) and visualizes the extracted information:

```
spk-iv-cli.bat extract examples/simple/wellformed/simple_rgb.tif
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
spk-iv-cli.bat extract -p "$.exiftool..PhotometricInterpretation" examples/simple/wellformed/simple_rgb.tif
```

Its output should be:

```JSON
[ "RGB" ]
```

There are many other possible path variants. A comprehensive list of examples can be found in the documentation of the Java library [Json-Path](https://github.com/json-path/JsonPath).

## Examples

You can test validating images from the two examples provided in the `examples` directory.

```
spk-iv-cli.bat validate -v --config=examples\simple\config.yaml
```

or

```
spk-iv-cli.bat validate -v --config=examples\spk-berlin\config.yaml
```

Both commands will generate an CSV file `output.csv` in their respective directories.

## User Guide

More information can be found in the separate document "User Guide".
