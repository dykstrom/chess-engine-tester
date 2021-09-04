@echo off

setlocal

set ORIGINAL_DIR=%CD%

set SCRIPT_DIR=%~dp0
cd %SCRIPT_DIR%

if "%JAVA_HOME%" == "" goto use_path
if not exist "%JAVA_HOME%\bin\java.exe" goto use_path

:use_java_home
set JAVA_CMD=%JAVA_HOME%\bin\java.exe
goto set_args

:use_path
set JAVA_CMD=java.exe

:set_args
set JVM_ARGS=-Djava.util.logging.config.file=logging.properties
set JAR_FILE=cli-${project.version}.jar

%JAVA_CMD% %JVM_ARGS% -jar %JAR_FILE% %*

cd %ORIGINAL_DIR%

endlocal
