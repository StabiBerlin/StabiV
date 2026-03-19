# Development Documentation

## Windows

In order to develop this tool in Windows, download and install the following software:

- [OpenJDK 21 or later](https://jdk.java.net/26/)
- [Maven 3](https://maven.apache.org/download.cgi)
- [Exiftool](https://exiftool.org/)

Make sure that Java, Maven and Exiftool are available on the command line:

1. Press `Windows+R`, type `systempropertiesadvanced`, press Enter
2. Edit environment variables and create

- `JAVA_HOME` with the directory of your Java installation, e.g. `C:\Path\To\jdk-21`
- `MAVEN_HOME` with the directory of your Maven installation, e.g. `C:\Path\To\apache-maven-3.9.14`
- `EXIFTOOL_HOME` with the directory of your Exiftool installation, e.g. `C:\Path\To\exiftool-13.52_64`

3. Edit the `Path` variable and add the respective directories containing binary files
   - for Java: `%JAVA_HOME%\bin`
   - for Maven: `%MAVEN_HOME%\bin`
   - for Exiftool: `%EXIFTOOL_HOME%`
4. Rename `exiftool(-k).exe` to `exiftool.exe`
5. Check that all commands can be executed from the command prompt:

- for Java: type in `java -version`
- for Maven: type in `mvn --version`
- for Exiftool: type in `exiftool -ver`

## Code Formatting

This repository uses `spotless` to automatically format code according to coding guidlines.
