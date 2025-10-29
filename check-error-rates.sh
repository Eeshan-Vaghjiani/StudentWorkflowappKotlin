#!/bin/bash
# Error Rate Monitoring Script for TeamSync App
# This script provides a quick overview of error rates and app health

echo "========================================"
echo "TeamSync Error Rate Monitor"
echo "========================================"
echo ""
echo "Date: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""

# Colors for output
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

# Function to check Firebase Crashlytics
check_crashlytics() {
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "1. Firebase Crashlytics Status"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "Opening Firebase Crashlytics in browser..."
    echo "URL: https://console.firebase.google.com/project/_/crashlytics"
    echo ""
    echo "Check these metrics:"
    echo "  ✓ Crash-free users (target: > 99%)"
    echo "  ✓ Crash-free sessions (target: > 99.5%)"
    echo "  ✓ Total crashes (trend should be decreasing)"
    echo "  ✓ New issues (should be minimal)"
    echo ""
    
    # Open in default browser (works on most Unix systems)
    if command -v xdg-open > /dev/null; then
        xdg-open "https://console.firebase.google.com/project/_/crashlytics" 2>/dev/null &
    elif command -v open > /dev/null; then
        open "https://console.firebase.google.com/project/_/crashlytics" 2>/dev/null &
    fi
    
    read -p "Press Enter when you've reviewed Crashlytics..."
    echo ""
}

# Function to check Firebase Performance
check_performance() {
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "2. Firebase Performance Monitoring"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "Opening Firebase Performance in browser..."
    echo "URL: https://console.firebase.google.com/project/_/performance"
    echo ""
    echo "Check these metrics:"
    echo "  ✓ App startup time (target: < 2 seconds)"
    echo "  ✓ Screen rendering time (target: < 1 second)"
    echo "  ✓ Network request duration (target: < 1 second)"
    echo "  ✓ Success/failure rates (target: > 95% success)"
    echo ""
    
    if command -v xdg-open > /dev/null; then
        xdg-open "https://console.firebase.google.com/project/_/performance" 2>/dev/null &
    elif command -v open > /dev/null; then
        open "https://console.firebase.google.com/project/_/performance" 2>/dev/null &
    fi
    
    read -p "Press Enter when you've reviewed Performance..."
    echo ""
}

# Function to check Play Console
check_play_console() {
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "3. Google Play Console Vitals"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "Opening Play Console in browser..."
    echo "URL: https://play.google.com/console"
    echo ""
    echo "Navigate to: Your App → Quality → Android vitals"
    echo ""
    echo "Check these metrics:"
    echo "  ✓ Crash rate (target: < 1%)"
    echo "  ✓ ANR rate (target: < 0.5%)"
    echo "  ✓ Excessive wakeups"
    echo "  ✓ Stuck wake locks"
    echo ""
    
    if command -v xdg-open > /dev/null; then
        xdg-open "https://play.google.com/console" 2>/dev/null &
    elif command -v open > /dev/null; then
        open "https://play.google.com/console" 2>/dev/null &
    fi
    
    read -p "Press Enter when you've reviewed Play Console..."
    echo ""
}

# Function to check Firebase Analytics
check_analytics() {
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "4. Firebase Analytics"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "Opening Firebase Analytics in browser..."
    echo "URL: https://console.firebase.google.com/project/_/analytics"
    echo ""
    echo "Check these metrics:"
    echo "  ✓ Active users (trend should be stable/growing)"
    echo "  ✓ User retention (Day 1: > 40%, Day 7: > 20%)"
    echo "  ✓ Event counts (verify key events are firing)"
    echo "  ✓ Conversion funnels (identify drop-off points)"
    echo ""
    
    if command -v xdg-open > /dev/null; then
        xdg-open "https://console.firebase.google.com/project/_/analytics" 2>/dev/null &
    elif command -v open > /dev/null; then
        open "https://console.firebase.google.com/project/_/analytics" 2>/dev/null &
    fi
    
    read -p "Press Enter when you've reviewed Analytics..."
    echo ""
}

# Function to collect manual input
collect_metrics() {
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "5. Record Current Metrics"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "Please enter the following metrics:"
    echo ""
    
    read -p "Crash-free users (%): " CRASH_FREE_USERS
    read -p "Total crashes (last 24h): " TOTAL_CRASHES
    read -p "Permission denied errors (last 24h): " PERMISSION_ERRORS
    read -p "API failures (last 24h): " API_FAILURES
    read -p "Network errors (last 24h): " NETWORK_ERRORS
    read -p "New issues opened: " NEW_ISSUES
    read -p "Issues resolved: " RESOLVED_ISSUES
    
    echo ""
}

# Function to analyze metrics
analyze_metrics() {
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "6. Metrics Analysis"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    
    # Analyze crash-free users
    if (( $(echo "$CRASH_FREE_USERS >= 99" | bc -l) )); then
        echo -e "${GREEN}✓${NC} Crash-free users: $CRASH_FREE_USERS% (GOOD)"
    elif (( $(echo "$CRASH_FREE_USERS >= 98" | bc -l) )); then
        echo -e "${YELLOW}⚠${NC} Crash-free users: $CRASH_FREE_USERS% (WARNING)"
    else
        echo -e "${RED}✗${NC} Crash-free users: $CRASH_FREE_USERS% (CRITICAL)"
    fi
    
    # Analyze total crashes
    if [ "$TOTAL_CRASHES" -lt 10 ]; then
        echo -e "${GREEN}✓${NC} Total crashes: $TOTAL_CRASHES (GOOD)"
    elif [ "$TOTAL_CRASHES" -lt 50 ]; then
        echo -e "${YELLOW}⚠${NC} Total crashes: $TOTAL_CRASHES (WARNING)"
    else
        echo -e "${RED}✗${NC} Total crashes: $TOTAL_CRASHES (CRITICAL)"
    fi
    
    # Analyze permission errors
    if [ "$PERMISSION_ERRORS" -lt 10 ]; then
        echo -e "${GREEN}✓${NC} Permission errors: $PERMISSION_ERRORS (GOOD)"
    elif [ "$PERMISSION_ERRORS" -lt 50 ]; then
        echo -e "${YELLOW}⚠${NC} Permission errors: $PERMISSION_ERRORS (WARNING)"
    else
        echo -e "${RED}✗${NC} Permission errors: $PERMISSION_ERRORS (CRITICAL)"
    fi
    
    # Analyze API failures
    if [ "$API_FAILURES" -lt 10 ]; then
        echo -e "${GREEN}✓${NC} API failures: $API_FAILURES (GOOD)"
    elif [ "$API_FAILURES" -lt 50 ]; then
        echo -e "${YELLOW}⚠${NC} API failures: $API_FAILURES (WARNING)"
    else
        echo -e "${RED}✗${NC} API failures: $API_FAILURES (CRITICAL)"
    fi
    
    # Analyze network errors
    if [ "$NETWORK_ERRORS" -lt 50 ]; then
        echo -e "${GREEN}✓${NC} Network errors: $NETWORK_ERRORS (GOOD)"
    elif [ "$NETWORK_ERRORS" -lt 100 ]; then
        echo -e "${YELLOW}⚠${NC} Network errors: $NETWORK_ERRORS (WARNING)"
    else
        echo -e "${RED}✗${NC} Network errors: $NETWORK_ERRORS (CRITICAL)"
    fi
    
    echo ""
    echo "Issue tracking:"
    echo "  New issues: $NEW_ISSUES"
    echo "  Resolved issues: $RESOLVED_ISSUES"
    echo ""
}

# Function to generate recommendations
generate_recommendations() {
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "7. Recommendations"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    
    CRITICAL_ISSUES=0
    
    if (( $(echo "$CRASH_FREE_USERS < 98" | bc -l) )); then
        echo -e "${RED}CRITICAL:${NC} Crash rate too high"
        echo "  → Investigate top crash issues immediately"
        echo "  → Prepare hotfix if needed"
        echo ""
        CRITICAL_ISSUES=$((CRITICAL_ISSUES + 1))
    fi
    
    if [ "$PERMISSION_ERRORS" -gt 50 ]; then
        echo -e "${RED}CRITICAL:${NC} High permission error rate"
        echo "  → Review Firestore security rules"
        echo "  → Check for recent rule changes"
        echo "  → Consider rollback if needed"
        echo ""
        CRITICAL_ISSUES=$((CRITICAL_ISSUES + 1))
    fi
    
    if [ "$API_FAILURES" -gt 50 ]; then
        echo -e "${RED}CRITICAL:${NC} High API failure rate"
        echo "  → Check Gemini AI service status"
        echo "  → Verify API configuration"
        echo "  → Review error logs"
        echo ""
        CRITICAL_ISSUES=$((CRITICAL_ISSUES + 1))
    fi
    
    if [ "$CRITICAL_ISSUES" -eq 0 ]; then
        echo -e "${GREEN}✓${NC} No critical issues detected"
        echo ""
        echo "Routine maintenance:"
        echo "  → Continue daily monitoring"
        echo "  → Review and triage new issues"
        echo "  → Plan fixes for next release"
    fi
    
    echo ""
}

# Function to save report
save_report() {
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "8. Save Report"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    
    REPORT_FILE="error-monitoring-report-$(date +%Y%m%d-%H%M%S).txt"
    
    {
        echo "TeamSync Error Monitoring Report"
        echo "Date: $(date '+%Y-%m-%d %H:%M:%S')"
        echo ""
        echo "Metrics:"
        echo "  Crash-free users: $CRASH_FREE_USERS%"
        echo "  Total crashes: $TOTAL_CRASHES"
        echo "  Permission errors: $PERMISSION_ERRORS"
        echo "  API failures: $API_FAILURES"
        echo "  Network errors: $NETWORK_ERRORS"
        echo "  New issues: $NEW_ISSUES"
        echo "  Resolved issues: $RESOLVED_ISSUES"
        echo ""
    } > "$REPORT_FILE"
    
    echo "Report saved to: $REPORT_FILE"
    echo ""
}

# Main execution
main() {
    check_crashlytics
    check_performance
    check_play_console
    check_analytics
    collect_metrics
    analyze_metrics
    generate_recommendations
    save_report
    
    echo "========================================"
    echo "Monitoring Complete"
    echo "========================================"
    echo ""
    echo "Next steps:"
    echo "1. Address any critical issues immediately"
    echo "2. Triage new issues within 24 hours"
    echo "3. Update issue tracker"
    echo "4. Schedule fixes for next release"
    echo "5. Run this script again tomorrow"
    echo ""
}

# Run main function
main
