@echo off
REM Verification script for User Profile Firestore Rules
REM This script helps verify the deployed rules are working correctly

echo ========================================
echo User Profile Rules Verification
echo ========================================
echo.
echo Project: android-logreg
echo Backup: firestore.rules.backup-20251031-155957
echo.
echo ========================================
echo Verification Steps:
echo ========================================
echo.
echo 1. Check Firebase Console:
echo    https://console.firebase.google.com/project/android-logreg/firestore
echo.
echo 2. Monitor for PERMISSION_DENIED errors
echo.
echo 3. Test with user accounts:
echo    - Sign in with new account (profile should be created)
echo    - Sign in with existing account (lastActive should update)
echo    - Try to start a chat (should work without errors)
echo.
echo 4. Run automated tests:
echo    cd firestore-rules-tests
echo    npm test
echo.
echo ========================================
echo Current Rules Status:
echo ========================================
echo.

firebase projects:list

echo.
echo ========================================
echo To rollback if needed:
echo ========================================
echo copy firestore.rules.backup-20251031-155957 firestore.rules
echo firebase deploy --only firestore:rules
echo.
echo Press any key to open Firebase Console...
pause > nul
start https://console.firebase.google.com/project/android-logreg/firestore
