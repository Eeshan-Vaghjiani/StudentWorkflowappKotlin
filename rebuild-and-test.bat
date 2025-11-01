@echo off
echo ========================================
echo Rebuilding App with Token Refresh Fix
echo ========================================
echo.

echo Step 1: Cleaning previous build...
call gradlew.bat clean
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Clean failed!
    pause
    exit /b 1
)
echo.

echo Step 2: Building debug APK...
call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)
echo.

echo ========================================
echo Build Successful!
echo ========================================
echo.
echo Next steps:
echo 1. Uninstall the current app from your device
echo 2. Install the new APK from: app\build\outputs\apk\debug\app-debug.apk
echo 3. Open the app and sign in
echo 4. Try creating a group
echo 5. Check logs for "Authentication token refreshed successfully"
echo.
echo Or run: adb install -r app\build\outputs\apk\debug\app-debug.apk
echo.
pause
