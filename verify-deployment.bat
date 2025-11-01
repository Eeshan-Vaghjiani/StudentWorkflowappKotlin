@echo off
echo ========================================
echo Deployment Verification Script
echo ========================================
echo.

echo [1/5] Checking Firebase project...
firebase projects:list
echo.

echo [2/5] Verifying Firestore rules deployment...
firebase firestore:rules:get
echo.

echo [3/5] Checking for recent deployments...
firebase deploy:history
echo.

echo [4/5] Verifying app build status...
if exist "app\build\outputs\apk" (
    echo ✓ APK build directory exists
    dir /b app\build\outputs\apk\debug\*.apk 2>nul
    if errorlevel 1 (
        echo ✗ No APK found - app needs to be built
    ) else (
        echo ✓ APK found
    )
) else (
    echo ✗ Build directory not found - app needs to be built
)
echo.

echo [5/5] Checking test results...
if exist "app\build\test-results" (
    echo ✓ Test results directory exists
) else (
    echo ✗ No test results found
)
echo.

echo ========================================
echo Verification Complete
echo ========================================
echo.
echo Next Steps:
echo 1. Review Firestore rules above
echo 2. Check deployment history
echo 3. Build app if needed: gradlew assembleDebug
echo 4. Run tests: gradlew test
echo 5. Monitor Firebase Console for errors
echo.
pause
