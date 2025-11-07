@echo off
echo ========================================
echo Rebuilding and Installing App
echo ========================================
echo.

echo Step 1: Cleaning project...
call gradlew clean
if errorlevel 1 (
    echo ERROR: Clean failed!
    pause
    exit /b 1
)

echo.
echo Step 2: Building debug APK...
call gradlew assembleDebug
if errorlevel 1 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo Step 3: Installing on device/emulator...
call gradlew installDebug
if errorlevel 1 (
    echo ERROR: Install failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo SUCCESS! App rebuilt and installed.
echo ========================================
echo.
echo Now try creating a group in the app.
echo.
pause
