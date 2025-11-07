@echo off
REM TeamSync Critical Issues Verification Script
REM This script helps verify that all critical fixes are in place

echo ========================================
echo TeamSync Critical Issues Verification
echo ========================================
echo.

echo [1/5] Checking Firestore Rules...
if exist firestore.rules (
    echo [OK] firestore.rules file exists
    findstr /C:"isAuthenticated()" firestore.rules >nul
    if %ERRORLEVEL% EQU 0 (
        echo [OK] Authentication helper functions found
    ) else (
        echo [WARNING] Authentication helpers not found
    )
) else (
    echo [ERROR] firestore.rules file not found!
)
echo.

echo [2/5] Checking Login Navigation...
findstr /C:"Intent.FLAG_ACTIVITY_NEW_TASK" app\src\main\java\com\example\loginandregistration\Login.kt >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Login navigation flags configured correctly
) else (
    echo [WARNING] Login navigation may need review
)
echo.

echo [3/5] Checking Background Thread Usage...
findstr /C:"withContext(Dispatchers.IO)" app\src\main\java\com\example\loginandregistration\repository\*.kt >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Background dispatchers found in repositories
) else (
    echo [WARNING] Background thread usage may need review
)
echo.

echo [4/5] Checking Message Model...
findstr /C:"val id: String = \"\"" app\src\main\java\com\example\loginandregistration\models\Message.kt >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] Message model has default values (GSON-compatible)
) else (
    echo [WARNING] Message model may need default values
)
echo.

echo [5/5] Checking UserProfile Annotation...
findstr /C:"@IgnoreExtraProperties" app\src\main\java\com\example\loginandregistration\models\UserProfile.kt >nul
if %ERRORLEVEL% EQU 0 (
    echo [OK] UserProfile has @IgnoreExtraProperties annotation
) else (
    echo [WARNING] UserProfile missing @IgnoreExtraProperties annotation
)
echo.

echo ========================================
echo Verification Complete
echo ========================================
echo.
echo Next Steps:
echo 1. Deploy Firestore rules: firebase deploy --only firestore:rules
echo 2. Build and test the app
echo 3. Monitor logs for any remaining issues
echo 4. Review CRITICAL_ISSUES_ANALYSIS.md for details
echo.
pause
