@ECHO OFF 
setlocal

REM Get directory of this script
set SCRIPT_DIR=%~dp0

java -jar %SCRIPT_DIR%\imagevalidation-cli-1.0.0-SNAPSHOT.jar %*

endlocal