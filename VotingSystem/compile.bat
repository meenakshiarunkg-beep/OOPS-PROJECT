@echo off
REM Compiles all Java sources into the bin\ folder, using the MySQL driver jar in lib\
if not exist bin mkdir bin

setlocal enabledelayedexpansion
set CP=lib\*

for /r src %%f in (*.java) do set FILES=!FILES! "%%f"

javac -d bin -cp "%CP%" %FILES%

if %errorlevel% neq 0 (
    echo.
    echo Compilation failed - see errors above.
    exit /b 1
)

echo.
echo Compiled successfully into bin\
