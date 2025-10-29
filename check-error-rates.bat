@echo off
REM Error Rate Monitoring Script for TeamSync App (Windows)
REM This script provides a quick overview of error rates and app health

echo ========================================
echo TeamSync Error Rate Monitor
echo ========================================
echo.
echo Date: %date% %time%
echo.

REM Function to check Firebase Crashlytics
:check_crashlytics
echo ========================================
echo 1. Firebase Crashlytics Status
echo ========================================
echo.
echo Opening Firebase Crashlytics in browser...
echo URL: https://console.firebase.google.com/project/_/crashlytics
echo.
echo Check these metrics:
echo   - Crash-free users (target: ^> 99%%)
echo   - Crash-free sessions (target: ^> 99.5%%)
echo   - Total crashes (trend should be decreasing)
echo   - New issues (should be minimal)
echo.

start https://console.firebase.google.com/project/_/crashlytics

pause
echo.

REM Function to check Firebase Performance
:check_performance
echo ========================================
echo 2. Firebase Performance Monitoring
echo ========================================
echo.
echo Opening Firebase Performance in browser...
echo URL: https://console.firebase.google.com/project/_/performance
echo.
echo Check these metrics:
echo   - App startup time (target: ^< 2 seconds)
echo   - Screen rendering time (target: ^< 1 second)
echo   - Network request duration (target: ^< 1 second)
echo   - Success/failure rates (target: ^> 95%% success)
echo.

start https://console.firebase.google.com/project/_/performance

pause
echo.

REM Function to check Play Console
:check_play_console
echo ========================================
echo 3. Google Play Console Vitals
echo ========================================
echo.
echo Opening Play Console in browser...
echo URL: https://play.google.com/console
echo.
echo Navigate to: Your App -^> Quality -^> Android vitals
echo.
echo Check these metrics:
echo   - Crash rate (target: ^< 1%%)
echo   - ANR rate (target: ^< 0.5%%)
echo   - Excessive wakeups
echo   - Stuck wake locks
echo.

start https://play.google.com/console

pause
echo.

REM Function to check Firebase Analytics
:check_analytics
echo ========================================
echo 4. Firebase Analytics
echo ========================================
echo.
echo Opening Firebase Analytics in browser...
echo URL: https://console.firebase.google.com/project/_/analytics
echo.
echo Check these metrics:
echo   - Active users (trend should be stable/growing)
echo   - User retention (Day 1: ^> 40%%, Day 7: ^> 20%%)
echo   - Event counts (verify key events are firing)
echo   - Conversion funnels (identify drop-off points)
echo.

start https://console.firebase.google.com/project/_/analytics

pause
echo.

REM Function to collect manual input
:collect_metrics
echo ========================================
echo 5. Record Current Metrics
echo ========================================
echo.
echo Please enter the following metrics:
echo.

set /p CRASH_FREE_USERS="Crash-free users (%%): "
set /p TOTAL_CRASHES="Total crashes (last 24h): "
set /p PERMISSION_ERRORS="Permission denied errors (last 24h): "
set /p API_FAILURES="API failures (last 24h): "
set /p NETWORK_ERRORS="Network errors (last 24h): "
set /p NEW_ISSUES="New issues opened: "
set /p RESOLVED_ISSUES="Issues resolved: "

echo.

REM Function to analyze metrics
:analyze_metrics
echo ========================================
echo 6. Metrics Analysis
echo ========================================
echo.

REM Analyze crash-free users (simplified for Windows)
if %CRASH_FREE_USERS% GEQ 99 (
    echo [GOOD] Crash-free users: %CRASH_FREE_USERS%%
) else if %CRASH_FREE_USERS% GEQ 98 (
    echo [WARNING] Crash-free users: %CRASH_FREE_USERS%%
) else (
    echo [CRITICAL] Crash-free users: %CRASH_FREE_USERS%%
)

REM Analyze total crashes
if %TOTAL_CRASHES% LSS 10 (
    echo [GOOD] Total crashes: %TOTAL_CRASHES%
) else if %TOTAL_CRASHES% LSS 50 (
    echo [WARNING] Total crashes: %TOTAL_CRASHES%
) else (
    echo [CRITICAL] Total crashes: %TOTAL_CRASHES%
)

REM Analyze permission errors
if %PERMISSION_ERRORS% LSS 10 (
    echo [GOOD] Permission errors: %PERMISSION_ERRORS%
) else if %PERMISSION_ERRORS% LSS 50 (
    echo [WARNING] Permission errors: %PERMISSION_ERRORS%
) else (
    echo [CRITICAL] Permission errors: %PERMISSION_ERRORS%
)

REM Analyze API failures
if %API_FAILURES% LSS 10 (
    echo [GOOD] API failures: %API_FAILURES%
) else if %API_FAILURES% LSS 50 (
    echo [WARNING] API failures: %API_FAILURES%
) else (
    echo [CRITICAL] API failures: %API_FAILURES%
)

REM Analyze network errors
if %NETWORK_ERRORS% LSS 50 (
    echo [GOOD] Network errors: %NETWORK_ERRORS%
) else if %NETWORK_ERRORS% LSS 100 (
    echo [WARNING] Network errors: %NETWORK_ERRORS%
) else (
    echo [CRITICAL] Network errors: %NETWORK_ERRORS%
)

echo.
echo Issue tracking:
echo   New issues: %NEW_ISSUES%
echo   Resolved issues: %RESOLVED_ISSUES%
echo.

REM Function to generate recommendations
:generate_recommendations
echo ========================================
echo 7. Recommendations
echo ========================================
echo.

set CRITICAL_ISSUES=0

if %CRASH_FREE_USERS% LSS 98 (
    echo [CRITICAL] Crash rate too high
    echo   -^> Investigate top crash issues immediately
    echo   -^> Prepare hotfix if needed
    echo.
    set /a CRITICAL_ISSUES+=1
)

if %PERMISSION_ERRORS% GTR 50 (
    echo [CRITICAL] High permission error rate
    echo   -^> Review Firestore security rules
    echo   -^> Check for recent rule changes
    echo   -^> Consider rollback if needed
    echo.
    set /a CRITICAL_ISSUES+=1
)

if %API_FAILURES% GTR 50 (
    echo [CRITICAL] High API failure rate
    echo   -^> Check Gemini AI service status
    echo   -^> Verify API configuration
    echo   -^> Review error logs
    echo.
    set /a CRITICAL_ISSUES+=1
)

if %CRITICAL_ISSUES% EQU 0 (
    echo [GOOD] No critical issues detected
    echo.
    echo Routine maintenance:
    echo   -^> Continue daily monitoring
    echo   -^> Review and triage new issues
    echo   -^> Plan fixes for next release
)

echo.

REM Function to save report
:save_report
echo ========================================
echo 8. Save Report
echo ========================================
echo.

set TIMESTAMP=%date:~-4%%date:~-10,2%%date:~-7,2%-%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
set REPORT_FILE=error-monitoring-report-%TIMESTAMP%.txt

(
    echo TeamSync Error Monitoring Report
    echo Date: %date% %time%
    echo.
    echo Metrics:
    echo   Crash-free users: %CRASH_FREE_USERS%%%
    echo   Total crashes: %TOTAL_CRASHES%
    echo   Permission errors: %PERMISSION_ERRORS%
    echo   API failures: %API_FAILURES%
    echo   Network errors: %NETWORK_ERRORS%
    echo   New issues: %NEW_ISSUES%
    echo   Resolved issues: %RESOLVED_ISSUES%
    echo.
) > %REPORT_FILE%

echo Report saved to: %REPORT_FILE%
echo.

REM Summary
echo ========================================
echo Monitoring Complete
echo ========================================
echo.
echo Next steps:
echo 1. Address any critical issues immediately
echo 2. Triage new issues within 24 hours
echo 3. Update issue tracker
echo 4. Schedule fixes for next release
echo 5. Run this script again tomorrow
echo.

pause
