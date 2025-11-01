@echo off
REM Android App Release Preparation Script for Windows
REM This script prepares the app for release by incrementing version and generating builds

echo ========================================
echo TeamSync App Release Preparation
echo ========================================
echo.

REM Configuration
set BUILD_GRADLE=app\build.gradle.kts

REM Extract current version (simplified for Windows)
findstr /C:"versionCode = " %BUILD_GRADLE% > temp_version.txt
set /p VERSION_LINE=<temp_version.txt
for /f "tokens=3 delims== " %%a in ("%VERSION_LINE%") do set CURRENT_VERSION_CODE=%%a
del temp_version.txt

findstr /C:"versionName = " %BUILD_GRADLE% > temp_version.txt
set /p VERSION_LINE=<temp_version.txt
for /f "tokens=3 delims==" %%a in ("%VERSION_LINE%") do set CURRENT_VERSION_NAME=%%a
set CURRENT_VERSION_NAME=%CURRENT_VERSION_NAME:"=%
set CURRENT_VERSION_NAME=%CURRENT_VERSION_NAME: =%
del temp_version.txt

echo Current Version:
echo   Version Code: %CURRENT_VERSION_CODE%
echo   Version Name: %CURRENT_VERSION_NAME%
echo.

REM Step 1: Increment version
echo [1/6] Increment version numbers
echo.
echo Version increment options:
echo   1) Patch (1.0.0 -^> 1.0.1) - Bug fixes
echo   2) Minor (1.0.0 -^> 1.1.0) - New features
echo   3) Major (1.0.0 -^> 2.0.0) - Breaking changes
echo   4) Custom version
echo   5) Keep current version
echo.
set /p VERSION_OPTION="Select option (1-5): "

if "%VERSION_OPTION%"=="1" (
    REM Patch increment - simplified
    set NEW_VERSION_NAME=%CURRENT_VERSION_NAME%
    echo Enter new patch version manually ^(e.g., 1.0.1^):
    set /p NEW_VERSION_NAME=
) else if "%VERSION_OPTION%"=="2" (
    REM Minor increment - simplified
    set NEW_VERSION_NAME=%CURRENT_VERSION_NAME%
    echo Enter new minor version manually ^(e.g., 1.1.0^):
    set /p NEW_VERSION_NAME=
) else if "%VERSION_OPTION%"=="3" (
    REM Major increment - simplified
    set NEW_VERSION_NAME=%CURRENT_VERSION_NAME%
    echo Enter new major version manually ^(e.g., 2.0.0^):
    set /p NEW_VERSION_NAME=
) else if "%VERSION_OPTION%"=="4" (
    REM Custom version
    set /p NEW_VERSION_NAME="Enter new version name (e.g., 1.2.0): "
) else if "%VERSION_OPTION%"=="5" (
    REM Keep current
    set NEW_VERSION_NAME=%CURRENT_VERSION_NAME%
) else (
    echo Invalid option. Exiting.
    exit /b 1
)

set /a NEW_VERSION_CODE=%CURRENT_VERSION_CODE%+1

echo.
echo New Version:
echo   Version Code: %NEW_VERSION_CODE%
echo   Version Name: %NEW_VERSION_NAME%
echo.
set /p CONFIRM="Proceed with version update? (yes/no): "
if /i not "%CONFIRM%"=="yes" (
    echo Version update cancelled
    exit /b 0
)

REM Update build.gradle.kts (manual edit recommended for Windows)
echo.
echo Please manually update %BUILD_GRADLE%:
echo   Change versionCode to: %NEW_VERSION_CODE%
echo   Change versionName to: "%NEW_VERSION_NAME%"
echo.
pause
echo.

REM Step 2: Clean build
echo [2/6] Cleaning previous builds...
call gradlew.bat clean
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Clean failed
    exit /b 1
)
echo.

REM Step 3: Run tests
echo [3/6] Running unit tests...
set /p RUN_TESTS="Run tests before building? (yes/no): "
if /i "%RUN_TESTS%"=="yes" (
    call gradlew.bat test
    if %ERRORLEVEL% NEQ 0 (
        echo WARNING: Tests failed. Continue anyway? ^(yes/no^)
        set /p CONTINUE=
        if /i not "%CONTINUE%"=="yes" (
            exit /b 1
        )
    )
)
echo.

REM Step 4: Build release APK
echo [4/6] Building release APK...
call gradlew.bat assembleRelease
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Release build failed
    exit /b 1
)
echo Release APK built successfully
echo.

REM Step 5: Build release AAB (for Play Store)
echo [5/6] Building release AAB (Android App Bundle)...
call gradlew.bat bundleRelease
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Bundle build failed
    exit /b 1
)
echo Release AAB built successfully
echo.

REM Step 6: Summary
echo [6/6] Build Summary
echo ========================================
echo Version: %NEW_VERSION_NAME% (Code: %NEW_VERSION_CODE%)
echo.
echo Build artifacts:
echo   APK: app\build\outputs\apk\release\app-release.apk
echo   AAB: app\build\outputs\bundle\release\app-release.aab
echo.
echo Next steps:
echo 1. Test APK on multiple devices
echo 2. Upload AAB to Google Play Console
echo 3. Deploy to internal testing track first
echo 4. Monitor for crashes and errors
echo 5. Promote to production after validation
echo.
echo Note: Ensure signing configuration is set up for production release
echo ========================================

pause
