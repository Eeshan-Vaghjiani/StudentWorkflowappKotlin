@echo off
echo Starting Firebase Emulator and running tests...
echo.
echo Step 1: Starting Firebase Emulator in background...
start /B firebase emulators:start --only firestore --project test-project

echo Waiting for emulator to start (10 seconds)...
timeout /t 10 /nobreak > nul

echo.
echo Step 2: Running tests...
call npm test

echo.
echo Step 3: Stopping emulator...
taskkill /F /IM java.exe /FI "WINDOWTITLE eq Firebase*" 2>nul

echo.
echo Done!
pause
