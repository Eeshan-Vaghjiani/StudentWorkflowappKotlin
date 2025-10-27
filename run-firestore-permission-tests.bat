@echo off
REM Firestore Permissions Integration Test Runner
REM Task 10: Test App with Updated Rules

echo ========================================
echo Firestore Permissions Integration Tests
echo ========================================
echo.

REM Check if device is connected
echo Checking for connected devices...
adb devices
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: ADB not found. Please ensure Android SDK is installed.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Running Integration Tests
echo ========================================
echo.

REM Run the integration tests
echo Running FirestorePermissionsIntegrationTest...
call gradlew.bat connectedAndroidTest --tests "com.example.loginandregistration.FirestorePermissionsIntegrationTest"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================
    echo TESTS FAILED
    echo ========================================
    echo.
    echo Please check the test results in:
    echo app\build\reports\androidTests\connected\index.html
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo TESTS PASSED
echo ========================================
echo.
echo Test results available at:
echo app\build\reports\androidTests\connected\index.html
echo.
echo Opening test report...
start app\build\reports\androidTests\connected\index.html

pause
