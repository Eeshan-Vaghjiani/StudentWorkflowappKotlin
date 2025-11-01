@echo off
echo ========================================
echo Testing Critical Fixes
echo ========================================
echo.

echo Step 1: Checking for UserProfile errors...
adb logcat -d | findstr /C:"Found conflicting getters" > nul
if %errorlevel% equ 0 (
    echo [FAIL] UserProfile serialization error still present
) else (
    echo [PASS] No UserProfile serialization errors
)
echo.

echo Step 2: Checking for permission errors...
adb logcat -d | findstr /C:"PERMISSION_DENIED" | findstr /C:"createGroup" > nul
if %errorlevel% equ 0 (
    echo [FAIL] Group creation permission errors still present
) else (
    echo [PASS] No group creation permission errors
)
echo.

echo Step 3: Checking if chat is working...
adb logcat -d | findstr /C:"Your profile is not set up" > nul
if %errorlevel% equ 0 (
    echo [FAIL] Chat profile errors still present
) else (
    echo [PASS] No chat profile errors
)
echo.

echo Step 4: Checking if groups are loading...
adb logcat -d | findstr /C:"Received.*groups from Firestore"
echo.

echo Step 5: Checking if messages can be sent...
adb logcat -d | findstr /C:"sendMessage"
echo.

echo ========================================
echo Test Complete
echo ========================================
echo.
echo If you see PASS for all tests, the fixes are working!
echo If you see FAIL, please rebuild and reinstall the app.
pause
