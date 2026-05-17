# User Guide

This document describes how the Image Validation CLI can be used to validate images and directories of images.

## Contents

This document is structured into the following sections:

- [Getting Started](#getting-started)
- [Fundamentals](#fundamentals)
  - [JSONPath](#jsonpath)
  - [YAML Format](#yaml-format)
- [Command Line Options](#command-line-options)
- [Configuration File Options](#command-line-options)
  - [Input Section](#input-section)
  - [Processing Section](#processing-section)
  - [Extraction Section](#extraction-section)
  - [Validation Section](#validation-section)
  - [Output Section](#output-section)
- [General Processing Steps](#general-processing-steps)
- [JSON Output Mode](#json-output-mode)
  - [Move Files Depending on Validation Success](#move-files-depending-on-validation-success)
- [Exit Codes](#exit-codes)

## Getting Started

The Image Validation CLI is currently supported on Windows and Linux. Since installation is slightly different on both platforms, there are two separate guides located here:

- [Getting Started on Windows](../getting-started/Windows.md)
- [Getting Started on Linux](../getting-started/Linux.md)

Once installed and running, the only difference between Windows and Linux is how file paths are specified, meaning `/path/to/directory` (Linux) vs. `C:\\path\to\directory` (Windows).

## Fundamentals

This section describes two important concepts that are required to successfully define validation rules: JSONPath and the YAML file format.

### JSONPath

A JSONPath is a method to reference one or multiple subproperties in a hierarchy of properties. It is used to specify exactly which image properties are supposed to be validated.

JSONPath is directly linked to the [JavaScript Object Notation](https://de.wikipedia.org/wiki/JSON) (JSON) format, which allows storing a hierarchy of properties and their values as simple text format.

JSON and JSONPath are not inventions of this tool but universal standards, see [RFC 8259](https://datatracker.ietf.org/doc/html/rfc8259) and [RFC 9535](https://datatracker.ietf.org/doc/html/rfc9535). Because of that, you can also look for additional guides and tutorials online. Unfortunately, JSONPath is not as widely documented, though.

If you have worked with XML before, then you may compare JSONPath with XPath. Both XPath and JSONPath serve the same purpose. Examples on how XPath compares to JSONPath can be found [here](https://datatracker.ietf.org/doc/html/rfc9535#tbl-xpath-equivalents).

A JSONPath is an expression that selects how hierarchical properties are navigated. Let's assume that an image file contains two sub-images: one thumbnail and one full resolution image. The following property hierarchy is extracted from the image (formatted as a JSON document):

```JSON
{
  "TIFF": {
    "IFD": [
      {
        "ImageWidth": 100,
        "ImageHeight": 100,
        "SubfileType": "Reduced-resolution image"
      },
      {
        "ImageWidth": 1000,
        "ImageHeight": 1000,
        "SubfileType": "Full-resolution image"
      }
    ]
  }
}
```

You can navigate the property hierarchy by combining the following operators into a path:

- `$` - references the root level of the hierarchy
- `.` - navigates one level down in the hierarchy based on the property name that is written after the dot
- `..` - scans the hierarchy for all properties with the same name that is written after both dots
- `[i]` - references the i-th element in a list of properties

For example:

- `$` - does not navigate at all, but represents the full hierarchy
- `$.TIFF` - navigates to the hierarchy level `TIFF` and all its subproperties
- `$.TIFF.IFD` - navigates to the list of IFDs of the TIF file
- `$.TIFF.IFD[0]` - navigates to the first IFD of the TIF file
- `$.TIFF.IFD[0].SubfileType` - selects the property `SubfileType` of the first IFD of the TIF
- `$..SubfileType` - selects any `SubfileType` properties in the hierarchy

You can experiment with different JSONPaths by copying the JSON document to the [JSONPath Online Evaluator](https://jsonpath.com/) and typing in any JSONPath. Alternatively, you can use the `extract` command of the Image Validation CLI and apply a JSONPath with the `--property` argument as mentioned in the _Getting Started_ guide.

JSONPaths containing special characters like asterisks (`*`) should be enclosed in double quotes (`"`) or single quotes (`'`) to avoid any parsing problems both in YAML configuration files and command line arguments.

There are a lot more possibilities to define JSONPaths. Unfortunately, a comprehensive documentation of all options exceeds the scope of this user guide. Detailed information about the syntax can be found in [RFC 9535](https://datatracker.ietf.org/doc/html/rfc9535) or in the [documentation](https://github.com/json-path/JsonPath) of the Java library that is used by the Image Validation CLI to resolve JSONPaths. Various examples are listed here (from the RFC documentation):

- [Various Examples](https://datatracker.ietf.org/doc/html/rfc9535#name-jsonpath-examples)
- [Wildcard Examples](https://datatracker.ietf.org/doc/html/rfc9535#name-examples-3)
- [Index Examples](https://datatracker.ietf.org/doc/html/rfc9535#name-examples-4)
- [Slice Examples](https://datatracker.ietf.org/doc/html/rfc9535#name-examples-5)
- [Filter Example](https://datatracker.ietf.org/doc/html/rfc9535#name-examples-6)
- [Function Examples](https://datatracker.ietf.org/doc/html/rfc9535#name-examples-7)
- [Scanning Example](https://datatracker.ietf.org/doc/html/rfc9535#name-examples-9)

### YAML Format

The [YAML](https://en.wikipedia.org/wiki/YAML) (Yet Another Markup Language) format is very similar to the JSON format and also allows storing hierarchical properties as simple text. The Image Validation CLI uses the YAML format to specify validation rules as configuration files.

In comparison to JSON, the YAML format is usually easier for humans to read and write. Besides, you can specify comments, which makes them ideal for configuration files.

Similar to JSON and JSONPath, the YAML format is also a universal standard (see [RFC 9512](https://datatracker.ietf.org/doc/html/rfc9512)). You can find lots of information about the YAML format online, e.g., at [Wikipedia](https://en.wikipedia.org/wiki/YAML) or [RedHat](https://www.redhat.com/en/topics/automation/what-is-yaml).

Most importantly, YAML is structured based on indentation, for example:

```YAML
property:
  subproperty: value
  another: something
```

The property `subproperty` is considered a subproperty of `property`, because it is slightly shifted to right with two spaces. The same is true for `another`, which is also considered a subproperty of `property`. In contrast, in the following YAML document, property `another` is no longer considered a subproperty of `property`.

```YAML
property:
  subproperty: value
another: something
```

Besides that, you can distinguish between named subproperties (like shown above) and a list of values or subproperties via the `-` (minus) symbol. A simple list is defined as:

```YAML
property:
  - value1
  - value2
  - value3
```

This document models the property `property` as a list instead of a section containing subproperties. The corresponding JSON document would look like this:

```JSON
{
  "property": ["value1", "value2", "value"]
}
```

The Image Validation CLI configuration file uses these lists whenever a configuration options accepts an arbitrary amount of something, e.g. the list of image validation rules, or the list of directories that are supposed to be validated.

Lists may not only contain specific values, but also subproperties:

```YAML
property:
  - subproperty: value
    another: something
  - subproperty: value2
    another: something2
```

This document corresponds to the following JSON document:

```JSON
{
  "property": [
    {"subproperty": "value", "another": "something"},
    {"subproperty": "value2", "another": "something2"}
  ]
}
```

In case of Image Validation CLI configuration files, each list element needs to contain the same subproperties. The following YAML document would usually not be a valid configuration even though it is considered a valid YAML document:

```YAML
property:
  - subproperty: value
  - another: something
```

If there are issues in a YAML configuration file, detailed error messages are printed.

## Command Line Options

The Image Validation CLI can be configured via a separate configuration file as well as command line options. Command line options usually extend options from a configuration file, unless only one specific value is allowed for this option. For example, the list of included files and directories from a configuration file is extended by additional files or directories provided via command line options. In contrast, the configuration file option to specify the output location of the CSV report is overwritten by the command line option `--csv`, because only one value is allowed for this option.

A detailed description of all command line options can be found inside the separate `cli` documentation (HTML files), or by adding the `--help` option when starting the Image Validation CLI. Both of these descriptions are the same.

## Configuration File Options

The Image Validation CLI can be configured with a single configuration file in [YAML](https://en.wikipedia.org/wiki/YAML) format. Loading multiple configuration files is not supported. You can find example configuration files in the `examples` directory.

Configuration files consists of the following main YAML sections:

- `input` - options to decide which images and directories need to be validated
- `processing` - general processing options
- `extraction` - options about which extractor module to apply
- `validation` - options about how images and directories are validated
- `output` - options about logging and the validation report

Each section is described in detail as follows.

### Input Section

The `input` section specifies which directories and files will be processed, and which directories and files will be ignored.

You may specify two lists of file patterns (`includes` and `excludes`), which define with files to include and which files to exclude from validation. Relative file paths are resolved relative to the location of the configuration file.

If a directory is listed in `includes`, all of its contents are validated, including any subdirectories. If a pattern is listed in `includes`, subdirectories and their files are only processed if the full file path matches the pattern. For example, the pattern `directory/*.tif` only matches files with the `.tif` extension, and thus, subdirectories are skipped. In contrast, the pattern `directory/**` matches any files or subdirectories, and thus, subdirectories and their files are also validated.

Patterns can be defined via the "glob" syntax. Single asterisks (\*) match any characters without crossing directory boundaries. Double asterisks (\*\*) match any characters crossing directory bounds. More information about the glob syntax can be found in the documentation of [FileSystem::getPathMatcher](<https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)>).

Patterns containing special characters like asterisks (`*`) should be enclosed in double quotes (`"`) to avoid any parsing problems.

For Example:

```YAML
input:

  includes:
    - /path/to/directory
    - /path/to/file.tif
    - "/all/tif/files/*.tif"
    - "/all/tif/files/in/all/subdirectories/**/*.tif"
    - "/path/to/mixed/files/*.{tif,jpg}"

  excludes:
    - /path/to/directory
    - "/path/to/mixed/files/**.jpg"
```

Alternatively, but not recommended, the list of includes and excludes can also be defined as pairs of `baseDirectory` and `path`, for example:

```YAML
input:

  includes:
    - baseDirectory: /path/to/directory
    - path: "subdirectory/*.tif"
```

This distinction is used by the tool to differentiate how relative paths are resolved in case they are provided as CLI argument or from a configuration file. When providing a relative filepath via CLI argument, the `baseDirectory` defaults to the current working directory. When defining configuration file options, the `baseDirectory` defaults to the directory that contains the configuration file.

By default, no files are included or excluded, which corresponds to the following empty configuration:

```YAML
input:

  includes:

  excludes:
```

### Processing Section

The `processing` section allows configuring general processing options that are not directly related to the extraction of image properties and their validation.

#### Parallel Processing Threads

Specify the number of images that are processed in parallel (default 4). Only images of the same directory are processed in parallel. Directories are processed one after another.

For example:

```YAML
processing:

  threads: 1
```

Default configuration:

```YAML
processing:

  threads: 4
```

#### Checksum Algorithms

Specify which checksum algorithm is used when calculating the checksum of an image file. Only certain algorithms are available, see [MessageDigest Algorithms](https://docs.oracle.com/en/java/javase/21/docs/specs/security/standard-names.html#messagedigest-algorithms), e.g., `MD5`, `SHA-1`, `SHA-512`.

For example:

```YAML
processing:

  checksum: MD5
```

Default configuration:

```YAML
processing:

  checksum: SHA-1
```

### Extraction Section

The `extraction` sections allows configuring how image properties are extracted from images. Image properties may be extracted using the following two extraction modules:

- `jhove` - uses [JHove](https://jhove.openpreservation.org/)
- `exiftool` - uses [ExifTool](https://exiftool.org/)

#### Extraction Modules

Currently, only the list of modules can be configured:

For example:

```YAML
extraction:

  modules:
    - exiftool
```

Default configuration:

```YAML
extraction:

  modules:
    - exiftool
    - jhove
```

### Validation Section

The `validation` section defines all rules that are checked when validating individual images and directories of images.

#### Image Rules

The `imageRules` section contains a list of validation rules that check extracted image properties against certain conditions (e.g. `exists`, `equal`, `oneOf`). An image rule consists of the following options:

- `message` - an error or warning message that is shown in case the validation rule fails
- `severity` - either `error` (default) or `warning`, an image with a warning still passes validation successfully
- `reportColumns` - a list of properties that are included in the validation report (similar to the `output` section, see below)
- `context` - the set of properties or property subtrees that are checked against the condition
- `condition` - the condition that is checked

The option `message` allows specifying a message that is shown for an image in case it violates the validation rule. The severity of failing a rule is defined in the `severity` option. A value of `error` means that failing this rule also fails the overall validation of the image. A value of `warning` declares this rule to be a hint only. A failed rule with severity level `warning` will still pass the overall validation.

The option `reportColumns` allows specifying a list of properties that are added as additional columns to the validation report. In contrast to columns defined in the `output` section of the configuration file, columns that are defined for a specific validation rule are linked to that rule. If a rule fails for an image, the corresponding Excel cells of all listed columns are highlighted as error or warning. Columns defined in the `output` section are not linked to any rule and will not be highlighted. However, you can declare the same property or the same column both in the `output` section and various validation rules. Columns with the same label and the same JSONPath are detected as duplicates and will only be included once in the final report. Each column needs to be declared with a `label` (column name) and `property` (JSONPath) value, similar to the `output` section, see below. An example would be:

```YAML
message: JHove must detect file as wellformed
severity: error
reportColumns:
  - label: JHove wellformed
    property: $.jhove.wellformed
context: $.jhove
condition:
  property: $.wellformed
  assert: equal
  value: true
```

The options `context` and `condition` work together in the following way: the context option describes which property or properties are checked, and the condition option describes how they are checked. This two-fold definition is required, because image properties are hierarchical and may occur multiple times, e.g., in case of multipage TIF images with multiple IFDs. Suppose you would like validate that every multipage TIF sub-image uses the color space `rgb`. Then, the context will describe the part "every IFD", and the condition will describe the part "has to have RGB color space". An appropriate rule for this scenario would be:

```YAML
message: Every IFD needs to be have RGB colorspace
severity: error
context: $.exiftool.TIFF.IFD
condition:
  property: $.PhotometricInterpretation
  assert: equal
  value: RGB
```

Also, if the context of an image rule does not resolve to any property or properties, because they do not exist, the rule is simply skipped. This can be useful in case of rules for optional subtree properties, e.g., checking whether the ICC Profile matches a condition, only if there is an ICC Profile attached to the file.

By default, the rule context is defined as the root property tree (JSONPath `$`), meaning the context does not have any effect on the rule condition and can be ignored.

JSONPaths containing special characters like asterisks (`*`) should be enclosed in double quotes (`"`) to avoid any parsing problems.

##### Conditions

Image rule conditions are comprised of the following options:

- `property` - the JSONPath to the property that is being checked relative to the `context`
- `assert` - the type of check that is performed (e.g. `equal`, `oneOf`)
- `value` - the value or list of values that the extracted image property is checked against

The following assert types are supported:

- `equal` - checks that the property value exactly matches the comparison value
- `equalsProperty` - checks that the property value exactly matches another property's value extracted from the provided JSONPath
- `oneOf` - checks that the property matches one of the provided alternative values
- `isEmpty` - checks that the property value is empty (matches an empty string)
- `exists` - checks that the property exists and ignores its value
- `matches` - checks that the property matches the regular expression defined as comparison value
- `occurrences` - checks that the property occurs a certain number of times
- `lowerThan` - checks whether the property can be interpreted as number and is lower than the comparison value
- `lowerThanOrEqual` - checks whether the property can be interpreted as number and is lower than or equal to the comparison value
- `greaterThan` - checks whether the property can be interpreted as number and is greater than the comparison value
- `greaterThanOrEqual` - checks whether the property can be interpreted as number and is greater than or equal to the comparison value
- `isInteger` - checks whether the property can be interpreted as a natural number

In addition, conditions can be logically combined with `and`, `or` and `not` by enclosing a condition with an `and`-, `or`- or `not`-YAML section like this:

```YAML
and:
  - <condition>
  - <condition>
  - <condition>
```

```YAML
or:
  - <condition>
  - <condition>
  - <condition>
```

```YAML
not:
  <condition>
```

For example, the following rule checks that a non-empty `SerialNumber` property exists either in the subtree `ExifIFD` or subtree `XMP`:

```YAML
message: SerialNumber required
severity: error
context: $.exiftool
condition:
  or:
    - not:
        property: $..ExifIFD.SerialNumber
        assert: isEmpty
    - not:
        property: $..XMP.SerialNumber
        assert: isEmpty
```

##### Additional Image Rule Examples

Check that JHove has detected the image as well-formed if the JHove extractor module is used:

```YAML
message: JHove must detect file as wellformed
severity: error
context: $.jhove
condition:
  property: $.wellformed
  assert: equal
  value: true
```

Always check that the JHove extractor is applied and that the image is detected as well-formed even if the JHove extractor is disabled based on other configuration options:

```YAML
message: JHove must detect file as wellformed
severity: error
condition:
  property: $.jhove.wellformed
  assert: equal
  value: true
```

Check that a TIF image has exactly one IFD if the ExifTool extractor module is used:

```YAML
message: Must have exactly one IFD
severity: error
context: $.exiftool
condition:
  property: $.TIFF.IFD
  assert: occurrences
  value: 1
```

Another example rules can be found in the configuration files in the `examples` directory.

#### Directory Rules

The following options are used to determine whether a directory is considered valid or not. Even if no options are provided, a directory is only considered valid if all images files contained in it are evaluated as valid based on all defined image rules.

##### Consistent Properties by Directory

Check that properties for all images within a directory are consistent, meaning, that they have the exact same value. If there are two images with different values for the same property, the validation of this directory fails. The most frequent value is considered the "correct" target value. All images that have other values are considered inconsistent and will fail validation depending on the specified severity level. Each consistency rule consists of the following options:

- `label` - the label that describes the property that is checked for consistency
- `property` - the JSONPath for the property that is checked for consistency
- `severity` - whether an inconsistent value is considered an `error` (default) or `warning`
- `reportColumn` - whether the property should be included in the final validation report as additional column, either `true` (default) or `false`

For Example:

```YAML
validation:

  directoryRules:

    consistentProperties:
      - label: Color Space
        property: $.exiftool.TIFF.IFD[0].PhotometricInterpretation
        severity: error
        reportColumn: true

      - label: Bits Per Sample
        property: $.exiftool.TIFF.IFD[0].BitsPerSample
        severity: warning
        reportColumn: false
```

By default, no properties are checked for consistency, meaning the following configuration is used:

```YAML
validation:

  directoryRules:

    consistentProperties:
```

##### Consecutive Numbering of Files in a Directory

Define a regular expression that is used to parse a unique number from all filenames in a directory that are then checked for consecutiveness. This check will verify that image files are numbered without gaps inside a directory, e.g., `00000001.tif`, `00000002.tif`, and so on.

The following options need to be defined:

- `filenameRegExp` - the regular expression that is used to extract a number from the filename
- `numberMatchGroup` - the id of the capture group that matches the number part of the filename
- `severity` - whether to consider missing images an `error` (default) or `warning`
- `filenameTemplate` - a template that is used to generate expected filenames from missing image numbers
- `startNumber` - the starting number of the consecutiveness check (usually 0 or 1)

The regular expression needs to be defined in a way that the number contained in a filename is matched with a single match group (or capture group). For example, the regular expression `^(\d{8})\.tif$` defines a match group around an 8-digit number by adding parentheses around this part of the filename. There can be multiple match groups if required. The match group number can be specified (defaults to 1). More information about regular expressions can be found in the Java [java.util.regex.Pattern](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html) documentation.

In order to print nice filenames for missing files, you may specify a `filenameTemplate`. The filename template is then translated into an actual filename for each missing image file. Filename templates need to be defined as `printf`-style format strings (see [Java documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Formatter.html)). The filename template will be provided with one integer number: the image number of the missing image. Thus, you need to include this image number via `%d` (d = decimal integer) inside your filename template. For example, the filename template `%d.tif` will generate filenames `1.tif`, `2.tif`, `3.tif`, etc. You can add zero-padding to your filename template by including the number of digits prefixed by `0`. The filename template `%08d.tif` will generate 8-digit zero-padded filenames `00000001.tif`, `00000002.tif`, `00000003.tif`. If no filename template is provided, simply the image number is included inside error or warning messages.

Additionally, it is possible to configure the starting number expected image files (defaults to 1).

For Example:

```YAML
validation:

  directoryRules:

    consecutiveNumbering:
      filenameRegExp: "^(\d{8})\.tif$"
      filenameTemplate: "%08d.tif"
      severity: error
      numberMatchGroup: 1
      startNumber: 1
```

By default, files are not checked for consecutiveness. Thus, the following configuration is used:

```YAML
validation:

  directoryRules:

    consecutiveNumbering:
      filenameRegExp:
      filenameTemplate:
      severity: error
      numberMatchGroup: 1
      startNumber: 1
```

##### Property Deviations

You may check that a number-based property is almost consistent for all images within a directory by specifying a maximum allowed deviation from the average or median over all images. This is usually applied to dimension properties of images, e.g. the `ImageWidth` or `ImageHeight` property.

In order to specify a property deviation rule, you need to define the following options:

- `label` - the label that is used to describe the property in case of errors, mandatory
- `property` - the JSONPath referring to the property that is being checked, mandatory
- `reportColumn` - whether to include the property as additional column in the final validation report, either `true` (default) or `false`

Then, a property deviation condition consists of the following options:

- `aggregate` - the method of aggregation, e.g. `average` or `median`, optional (default is `average`)
- `maxDeviation` - the maximum deviation as absolute number or percentage, mandatory
- `inPercent` - whether the maximum deviation is provided as absolute number or percentage, optional (default is `false`)
- `severity` - whether to consider exceeding the maximum allowed deviation an `error` or `warning`

An example configuration would be:

```YAML
validation:

  directoryRules:

    propertyDeviations:
      - label: ImageWidth
        property: $.exiftool.TIFF.IFD[0].ImageWidth
        reportColumn: true
        condition:
          aggregate: average
          maxDeviation: 10
          inPercent: false
```

This configuration validates that the width of all images lies within at most 10 pixels of the average width of all images. If any image inside the directory deviates from the average by more than 10 pixels, the directory is considered invalid.

In addition, you may define property deviation rules for specific slices of images, e.g. the first 3 or last 3 images of a directory. These slices are defined by the following options:

- `from` - the starting index of the slice that is checked (inclusive, begins with 1)
- `to` - the end index of the slice that is checked (inclusive, begins with 1)
- `reversed` - whether slice indexes are counted in ascending or descending order
- `condition` - the property deviation condition (same as above)

The `from` and `to` indexes do not refer to the number extracted from filenames via a regular expression as defined for the consecutiveness check. Instead, the index is determined based on the lexicographical order of filenames, e.g.:

- `a.tif` - index 1
- `ba.tif` - index 2
- `bb.tif` - index 3

With the following configuration, you can validate that the first 3 images may deviate by at most 10 pixels from their average:

```YAML
validation:

  directoryRules:

    propertyDeviations:
      - label: ImageWidth
        property: $.exiftool.TIFF.IFD[0].ImageWidth
        condition:
          aggregate: average
          maxDeviation: 10
          inPercent: false
          severity: warning
        additionalSlices:
          - from: 1
            to: 3
            reversed: false
            condition:
              aggregate: average
              maxDeviation: 10
              inPercent: false
              severity: error
```

Additional slice conditions are evaluated within a slice. Meaning, the average is calculated within the slice. The width of the first 3 images could deviate from all other images by any amount. However, within the first 3 images, neither of them may deviate more than 10 pixels from their average.

Consequently, the default deviation condition does not apply to the images of the slice anymore. In this case, the default condition would only be applied to images 4, 5, 6, and so on. Also, the average that is used to calculate deviations would also only be calculated for these non-slice images.

By default, no property deviations are checked. The following configuration is used:

```YAML
validation:

  directoryRules:

    propertyDeviations:
```

### Output Section

The `output` section contains the following subsections:

- `checksum` - whether to generate checksum files for each valid directory
- `logging` - options to control logging
- `report` - options related to the validation report

#### Checksum Files

The checksum section allows configuring whether a checksum file is generated if a directory is considered valid. The checksum file contains checksums for every image file of the directory. Checksums are calculated based on the algorithm defined via the configuration option `checksum` in configuration section `processing`.

The following options can be specified to influence how checksum files are generated:

- `filename` - the filename template that is used to generate checksum filenames
- `lineFormat` - the format template that is used to generate a line inside a checksum file

Filename templates may use the following substitution variables to generate filenames:

- `directory` - the name of the directory that was successfully validated

Variables may be referenced via the syntax `${variable}`. For example, the filename template `${directory}.sha1` generates filenames that consist of the directory name followed by the file extension `.sha1`.

Line format templates may use the following substitution variables:

- `checksum` - the actual checksum of the image file
- `filename` the filename of the image file

For example, the line format `${checksum} *${filename}` generates lines that first print the checksum of the image file, followed by a space and asterisk character, followed by the filename of the image file.

An example configuration may look like this:

```YAML
output:

  checksum:
    filename: ${directory}.sha1
    lineFormat: ${checksum} *${filename}
```

By default, no checksum files are generated. The following default configuration is used:

```YAML
output:

  checksum:
    filename:
    lineFormat: ${checksum} *${filename}
```

#### Validation Report

The validation report can be generated as CSV and Excel file with the following options:

- `csvPath` - the file path that is used to write the validation report as CSV file
- `excelPath` - the file path that is used to write the validation report as Excel file
- `columns` - the list of additional property columns added to CSV report

A relative file path is resolved relative to the location of the configuration file.

Additional property columns can be specified as a list of two options:

- `label` - the column label that is used for this column in the CSV report
- `property` - the JSONPath to the property that is supposed to be included in the CSV report

The order of columns is determined based on where these columns are defined in the configuration file:

1. First, columns from the image validation rules are listed.
2. Then, columns defined as part of the consistency rules are listed.
3. Then, columns from the maximum deviation rules are listed.
4. Finally, additional columns defined in this section are listed.

For example:

```YAML
output:

  report:
    csvPath: output.csv
    excelPath: output.xlsx
    columns:
      - label: JHove wellformed
        property: $.jhove.wellformed
      - label: Number of IFDs
        property: $.exiftool.TIFF.IFD.length()
      - label: PhotometricInterpretation
        property: $.exiftool.TIFF.IFD[0].PhotometricInterpretation
```

Default configuration (no CSV or Excel report):

```YAML
output:

  report:
    csvPath:
    excelPath:
    columns:
```

#### Logging

The logging section allows controlling what log messages are generated during the execution of the image validation CLI. There are two options:

- `json` - whether to write JSON-formatted log messages to `stdout`, and regular log messages to `stderr`
- `verbosity` - the verbosity of regular log messages, either `error`, `warning`, `info`, `debug`, `trace`

For example:

```YAML
output:

  logging:
    json: true
    verbosity: info
```

Default configuration (no JSON output, only warning messages):

```YAML
output:

  logging:
    json: false
    verbosity: warning
```

## General Processing Steps

The "validate" command of the Image Validation CLI works by following these general processing steps:

1. Read command line options
2. Load configuration file if provided
3. Mix configuration and command line options into one configuration
4. Find all files based on the list of included and excluded file path patterns
5. Process one directory after another
   - Process all images of the directory in parallel (with max `--threads` concurrency)
     - Extract properties for the image with each extractor module one after another
     - Combine extracted properties for the image into one property tree
     - Iterate over all image rules and check them against the extracted properties
     - Generate a JSON command line output log summarizing the validation result of the image
   - Check whether all images of the directory were validated successfully
   - Check properties that are supposed to be consistent for all images in the directory
   - Check filename consecutiveness in the directory
   - Check property deviations in the directory
   - Write a checksum file if the directory is considered valid
   - Generate a JSON command line output log summarizing the validation result for the directory
6. Build CSV report of all validation results
7. Exit with exit code based on overall validation success

## JSON Output Mode

In order to post-process validation results the Image Validation CLI supports a JSON output mode by specifying the `--json` option. When this mode is active, validation reports are printed to the "stdout" [output stream](https://en.wikipedia.org/wiki/Standard_streams) for every image file and directory. Any log messages are redirected to the "stderr" output stream. This allows triggering other tools based on the validation success or failure, e.g. to send an e-mail in case there is a validation error.

The JSON output mode generates one JSON document per line of output. Each JSON document follows the same structure and consists of the following properties:

- `time` - the current time
- `type` - either `file` or `directory`, depending on whether the validation report concerns a file or directory
- `path` - the absolute path to the file or directory that was validated
- `checksum` - the checksum of the file (only for files)
- `validation` - either `success` or `failed` depending on the validation success
- `error` - either `null` or an error message describing the cause of validation failure
- `warning` - either `null` or a warning message describing images validation rules with severity level "warning" that did not pass

For example, successful images will be reported as:

```JSON
{"time":"2026-04-13 15:42:55.151","type":"file","path":"/path/to/valid.tif","checksum":"04baa2eb55625d139b56ea00d6e8f82e0d01462d","validation":"success","error":null,"warning":null}
```

Successful directories will be reported as:

```JSON
{"time":"2026-04-13 15:42:54.276","type":"directory","path":"/path/to/valid/directory","checksum":null,"validation":"success","error":null,"warning":null}
```

Images that fail validation are reported as:

```JSON
{"time":"2026-04-13 15:42:54.909","type":"file","path":"/path/to/invalid.tif","checksum":"79b65f7a496fc8bede1756f32c2f9f75d7468688","validation":"failed","error":"SerialNumber required","warning":null}
```

Directories that fail validation are reported as:

```JSON
{"time":"2026-04-13 15:42:54.912","type":"directory","path":"/path/to/invalid/directory","checksum":null,"validation":"failed","error":"at least one image has a validation error","warning":null}
```

### Move Files Depending on Validation Success

Based on the JSON output mode, it is possible to automate various tasks, e.g., to send an e-mail in case of validation errors. For Linux, there is one example script `move_directories.sh` in the `scripts` directory, which demonstrates how image directories can be moved to success or failure directories depending on the individual validation result of each directory.

The `move_directories.sh` script depends on the following options that need to be adjusted in the script itself:

- `SPK_IV_CLI_COMMAND` - the actual image validation command that is executed
- `RSYNC_OPTIONS` - options for the `rsync` command that determines how files are copied or moved, see [man page](https://linux.die.net/man/1/rsync)
- `SOURCE_BASE_DIRECTORY` - the base directory of the source files that will being copied or moved
- `SUCCESS_TARGET` - the target directory for image directories that were successfully validated
- `FAILURE_TARGET` - the target directory for image directories that failed validation

The `SOURCE_BASE_DIRECTORY` determines the starting point at which the directory hierarchy is preserved. For example, copying a directory `/path/to/base/with/subdirectory` with the base directory `/path/to/base` will copy `subdirectory` but also create a parent directory `with` at the target location.

After editing these options, you can start the `move_directories.sh` script by providing any image validation CLI options to it, for example:

```
bash move_directories.sh -v --config ../examples/spk-berlin/config.yaml
```

Note that, by default, the CLI command `validate` and JSON output option `--json` are added by the script itself.

Then, the script follows these steps:

1. Execute image validation CLI with provided command line arguments
2. Parse each JSON output line and extract information
3. If validation results were provided for a directory
   - determine whether validation was successful or not from the JSON output
   - call `rsync` with either the `SUCCESS_TARGET` or `FAILURE_TARGET` directory
4. Repeat until image validation CLI is done

## Exit Codes

The "validate" command returns special exit codes depending on whether validation for all images and directories was successful or not:

- `0` - successful validation of all images and directories without any errors
- `1` - generic program error, validation was aborted
- `2` - an unknown CLI option or invalid parameter was specified
- `3` - no program errors, but validation failed for at least one image or directory with an error
