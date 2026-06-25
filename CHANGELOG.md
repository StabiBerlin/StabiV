# Changelog

## v1.1.0 - 2026-06-25

General Improvements

- Allow to configure exiftool command line arguments and path to binary via cli options and config file

Breaking Changes

- Default arguments for exiftool was extended with `-charset filename=utf8` to `-a -u -s -G5 -charset filename=utf8`

## v1.0.1 - 2026-06-17

General Improvements

- Add system requirements section to Getting Started documents describing the approximate amount of RAM that is needed depending on the number of images and properties

## v1.0.0 - 2026-06-15

Breaking Changes

- Nameless cli input arguments may either list one or multiple image files that are being validated or a single directory that is scanned based on include and exclude rules

Other Improvements

- Updated ExifTool to v13.59
- Updated included OpenJDK to v26.0.1
- Improved documentation, in particular about validating individual image files via nameless cli input arguments
- Added the script `version_bump.sh` to update version strings for all pom files and `Constants.java`

## v0.7.0 - 2026-05-22

New Features

- Include version information of operating system, java and extractor modules in report metadata
- Include raw command line arguments in report metadata
- Add substitution variable `inputBaseDirectoryName` for the `path` configuration of both the `csv` and `excel` report
- Add various configuration options to customize both the csv and excel report
- Add "stress" example dataset for stress testing

Breaking Changes

- Restructured `csvPath` and `excelPath` configuration options into separate `csv` and `excel` subsections with a `path` option

General Improvements

- JHove parsing warning log messages have been reduced to debug log level
- Excel report cells with multiple values are now stored row-wise to avoid 16k column limit

## v0.6.0 - 2026-05-18

New Features

- Highlight various Excel report cells in case they are related to validation errors or warnings
- Show validation errors and warnings for a specific property column via an Excel note
- Show all validation errors and warnings in the Excel report via a drop-down menu
- Show individual error messages for each image with property deviations (in addition to reporting the directory error)
- The first 2 rows and 7 columns are frozen in the Excel report
- The first column (full path to file or directory) is hidden by default in the Excel report
- The Excel report is generated with borders around every cell
- Report property columns can also be configured inside all validation rules individually (such that report cells can be highlighted in case of errors)
- Severity levels of property deviation rules, consistency rules and the consecutive numbering check can be configured
- Directories and files are listed in lexicographic sort order in the report
- Show the expected number of images in the validation report if there are missing files in a directory
- CSV and Excel report files are never overwritten, instead filenames are modified to include a number
- The report now contains basic metadata: version information, the date and time, and the config file that was used
- The CSV and Excel report file paths may use the substitution variables `date` and `time`

Breaking Changes

- Layout of CSV and Excel report was changed significantly
- Error and warning messages are listed inside a single column in the CSV and Excel report
- The checksum column has been moved to the end of the report
- The order of report columns is determined based on where report columns are configured in the configuration file (there are now multiple locations to define them)
- Relative file paths inside a configuration file are no longer resolved with respect to the location of the configuration file
- Relative file paths inside a configuration file are now resolved with respect to a base directory that can be defined as command line option

Backend Improvement

- Instead of aborting the validation of an image upon the first error, all validation rules are evaluated for each image

## v0.5.0 - 2026-04-29

New Features

- Validation reports can be generated both as CSV and Excel (xlsx) files.
- Added short description of the `move_directories.sh` script to the user guide.

Breaking Changes

- The configuration section `csv` was renamed into `report` with sub-options `csvPath` and `excelPath`.

Backend Improvements

- Consider that the overall validation has failed if no files were found
- Catch any processing error as validation error when processing an image
- Add basic automated tests for extract and validate CLI commands
- Add automated tests for image rules
- Add automated tests for spk-berlin example configuration and check JSON output
- Add automated tests for ExifTool extractor module

Fixes

- Fix ExifTool parsing error if the same property exists without and with a trailing number, e.g. `VignettingCorr`, `VignettingCorr2`

Known Issues

- The `move_directories.sh` script is not yet documented
- The spk-berlin example configuration file doesn't check the name of the ICC profile

## v0.4.0 - 2026-04-21

New Features

- Add check for property deviations from average value (e.g. image width and image height) via the `--propertyDeviation` option or the corresponding YAML config
- Two properties can be checked for equality via the `equalsProperty` condition, e.g., verifying that `XResolution` is the same as `YResolution`
- Generate checksum files for directories that have been successfully validated via the `--checksumFilename` option or the corresponding YAML config
- CSV report lists validation success for directories and files in two separate columns
- CSV report does contain an overall validation summary and a summary for each directory
- Parent directories that contain subdirectories but no image files are not validated
- Added more documentation about the YAML format and JSONPath to the user guide

Breaking Changes

- Configuration option `consistentProperties` now distinguishes between `label` and `property` to show easier error messages
- Use slash (`/`) instead of pipe (`|`) character for CLI options `--imageRule` and `--csvColumn` due to incompatibility with Windows powershell
- The CLI option `--checksum` of the validate command is now called `--checksumAlgorithm` instead

Fixes

- Fix negation of "isEmpty" is considered valid if a property is not available at all

Known Issues

- The `move_directories.sh` script is not yet documented
- The spk-berlin example configuration file doesn't check the name of the ICC profile

## v0.3.0 - 2026-04-14

New Features

- Exit codes are now correct and reflect the overall validation success or failure
- Added an example script `move_directories.sh` that demonstrates how directories can be moved via `rsync` to two different targets depending on validation success
- Image filenames can be checked with regular expressions via Exiftool property `$.exiftool.System.FileName` and the `matches` operation
- The checksum algorithm can now be configured via `--checksum` or the corresponding yaml config
- Configuration files are validated and problems will be logged as error messages
- Exiftool and Java is now bundled with the Linux version
- Add license text and include third-party licenses in assemblies

Breaking Changes

- Configuration option `parallelThreads` was moved from YAML section `validation` to YAML section `processing` and is now called `threads`

Fixes

- Fix log messages were not redirected to `stderr` in `--json` mode

Known Issues

- It is not possible yet to define maximum deviations for image dimensions within one directory
- CSV columns do not include Tiff tag ids
- The CSV report doesn't contain an overall validation summary
- Image directories cannot be moved to a target directory based on validation success (requires bash script, see `move_directories.sh`)
- Checksum are not written to a separate file yet
- Two image properties cannot be checked for equality, e.g. checking that image resolution is the same for both x and y axis

## v0.2.0 - 2026-04-08

New Features

- Set the number of parallel validation threads via `--threads` CLI option or the corresponding yaml config
- Check properties for consistency per directory via `--consistentProperty` or the corresponding yaml config
- Check consecutive numbering within a directory via `--consecutiveNumbering` or the corresponding yaml config
- Additional images for the `spk-berlin` example demonstrating consistent property check and consecutive numbering check
- Rename `run.sh` and `run.bat` in assemblies to `spk-iv-cli.sh` and `spk-iv-cli.bat`
- Added a user guide describing all configuration options

Fixes

- Fix command line help is printed twice

Known Issues

- Image filenames cannot be checked with regular expressions yet
- The checksum algorithm cannot be configured yet (fixed to SHA-1)
- Valid images cannot be moved to a target directory yet
- It is not possible yet to define maximum deviations for image dimensions within one directory
- Configuration file problems will trigger unreadable error messages
- CSV columns do not include Tiff tag ids
- The CSV report doesn't contain an overall validation summary

## v0.1.0 - 2026-03-22

Initial version

Known Issues

- Image properties not checked for consistency in the same directory yet
- Image filenames are not checked for consistent numbering (1,2,3...) yet
- Image filenames cannot be checked with regular expressions yet
- The checksum algorithm cannot be configured yet (fixed to SHA-1)
- Valid images cannot be moved to a target directory yet
- It is not possible yet to define maximum deviations for image dimensions within one directory
- Configuration file problems will trigger unreadable error messages
- CSV columns do not include Tiff tag ids
- The CSV report doesn't contain an overall validation summary, or a summary of each directory yet
- There is no user guide yet, except for the command help man page
- Exit codes are not correct
