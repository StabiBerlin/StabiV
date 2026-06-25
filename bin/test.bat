@ECHO OFF 
setlocal

REM change to code parent directory
CD /D "%~dp0/../code/parent"

REM change to utf8 console
chcp 65001 > nul

REM mvn test
mvn test