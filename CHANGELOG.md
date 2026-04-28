# Changelog

## v0.5.0 - 2026-04-29

Improvements

- Consider that no files were found a failed validation
- Catch any processing error as validation error when processing an image
- Add automated tests for ExifTool extractor module

Fixes

- Fix ExifTool parsing error if the same property exists without and with a trailing number, e.g. `VignettingCorr`, `VignettingCorr2`

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

- Configurtion option `parallelThreads` was moved from YAML section `validation` to YAML section `processing` and is now called `threads`

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
