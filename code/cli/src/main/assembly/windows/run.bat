@ECHO OFF 
setlocal

REM Get directory of this script
set SCRIPT_DIR=%~dp0

REM java directory
set JAVA_HOME=%SCRIPT_DIR%\tools\java

REM exiftool directory
set EXIFTOOL_HOME=%SCRIPT_DIR%\tools\exiftool

REM modify path
set PATH=%EXIFTOOL_HOME%;%PATH%

%JAVA_HOME%\bin\java -jar %SCRIPT_DIR%\tools\spk-iv-cli\imagevalidation-cli-0.1.0.jar %*

endlocal