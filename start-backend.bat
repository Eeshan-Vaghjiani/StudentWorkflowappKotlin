@echo off
echo Starting Student Workflow Application...
echo.
echo Killing any existing processes...
taskkill /F /IM java.exe 2>nul
taskkill /F /IM gradle.exe 2>nul

echo.
echo Starting backend server...
echo Server will run on: http://localhost:8081
echo.
echo Press Ctrl+C to stop the server
echo.

gradlew.bat :backend:run
