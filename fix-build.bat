@echo off
echo ========================================
echo Fixing Kotlin Daemon Build Issues
echo ========================================
echo.

echo Step 1: Stopping all Java/Kotlin processes...
taskkill /F /IM java.exe 2>nul
taskkill /F /IM kotlin-compiler-daemon.exe 2>nul
timeout /t 3 /nobreak >nul

echo Step 2: Stopping Gradle daemons...
call gradlew --stop
timeout /t 2 /nobreak >nul

echo Step 3: Cleaning build directories...
rmdir /s /q app\build 2>nul
rmdir /s /q build 2>nul
rmdir /s /q .gradle 2>nul
rmdir /s /q .kotlin 2>nul
timeout /t 2 /nobreak >nul

echo Step 4: Building project...
call gradlew assembleDebug --no-daemon --stacktrace

echo.
echo ========================================
echo Build process complete!
echo ========================================
pause
