@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

call :install_one jams-api
if errorlevel 1 exit /b 1
call :install_one jams-main
if errorlevel 1 exit /b 1
exit /b 0

:install_one
set "ARTIFACT=%~1"
set "JAR="
for %%f in (jams-libs\%ARTIFACT%-*.jar) do (
    if not defined JAR set "JAR=%%f"
)
if not defined JAR (
    echo Error: no %ARTIFACT%-*.jar found in jams-libs\ 1>&2
    exit /b 1
)
echo Installing %JAR% as org.jams:%ARTIFACT%:local
call mvnw.cmd -q org.apache.maven.plugins:maven-install-plugin:3.1.2:install-file -Dfile="%JAR%" -DgroupId=org.jams -DartifactId=%ARTIFACT% -Dversion=local -Dpackaging=jar -DgeneratePom=true
exit /b %ERRORLEVEL%
