@echo off
REM Validation Log Analysis Script for Windows
REM Analyzes Android logs for validation errors and NullPointerException

echo ========================================
echo Validation Log Analysis
echo ========================================
echo.
echo Date: %date% %time%
echo.

REM Check if ADB is available
where adb >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: ADB not found in PATH
    echo Please install Android SDK Platform Tools
    echo.
    pause
    exit /b 1
)

REM Check if device is connected
adb devices | findstr "device$" >nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: No Android device connected
    echo Please connect a device or start an emulator
    echo.
    pause
    exit /b 1
)

echo Device connected. Analyzing logs...
echo.

REM Create temporary file for log analysis
set TEMP_LOG=%TEMP%\adb_log_analysis.txt
adb logcat -d > %TEMP_LOG%

echo ========================================
echo 1. Validation Error Analysis
echo ========================================
echo.

REM Count validation rejections (expected behavior)
findstr /C:"Invalid message:" %TEMP_LOG% > nul
if %ERRORLEVEL% EQU 0 (
    for /f %%a in ('findstr /C:"Invalid message:" %TEMP_LOG% ^| find /c /v ""') do set VALIDATION_REJECTIONS=%%a
) else (
    set VALIDATION_REJECTIONS=0
)
echo Validation rejections: %VALIDATION_REJECTIONS%

REM Count null message attempts (expected behavior)
findstr /C:"Attempted to queue null message" %TEMP_LOG% > nul
if %ERRORLEVEL% EQU 0 (
    for /f %%a in ('findstr /C:"Attempted to queue null message" %TEMP_LOG% ^| find /c /v ""') do set NULL_ATTEMPTS=%%a
) else (
    set NULL_ATTEMPTS=0
)
echo Null message attempts: %NULL_ATTEMPTS%

REM Count blank field rejections
findstr /C:"Message ID is blank" %TEMP_LOG% > nul
if %ERRORLEVEL% EQU 0 (
    for /f %%a in ('findstr /C:"Message ID is blank" %TEMP_LOG% ^| find /c /v ""') do set BLANK_ID=%%a
) else (
    set BLANK_ID=0
)
echo Blank ID rejections: %BLANK_ID%

findstr /C:"Chat ID is blank" %TEMP_LOG% > nul
if %ERRORLEVEL% EQU 0 (
    for /f %%a in ('findstr /C:"Chat ID is blank" %TEMP_LOG% ^| find /c /v ""') do set BLANK_CHAT_ID=%%a
) else (
    set BLANK_CHAT_ID=0
)
echo Blank Chat ID rejections: %BLANK_CHAT_ID%

findstr /C:"Sender ID is blank" %TEMP_LOG% > nul
if %ERRORLEVEL% EQU 0 (
    for /f %%a in ('findstr /C:"Sender ID is blank" %TEMP_LOG% ^| find /c /v ""') do set BLANK_SENDER_ID=%%a
) else (
    set BLANK_SENDER_ID=0
)
echo Blank Sender ID rejections: %BLANK_SENDER_ID%

findstr /C:"Message has no content" %TEMP_LOG% > nul
if %ERRORLEVEL% EQU 0 (
    for /f %%a in ('findstr /C:"Message has no content" %TEMP_LOG% ^| find /c /v ""') do set NO_CONTENT=%%a
) else (
    set NO_CONTENT=0
)
echo No content rejections: %NO_CONTENT%

echo.

echo ========================================
echo 2. NullPointerException Analysis
echo ========================================
echo.

REM Count NullPointerException (unexpected - should be 0)
findstr /C:"NullPointerException" %TEMP_LOG% > nul
if %ERRORLEVEL% EQU 0 (
    for /f %%a in ('findstr /C:"NullPointerException" %TEMP_LOG% ^| find /c /v ""') do set NPE_COUNT=%%a
) else (
    set NPE_COUNT=0
)

if %NPE_COUNT% GTR 0 (
    echo [WARNING] NullPointerException detected: %NPE_COUNT%
    echo.
    echo Recent NullPointerException entries:
    echo ----------------------------------------
    findstr /C:"NullPointerException" %TEMP_LOG% | findstr /N "^" | findstr /E ":[0-9]*:.*" > nul
    for /f "tokens=*" %%a in ('findstr /C:"NullPointerException" %TEMP_LOG%') do (
        echo %%a
    )
    echo.
) else (
    echo [GOOD] No NullPointerException detected
    echo.
)

echo ========================================
echo 3. Chat-Related Error Analysis
echo ========================================
echo.

REM Count ChatRepository errors
findstr /C:"ChatRepository" %TEMP_LOG% | findstr /C:"Error" > nul
if %ERRORLEVEL% EQU 0 (
    for /f %%a in ('findstr /C:"ChatRepository" %TEMP_LOG% ^| findstr /C:"Error" ^| find /c /v ""') do set CHAT_REPO_ERRORS=%%a
) else (
    set CHAT_REPO_ERRORS=0
)
echo ChatRepository errors: %CHAT_REPO_ERRORS%

REM Count OfflineMessageQueue errors
findstr /C:"OfflineMessageQueue" %TEMP_LOG% | findstr /C:"Error" > nul
if %ERRORLEVEL% EQU 0 (
    for /f %%a in ('findstr /C:"OfflineMessageQueue" %TEMP_LOG% ^| findstr /C:"Error" ^| find /c /v ""') do set QUEUE_ERRORS=%%a
) else (
    set QUEUE_ERRORS=0
)
echo OfflineMessageQueue errors: %QUEUE_ERRORS%

REM Count ChatRoomViewModel errors
findstr /C:"ChatRoomViewModel" %TEMP_LOG% | findstr /C:"Error" > nul
if %ERRORLEVEL% EQU 0 (
    for /f %%a in ('findstr /C:"ChatRoomViewModel" %TEMP_LOG% ^| findstr /C:"Error" ^| find /c /v ""') do set VIEWMODEL_ERRORS=%%a
) else (
    set VIEWMODEL_ERRORS=0
)
echo ChatRoomViewModel errors: %VIEWMODEL_ERRORS%

echo.

echo ========================================
echo 4. Summary and Assessment
echo ========================================
echo.

set /a TOTAL_VALIDATIONS=%VALIDATION_REJECTIONS% + %NULL_ATTEMPTS% + %BLANK_ID% + %BLANK_CHAT_ID% + %BLANK_SENDER_ID% + %NO_CONTENT%
echo Total validation events: %TOTAL_VALIDATIONS%
echo Total NullPointerException: %NPE_COUNT%
echo Total chat errors: %CHAT_REPO_ERRORS% + %QUEUE_ERRORS% + %VIEWMODEL_ERRORS%
echo.

REM Assessment
echo Assessment:
echo -----------

if %NPE_COUNT% EQU 0 (
    echo [GOOD] No NullPointerException detected
) else (
    echo [CRITICAL] NullPointerException found - investigate immediately!
)

if %TOTAL_VALIDATIONS% GTR 0 (
    echo [INFO] Validation system is working - rejecting invalid messages
) else (
    echo [INFO] No validation events detected
)

set /a TOTAL_ERRORS=%CHAT_REPO_ERRORS% + %QUEUE_ERRORS% + %VIEWMODEL_ERRORS%
if %TOTAL_ERRORS% GTR 10 (
    echo [WARNING] High error count - review logs for patterns
) else if %TOTAL_ERRORS% GTR 0 (
    echo [INFO] Some errors detected - normal operation
) else (
    echo [GOOD] No errors detected
)

echo.

echo ========================================
echo 5. Recommendations
echo ========================================
echo.

if %NPE_COUNT% GTR 0 (
    echo [ACTION REQUIRED]
    echo 1. Review NullPointerException stack traces above
    echo 2. Identify the source of null values
    echo 3. Add additional null checks if needed
    echo 4. Test fix thoroughly before deploying
    echo.
)

if %TOTAL_VALIDATIONS% GTR 100 (
    echo [REVIEW NEEDED]
    echo 1. High validation rejection rate detected
    echo 2. Review data quality from Firestore
    echo 3. Check client-side message creation
    echo 4. Consider if validation rules are too strict
    echo.
)

if %TOTAL_ERRORS% GTR 20 (
    echo [MONITORING NEEDED]
    echo 1. Elevated error count detected
    echo 2. Review error patterns in full logs
    echo 3. Check for specific error types
    echo 4. Monitor trends over time
    echo.
)

if %NPE_COUNT% EQU 0 (
    if %TOTAL_ERRORS% LSS 10 (
        echo [ALL CLEAR]
        echo System is operating normally
        echo Continue regular monitoring
        echo.
    )
)

echo ========================================
echo 6. Save Report
echo ========================================
echo.

set TIMESTAMP=%date:~-4%%date:~-10,2%%date:~-7,2%-%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
set REPORT_FILE=validation-analysis-report-%TIMESTAMP%.txt

(
    echo Validation Log Analysis Report
    echo Date: %date% %time%
    echo.
    echo Validation Events:
    echo   Validation rejections: %VALIDATION_REJECTIONS%
    echo   Null message attempts: %NULL_ATTEMPTS%
    echo   Blank ID rejections: %BLANK_ID%
    echo   Blank Chat ID rejections: %BLANK_CHAT_ID%
    echo   Blank Sender ID rejections: %BLANK_SENDER_ID%
    echo   No content rejections: %NO_CONTENT%
    echo   Total: %TOTAL_VALIDATIONS%
    echo.
    echo Error Analysis:
    echo   NullPointerException: %NPE_COUNT%
    echo   ChatRepository errors: %CHAT_REPO_ERRORS%
    echo   OfflineMessageQueue errors: %QUEUE_ERRORS%
    echo   ChatRoomViewModel errors: %VIEWMODEL_ERRORS%
    echo.
) > %REPORT_FILE%

echo Report saved to: %REPORT_FILE%
echo.

REM Cleanup
del %TEMP_LOG%

echo ========================================
echo Analysis Complete
echo ========================================
echo.
echo Next steps:
echo 1. Review any critical issues identified
echo 2. Monitor trends over time
echo 3. Run this script daily during monitoring period
echo 4. Compare results with previous runs
echo.

pause
