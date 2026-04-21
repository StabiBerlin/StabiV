@ECHO OFF 
setlocal

REM change to code parent directory
CD /D "%~dp0/../code/parent"

REM mvn package
mvn package -Dcli