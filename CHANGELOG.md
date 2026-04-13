# Changelog

## v0.3.0 - tbd

New Features

- Exit codes are now correct and reflect the overall validation success or failure
- Added an example script `move_directories.sh` that demonstrates how directories can be moved via `rsync` to two different targets depending on validation success
- Image filenames can be checked with regular expressions via Exiftool property `$.exiftool.System.FileName` and the `matches` operation
- The checksum algorithm can now be configured via `--checksum` or the corresponding yaml config
- Configuration files are validated and problems will be logged as error messages

Breaking Changes

- Configurtion option `parallelThreads` was moved from YAML section `validation` to YAML section `processing` and is now called `threads`

Fixes

- Fix log messages were not redirected to `stderr` in `--json` mode

Known Issues

- It is not possible yet to define maximum deviations for image dimensions within one directory
- CSV columns do not include Tiff tag ids
- The CSV report doesn't contain an overall validation summary
- Exiftool and Java is not bundled with the Linux version

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
