# Changelog

## 0.2.0 - 2026-04-08

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
- Exit codes are not correct

## 0.1.0 - 2026-03-22

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
