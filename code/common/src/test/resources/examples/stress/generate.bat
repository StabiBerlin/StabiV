@echo off
setlocal EnableDelayedExpansion

REM change to script directory
CD /D "%~dp0"

REM read script arguments
set "SOURCE_IMAGE=%~1"
set "NUMBER_OF_DIRECTORIES=%~2"
set "NUMBER_OF_FILES_PER_DIRECTORY=%~3"

REM check script arguments
if "%SOURCE_IMAGE%"=="" (
    echo "you need to specify the path to the source image as first argument"
    exit /b 1
)

if "%NUMBER_OF_DIRECTORIES%"=="" (
    echo "you need to specify the number of directories that are generated as second argument"
    exit /b 1
)

if "%NUMBER_OF_FILES_PER_DIRECTORY%"=="" (
    echo "you need to specify the number of files that are generated for each directory as third argument"
    exit /b 1
)

REM clean generated directory
if exist generated (
    rmdir /s /q generated
)
mkdir generated

REM copy files to directories
for /L %%i in (1,1,%NUMBER_OF_DIRECTORIES%) do (
    
    REM zero-pad directory number to 5 digits
    set "DIRNUM=00000%%i"
    set "DIRNUM=!DIRNUM:~-5!"

    echo create directory !DIRNUM!

    mkdir "generated\!DIRNUM!"

    for /L %%j in (1,1,%NUMBER_OF_FILES_PER_DIRECTORY%) do (

        REM zero-pad file number to 8 digits
        set "FILENUM=00000000%%j"
        set "FILENUM=!FILENUM:~-8!"

        copy "%SOURCE_IMAGE%" "generated\!DIRNUM!\!FILENUM!.tif" >nul
    )
)

endlocal