# User Guide

This document describes how the Image Validation CLI can be used to validate images and directories of images.

## Getting Started

The Image Validation CLI is currently supported on Windows and Linux. Since installation is slightly different on both plattforms, there are two separate guides located here:

- [Getting Started on Windows](./Windows.md)
- [Getting Started on Linux](./Linux.md)

Once installed and running, the only difference between Windows and Linux is how filepaths are specified, meaning `/path/to/directory` (Linux) vs. `C:\\path\to\directory` (Windows).

## Command Line Options

The Image Validation CLI can be configured via a separate configuration file as well as command line options. Command line options usually extend options from a configuration file, unless only one specific value is allowed for this option. For example, the list of included files and directories from a configuration file is extended by additional files or directories provided via command line options. In contrast, the configuration file option to specify the output location of the CSV report is overwritten by the command line option `--csv`, because only one value is allowed for this option.

A detailed description of all command line options can be found inside the separate "cli" documentation (HTML files), or by adding the `--help` option when starting the Image Validation CLI. Both of these descriptions are the same.

## Configuration Files

The Image Validation CLI can be configured with a single configuration file in [YAML](https://en.wikipedia.org/wiki/YAML) format. Loading multiple configuration files is not supported. You can find example configuration files in the `examples` directory.

Configuration files consists of the following main YAML sections:

- `input` - options to decide which images and directories need to be validated
- `extraction` - options about which extractor module to apply
- `validation` - options about how images and directories are validated
- `output` - options about logging and the CSV report

Each section is described in detail as follows.

### Input Section

The `input` section specifies which directories and files to process, and which directories and files to ignore.

You may specify two lists of file patterns (`includes` and `excludes`), which define with files to include and which files to exclude from validation. Relative filepaths are resolved relative to the location of the configuration file.

If a directory is listed in `includes`, all of its contents are validated, including any subdirectories. If a pattern is listed in `includes`, subdirectories are only processed if the directory path matches the pattern. For example, the pattern `directory/*.tif` only matches files with the "tif" extension, and thus, subdirectories are skipped. In contrast, the pattern `directory/*` matches any files or subdirectories, and thus, subdirectories are also validated.

Patterns can be defined via the "glob" syntax. Single asterisks (\*) match any characters without crossing directory boundaries. Double asterisks (\*\*) match any characters crossing directory boundings. More information about the glob syntax can be found in the documentation of [FileSystem::getPathMatcher](<https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/FileSystem.html#getPathMatcher(java.lang.String)>).

For Example:

```
input:

  includes:
    - /path/to/directory
    - /path/to/file.tif
    - /all/tif/files/*.tif
    - /all/tif/files/in/all/subdirectories/**/*.tif
    - /path/to/mixed/files/*.{tif,jpg}

  excludes:
    - /path/to/directory
    - /path/to/mixed/files/**.jpg
```

Default configuration (no files included or excluded):

```
input:

  includes:

  excludes:
```

### Extraction Section

The `extraction` sections allows to configure which extractor modules are applied to each image, as well as additional options for each of these extractor modules. However, currently, only the list of modules can be configured:

For example:

```
extraction:

  modules:
    - exiftool
```

Default configuration:

```
extraction:

  modules:
    - exiftool
    - jhove
```

### Validation Section

#### Parallel Processing Threads

Specify the number of images that are validated in parallel (default 4). Only images of the same directory are validated in parallel. Directories are processed one after another.

For example:

```
validation:

  parallelThreads: 1
```

Default configuration:

```
validation:

  parallelThreads: 4
```

#### Image Rules

The `imageRules` section contains a list of validation rules that check extracted image properties against certain conditions (e.g. `exists`, `equal`, `oneOf`). An image rule is comprised of the following options:

- `message` - an error or warning message that is shown in case the validation rule fails
- `severity` - either `error` or `warning`, an image with a warning still passes validation succesfully
- `context` - the set of properties or property subtrees that are checked against the condition
- `condition` - the condition that is checked

The options `context` and `condition` work together in the following way: the context option describes which property or properties to check, and the condition option describes how to check them. This two-fold definition is required, because image properties are hierarchical and may occur multiple times, e.g., in case of multi-page tif images with multiple IFDs. Suppose you would like validate that every multi-page tif subimage uses the colorspace `rgb`. Then, the context will describe the part "every IFD", and the condition will describe the part "has to have rgb colorspace". An appropriate rule for this scenario would be:

```
message: Every IFD needs to be have RGB colorspace
severity: error
context: $.exiftool.TIFF.IFD
condition:
  property: $.PhotometricInterpretation
  assert: equal
  value: RGB
```

Also, if the context of an image rule does not resolve to any property or properties, because they do not exist, the rule is simply skipped. This can be useful in case of rules for optional subtree properties, e.g., checking whether the ICC Profile matches a condition, only if there is an ICC Profile attached to the file.

By default, the rule context is defined as the root property tree (JsonPath `$`), meaning the context does not have any effect on the rule condition and can be ignored.

#### Conditions

Image rule conditions are comprised of the following options:

- `property` - the JsonPath to the property that is being checked relative to the `context`
- `assert` - the type of check that is performed (e.g. `equal`, `oneOf`)
- `value` - the value or list of values that the extracted image property is checked against

Properties are defined a JsonPath on the property tree.

The following assert types are supported:

- `equal` - checks that the extracted property exactly matches the comparison value
- `oneOf` - checks that the property matches one of the provided alternative values
- `isEmpty` - checks that the property exists but is empty
- `exists` - checks that the property exists and ignores its value
- `matches` - checks that the property matches the regular expression defined as comparison value
- `occurrences` - checks that the property occurs a certain number of times
- `lowerThan` - checks whether the property can be interpreted as number and is lower than the comparison value
- `lowerThanOrEqual` - checks whether the property can be interpreted as number and is lower than or equal to the comparison value
- `greaterThan` - checks whether the property can be interpreted as number and is greater than the comparison value
- `greaterThanOrEqual` - checks whether the property can be interpreted as number and is greater than or equal to the comparison value
- `isInteger` - checks whether the property can be interpreted as a natural number

In addition, conditions can be logically combined with `and`, `or` and `not` by enclosing a condition with an `and`-, `or`- or `not`-YAML section like this:

```
and:
  - <condition>
  - <condition>
  - <condition>
```

```
or:
  - <condition>
  - <condition>
  - <condition>
```

```
not:
  <condition>
```

For example, the following rule checks that a non-empty `SerialNumber` property exists either in the subtree `ExifIFD` or subtree `XMP`:

```
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

#### Additional Image Rule Examples

Check that JHove has detected the image as wellformed if the JHove extractor module is used:

```
message: JHove must detect file as wellformed
severity: error
context: $.jhove
condition:
  property: $.wellformed
  assert: equal
  value: true
```

Always check that the JHove extractor is applied and that the image is detected as wellformed even if the JHove extractor is disabled based on other configuration options:

```
message: JHove must detect file as wellformed
severity: error
condition:
  property: $.jhove.wellformed
  assert: equal
  value: true
```

Check that a Tif image has exactly one IFD if the Exiftool extractor module is used:

```
message: Must have exactly one IFD
severity: error
context: $.exiftool
condition:
  property: $.TIFF.IFD
  assert: occurrences
  value: 1
```

#### Directory Rules

The following options are used to determine whether a directory is considered valid or not. Even if no options are provided, a directory is only considered valid if all images files contained in it are evalated as valid.

##### Consistent Properties by Directory

Define a list of properties with JsonPath that are required to be consistent for all images within a directory. If there are two images with different values for the same property, the validation of this directory fails. Individual images may still be valid by themselves.

For Example:

```
validation:

  directoryRules:

    consistentProperties:
      - $.exiftool.TIFF.IFD[0].PhotometricInterpretation
      - $.exiftool.TIFF.IFD[0].BitsPerSample
```

Default configuration (no check for consistent properties):

```
validation:

  directoryRules:

    consistentProperties:
```

##### Consecutive Numbering of Files in a Directory

Define a regular expression that is used to parse a unique number from a filename that can be checked for consecutiveness within a directory. This check will verify that image files are numbered without gaps inside a directory, e.g., `000001.tif`, `000002.tif`, and so on.

The regular expression needs to be defined in a way that the number contained in a filename is matched with a single match group. For example, the regular expression `^(\d{6})\.tif$` defines a match group around a 6-digit number by adding parentheses around this part of the filename. There can be multiple match groups if required. The match group number can be specified (defaults to 1). More information about regular expressions can be found in the Java [java.util.regex.Pattern](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html) documentation.

Additionally, it is possible to configure the starting number of files (defaults to 1).

For Example:

```
validation:

  directoryRules:

    consecutiveNumbering:
      filenameRegExp: ^(\d{6})\.tif$
      numberMatchGroup: 1
      startNumber: 1
```

Default configuration (no check for consecutiveness):

```
validation:

  directoryRules:

    consecutiveNumbering:
      filenameRegExp:
      numberMatchGroup: 1
      startNumber: 1
```

### Output Section

The `output` section contains the following subsections:

- `logging` - options to control logging
- `csv` - options related to the CSV report

#### Logging

The logging section allows to control what log messages are generated during the excecution of the image validation CLI. There are two options:

- `json` - whether to write JSON-formatted log messages to `stdout`, and regular log messages to `stderr`
- `verbosity` - the verbosity of regular log messages, either `error`, `warning`, `info`, `debug`, `trace`

For example:

```
output:

  logging:
    json: true
    verbosity: info
```

Default configuration (no JSON output, only warning messages):

```
output:

  logging:
    json: false
    verbosity: warning
```

#### CSV Report

The CSV report can be configured based on two options:

- `path` - the filepath that is used to write the CSV report
- `columns` - the list of additional property columns added to CSV report

A relative filepath is resolved relative to the location of the configuration file.

Additional property columns can be specified as a list of two options:

- `label` - the column label that is used for this column in the CSV report
- `property` - the JsonPath to the property that is supposed to be included in the CSV report

For example:

```
output:

  csv:
    path: output.csv
    columns:
      - label: JHove wellformed
        property: $.jhove.wellformed
      - label: Number of IFDs
        property: $.exiftool.TIFF.IFD.length()
      - label: PhotometricInterpretation
        property: $.exiftool.TIFF.IFD[0].PhotometricInterpretation
```

Default configuration (no CSV report):

```
output:

  csv:
    path:
    columns:
```

## General Processing Steps

The "validate" command of the Image Validation CLI works by following these general processing steps:

1. Process command line options
2. Load configuration file if provided
3. Mix configuration and command line options into one configuration
4. Find all files based on the list of included and excluded filepath patterns
5. Process one directory after another
   - Process all images of the directory in parallel (with max `--threads` concurrency)
     - Extract properties for the image with each extractor module one after another
     - Combine extracted properties for the image into one property tree
     - Iterate over all image rules and check them against the extracted properties
     - Generate a JSON command line output log summarizing the validation result of the image
   - Check whether all images of the directory were validated successfully
   - Check properties that are supposed to be consistent for all images in the directory
   - Check filename consecutiveness in the directory
   - Generate a JSON command line output log summarizing the validation result for the directory
6. Build CSV report of all validation results
7. **_TODO_**: Exit with exit code based on overall validation success
