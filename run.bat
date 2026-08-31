@echo off
setlocal

where javac >nul 2>nul
if errorlevel 1 (
  echo Java compiler not found. Install JDK 17 or newer and reopen this window.
  pause
  exit /b 1
)

if not exist out mkdir out
javac --add-modules jdk.httpserver -d out src\main\java\com\melanie\cen4802\App.java
if errorlevel 1 exit /b 1

echo.
echo Open http://localhost:8080 in your browser.
java --add-modules jdk.httpserver -cp out com.melanie.cen4802.App
