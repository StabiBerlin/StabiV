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

### Packaging for Windows

When packaging everything as a zip file, all java and exiftool files from `%JAVA_HOME%` and `%EXIFTOOL_HOME%` are copied into the bundle. Make sure to use a version that is appropriate when creating the final bundle.

## Linux

In order to develop this tool on Linux, download and install the following software:

- [OpenJDK 21 or later](https://jdk.java.net/26/)
- [Maven 3](https://maven.apache.org/download.cgi)
- [Exiftool](https://exiftool.org/)

You should be able to install everything from your favorite package manager:

- Ubuntu

  ```
  apt-get install default-jdk maven exiftool
  ```

- Fedora
  ```
  dnf install java-latest-openjdk maven exiftool
  ```

Alternatively, you can use the Dockerfile located in the directory `deployment/development`. It contains all relevant tools.

### Packaging For Linux

In order to bundle everything into one zip file, two things need to be done manually:

1. Download a Linux build for [OpenJDK](https://jdk.java.net/26/) and unpack everything into the directory `code/cli/opt/java` such that the Java binary is located at `code/cli/opt/java/bin/java.`

2. Install and set up "podman" or "docker" and start the build script `code/extractor-exiftool/src/main/exiftool/build.sh`. This script will build a dynamically linked binary of `exiftool` to be included in the final bundle. Make sure to use an appropriate old or new distribution to build a binary that works on most Linux distributions.

Now, when running `bash bin/package.sh`, both Java and exiftool is included in the final zip file.

## Code Formatting

This repository uses `spotless` to automatically format code according to coding guidlines.
