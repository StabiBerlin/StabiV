@ECHO OFF 
setlocal enabledelayedexpansion

REM Get directory of this script
set SCRIPT_DIR=%~dp0

REM Build path to parent pom.xml
set PARENT_POM=%SCRIPT_DIR%..\code\parent\pom.xml

REM Collect and quote all arguments
set ARGS=
:loop
if "%~1"=="" goto done
set ARGS=!ARGS! \"%~1\"
shift
goto loop
:done

REM Run Maven
mvn -f "%PARENT_POM%" -pl ../cli -am compile exec:java -P !checkstyle,!spotless -Dexec.mainClass="de.spkberlin.stabiv.cli.entrypoint.Main" -Dexec.args="%ARGS%"

endlocal