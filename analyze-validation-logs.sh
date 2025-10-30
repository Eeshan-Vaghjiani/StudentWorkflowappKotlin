#!/bin/bash
# Validation Log Analysis Script
# Analyzes Android logs for validation errors and NullPointerException

echo "========================================"
echo "Validation Log Analysis"
echo "========================================"
echo ""
echo "Date: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""

# Colors for output
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if ADB is available
if ! command -v adb &> /dev/null; then
    echo -e "${RED}ERROR:${NC} ADB not found in PATH"
    echo "Please install Android SDK Platform Tools"
    exit 1
fi

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo -e "${RED}ERROR:${NC} No Android device connected"
    echo "Please connect a device or start an emulator"
    exit 1
fi

echo -e "${GREEN}✓${NC} Device connected. Analyzing logs..."
echo ""

# Get logs
TEMP_LOG=$(mktemp)
adb logcat -d > "$TEMP_LOG"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1. Validation Error Analysis"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Count validation rejections (expected behavior)
VALIDATION_REJECTIONS=$(grep -c "Invalid message:" "$TEMP_LOG" || echo "0")
echo "Validation rejections: $VALIDATION_REJECTIONS"

# Count null message attempts (expected behavior)
NULL_ATTEMPTS=$(grep -c "Attempted to queue null message" "$TEMP_LOG" || echo "0")
echo "Null message attempts: $NULL_ATTEMPTS"

# Count blank field rejections
BLANK_ID=$(grep -c "Message ID is blank" "$TEMP_LOG" || echo "0")
echo "Blank ID rejections: $BLANK_ID"

BLANK_CHAT_ID=$(grep -c "Chat ID is blank" "$TEMP_LOG" || echo "0")
echo "Blank Chat ID rejections: $BLANK_CHAT_ID"

BLANK_SENDER_ID=$(grep -c "Sender ID is blank" "$TEMP_LOG" || echo "0")
echo "Blank Sender ID rejections: $BLANK_SENDER_ID"

NO_CONTENT=$(grep -c "Message has no content" "$TEMP_LOG" || echo "0")
echo "No content rejections: $NO_CONTENT"

echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2. NullPointerException Analysis"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Count NullPointerException (unexpected - should be 0)
NPE_COUNT=$(grep -c "NullPointerException" "$TEMP_LOG" || echo "0")

if [ "$NPE_COUNT" -gt 0 ]; then
    echo -e "${RED}⚠ WARNING:${NC} NullPointerException detected: $NPE_COUNT"
    echo ""
    echo "Recent NullPointerException entries:"
    echo "----------------------------------------"
    grep "NullPointerException" "$TEMP_LOG" | tail -10
    echo ""
else
    echo -e "${GREEN}✓ GOOD:${NC} No NullPointerException detected"
    echo ""
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3. Chat-Related Error Analysis"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Count ChatRepository errors
CHAT_REPO_ERRORS=$(grep "ChatRepository" "$TEMP_LOG" | grep -c "Error" || echo "0")
echo "ChatRepository errors: $CHAT_REPO_ERRORS"

# Count OfflineMessageQueue errors
QUEUE_ERRORS=$(grep "OfflineMessageQueue" "$TEMP_LOG" | grep -c "Error" || echo "0")
echo "OfflineMessageQueue errors: $QUEUE_ERRORS"

# Count ChatRoomViewModel errors
VIEWMODEL_ERRORS=$(grep "ChatRoomViewModel" "$TEMP_LOG" | grep -c "Error" || echo "0")
echo "ChatRoomViewModel errors: $VIEWMODEL_ERRORS"

echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "4. Summary and Assessment"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

TOTAL_VALIDATIONS=$((VALIDATION_REJECTIONS + NULL_ATTEMPTS + BLANK_ID + BLANK_CHAT_ID + BLANK_SENDER_ID + NO_CONTENT))
echo "Total validation events: $TOTAL_VALIDATIONS"
echo "Total NullPointerException: $NPE_COUNT"
TOTAL_ERRORS=$((CHAT_REPO_ERRORS + QUEUE_ERRORS + VIEWMODEL_ERRORS))
echo "Total chat errors: $TOTAL_ERRORS"
echo ""

# Assessment
echo "Assessment:"
echo "───────────"

if [ "$NPE_COUNT" -eq 0 ]; then
    echo -e "${GREEN}✓ GOOD:${NC} No NullPointerException detected"
else
    echo -e "${RED}✗ CRITICAL:${NC} NullPointerException found - investigate immediately!"
fi

if [ "$TOTAL_VALIDATIONS" -gt 0 ]; then
    echo -e "${BLUE}ℹ INFO:${NC} Validation system is working - rejecting invalid messages"
else
    echo -e "${BLUE}ℹ INFO:${NC} No validation events detected"
fi

if [ "$TOTAL_ERRORS" -gt 10 ]; then
    echo -e "${YELLOW}⚠ WARNING:${NC} High error count - review logs for patterns"
elif [ "$TOTAL_ERRORS" -gt 0 ]; then
    echo -e "${BLUE}ℹ INFO:${NC} Some errors detected - normal operation"
else
    echo -e "${GREEN}✓ GOOD:${NC} No errors detected"
fi

echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "5. Recommendations"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

RECOMMENDATIONS=0

if [ "$NPE_COUNT" -gt 0 ]; then
    echo -e "${RED}[ACTION REQUIRED]${NC}"
    echo "1. Review NullPointerException stack traces above"
    echo "2. Identify the source of null values"
    echo "3. Add additional null checks if needed"
    echo "4. Test fix thoroughly before deploying"
    echo ""
    RECOMMENDATIONS=1
fi

if [ "$TOTAL_VALIDATIONS" -gt 100 ]; then
    echo -e "${YELLOW}[REVIEW NEEDED]${NC}"
    echo "1. High validation rejection rate detected"
    echo "2. Review data quality from Firestore"
    echo "3. Check client-side message creation"
    echo "4. Consider if validation rules are too strict"
    echo ""
    RECOMMENDATIONS=1
fi

if [ "$TOTAL_ERRORS" -gt 20 ]; then
    echo -e "${YELLOW}[MONITORING NEEDED]${NC}"
    echo "1. Elevated error count detected"
    echo "2. Review error patterns in full logs"
    echo "3. Check for specific error types"
    echo "4. Monitor trends over time"
    echo ""
    RECOMMENDATIONS=1
fi

if [ "$RECOMMENDATIONS" -eq 0 ]; then
    echo -e "${GREEN}[ALL CLEAR]${NC}"
    echo "System is operating normally"
    echo "Continue regular monitoring"
    echo ""
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "6. Detailed Error Breakdown"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Show recent validation errors
if [ "$TOTAL_VALIDATIONS" -gt 0 ]; then
    echo "Recent validation errors (last 5):"
    echo "───────────────────────────────────"
    grep -E "Invalid message:|Attempted to queue null message|is blank|has no content" "$TEMP_LOG" | tail -5
    echo ""
fi

# Show recent chat errors
if [ "$TOTAL_ERRORS" -gt 0 ]; then
    echo "Recent chat errors (last 5):"
    echo "────────────────────────────"
    grep -E "ChatRepository|OfflineMessageQueue|ChatRoomViewModel" "$TEMP_LOG" | grep "Error" | tail -5
    echo ""
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "7. Save Report"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

REPORT_FILE="validation-analysis-report-$(date +%Y%m%d-%H%M%S).txt"

{
    echo "Validation Log Analysis Report"
    echo "Date: $(date '+%Y-%m-%d %H:%M:%S')"
    echo ""
    echo "Validation Events:"
    echo "  Validation rejections: $VALIDATION_REJECTIONS"
    echo "  Null message attempts: $NULL_ATTEMPTS"
    echo "  Blank ID rejections: $BLANK_ID"
    echo "  Blank Chat ID rejections: $BLANK_CHAT_ID"
    echo "  Blank Sender ID rejections: $BLANK_SENDER_ID"
    echo "  No content rejections: $NO_CONTENT"
    echo "  Total: $TOTAL_VALIDATIONS"
    echo ""
    echo "Error Analysis:"
    echo "  NullPointerException: $NPE_COUNT"
    echo "  ChatRepository errors: $CHAT_REPO_ERRORS"
    echo "  OfflineMessageQueue errors: $QUEUE_ERRORS"
    echo "  ChatRoomViewModel errors: $VIEWMODEL_ERRORS"
    echo "  Total: $TOTAL_ERRORS"
    echo ""
    echo "Assessment:"
    if [ "$NPE_COUNT" -eq 0 ]; then
        echo "  ✓ No NullPointerException detected"
    else
        echo "  ✗ NullPointerException found: $NPE_COUNT"
    fi
    if [ "$TOTAL_VALIDATIONS" -gt 0 ]; then
        echo "  ℹ Validation system working"
    fi
    if [ "$TOTAL_ERRORS" -gt 10 ]; then
        echo "  ⚠ High error count"
    elif [ "$TOTAL_ERRORS" -eq 0 ]; then
        echo "  ✓ No errors detected"
    fi
} > "$REPORT_FILE"

echo "Report saved to: $REPORT_FILE"
echo ""

# Cleanup
rm "$TEMP_LOG"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Analysis Complete"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Next steps:"
echo "1. Review any critical issues identified"
echo "2. Monitor trends over time"
echo "3. Run this script daily during monitoring period"
echo "4. Compare results with previous runs"
echo ""

# Exit with appropriate code
if [ "$NPE_COUNT" -gt 0 ]; then
    exit 1
else
    exit 0
fi
