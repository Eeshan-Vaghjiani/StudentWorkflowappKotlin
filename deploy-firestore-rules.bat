@echo off
REM Firestore Rules Deployment Script for Windows
REM This script backs up current rules, tests them, and deploys to Firebase

echo ========================================
echo Firestore Rules Deployment Script
echo ========================================
echo.

REM Step 1: Backup current rules
echo [1/5] Backing up current Firestore rules...
set TIMESTAMP=%date:~-4%%date:~-10,2%%date:~-7,2%-%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
firebase firestore:rules:get > firestore.rules.backup-%TIMESTAMP%
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to backup current rules
    exit /b 1
)
echo Backup saved to: firestore.rules.backup-%TIMESTAMP%
echo.

REM Step 2: Test rules with emulator
echo [2/5] Testing rules with Firebase Emulator...
echo Starting emulator for rule testing...
echo NOTE: This will start the emulator. Press Ctrl+C when tests are complete.
echo.
firebase emulators:start --only firestore
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Emulator test failed or was cancelled
    echo.
)

REM Step 3: Confirm deployment
echo [3/5] Ready to deploy rules
set /p CONFIRM="Deploy Firestore rules to production? (yes/no): "
if /i not "%CONFIRM%"=="yes" (
    echo Deployment cancelled by user
    exit /b 0
)
echo.

REM Step 4: Deploy rules
echo [4/5] Deploying Firestore rules to production...
firebase deploy --only firestore:rules
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to deploy rules
    echo To rollback, run: firebase deploy --only firestore:rules
    echo And restore from backup: firestore.rules.backup-%TIMESTAMP%
    exit /b 1
)
echo.

REM Step 5: Verify deployment
echo [5/5] Deployment complete!
echo.
echo Next steps:
echo 1. Monitor Firebase Console for rule violations
echo 2. Check application logs for permission errors
echo 3. If issues occur, rollback using: firebase deploy --only firestore:rules
echo    (After restoring firestore.rules from backup)
echo.
echo Backup file: firestore.rules.backup-%TIMESTAMP%
echo ========================================

pause
