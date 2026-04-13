#/bin/bash

# This script parses the JSON output of the image validation CLI and uses the "rsync" command to move directories 
# to two different locations depending on whether validation was succesful or not for that directory.

# the directory of this script
SCRIPT_DIR=$(dirname "$0")

# the base validation command for the image validation CLI
SPK_IV_CLI_COMMAND="bash $SCRIPT_DIR/../spk-iv-cli.sh validate --json $@"

# any additional rsync options, e.g.
# -a, archive mode, recursive copy, preserve permissions, owner, group, modification time, symlinks
# -v, verbose output
# -h, human readable numbers
# --remove-source-files, to delete sources files after copying
# --dry-run, only simulate moving files
RSYNC_OPTIONS="-vah --remove-source-files --dry-run"

# the base directory that determines the starting point at which the directory hierarchy is preserved
# for example, coyping a directory "/path/to/base/with/subdirectory" with the base directory "/path/to/base" 
# will copy "subdirectory" but also create a parent directory "with" at the target location
SOURCE_BASE_DIRECTORY="$(realpath $SCRIPT_DIR/../examples)"

# the target location for directories that were succesfully validated (anything supported by rsync)
# if empty, valid directories are not moved
SUCCESS_TARGET="$(realpath $SCRIPT_DIR/../target/success)"

# the target location for directories that failed validation (anything supported by rsync)
# if empty, invalid directories are not moved
FAILURE_TARGET="$(realpath $SCRIPT_DIR/../target/failed)"

### --- end of settings ---

echo "start spk-iv-cli"
echo "+ $SPK_IV_CLI_COMMAND"
$SPK_IV_CLI_COMMAND \
\
| while read -r LINE; do
  # parse JSON output
  VALIDATION_RESULT=$(jq -r '.validation' <<< "$LINE") # either "success" or "failed"
  VALIDATION_ERROR=$(jq -r '.error' <<< "$LINE")       # either empty or description of validation error
  FILE_OR_DIRECTORY=$(jq -r '.type' <<< "$LINE")       # either "file" or "directory"
  FILE_OR_DIRECTORY_PATH=$(jq -r '.path' <<< "$LINE")  # file path to file or directory

  if [[ "$FILE_OR_DIRECTORY" == "file" ]]; then
    # ignore validation reports about individual files, since only directors are moved
    : # echo "File $FILE_OR_DIRECTORY_PATH was validated with $VALIDATION_RESULT"
  elif [[ "$FILE_OR_DIRECTORY" == "directory" ]]; then
    # determine relative path between validated directory and base directory
    # such that the directory structure is preserved at the target location
    RELATIVE_PATH=$(realpath --relative-to="$SOURCE_BASE_DIRECTORY" "$FILE_OR_DIRECTORY_PATH")
    # check whether validation was succesful or not
    case "$VALIDATION_RESULT" in
      success)
        echo "   "
        echo "---"
        echo "validation for directory \"$FILE_OR_DIRECTORY_PATH\" was succesfull"
        if [[ -n "$SUCCESS_TARGET" ]]; then
          (
            # trigger rsync command that moves a valid directory to its target location
            set -x
            rsync $RSYNC_DRY_RUN $RSYNC_OPTIONS --relative $SOURCE_BASE_DIRECTORY/./$RELATIVE_PATH $SUCCESS_TARGET
          )
        fi
        ;;
      failed)
        echo "   "
        echo "---"
        echo "validation for directory \"$FILE_OR_DIRECTORY_PATH\" failed with error \"$VALIDATION_ERROR\""
        if [[ -n "$FAILURE_TARGET" ]]; then
          (
            # trigger rsync command that moves an invalid directory to its target location
            set -x
            rsync $RSYNC_DRY_RUN $RSYNC_OPTIONS --relative $SOURCE_BASE_DIRECTORY/./$RELATIVE_PATH $FAILURE_TARGET
          )
        fi
        ;;
    esac
  else
    echo "unknown json type \"$FILE_OR_DIRECTORY\""
  fi 
done