#!/bin/bash
# Performance Monitoring Script for TeamSync App
# This script provides a quick overview of performance metrics

echo "========================================"
echo "TeamSync Performance Monitor"
echo "========================================"
echo ""
echo "Date: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""

# Colors for output
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

# Open Firebase Performance
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1. Firebase Performance Monitoring"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Opening Firebase Performance in browser..."
echo "URL: https://console.firebase.google.com/project/_/performance"
echo ""
echo "Check these metrics:"
echo "  ✓ App startup time (target: < 2 seconds)"
echo "  ✓ Screen rendering time (target: < 1 second)"
echo "  ✓ Network request duration (target: < 1 second)"
echo "  ✓ Frame rate (target: > 50 FPS)"
echo ""

# Open in default browser
if command -v xdg-open > /dev/null; then
    xdg-open "https://console.firebase.google.com/project/_/performance" 2>/dev/null &
elif command -v open > /dev/null; then
    open "https://console.firebase.google.com/project/_/performance" 2>/dev/null &
fi

read -p "Press Enter when you've reviewed Firebase Performance..."
echo ""

# Collect performance metrics
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2. Record Performance Metrics"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Please enter the following metrics from Firebase Performance:"
echo ""

read -p "App startup time (seconds): " APP_STARTUP
read -p "Number of slow screens (>1s): " SLOW_SCREENS
read -p "Average network request time (ms): " AVG_NETWORK_TIME
read -p "Network request failures (%): " NETWORK_FAILURES
read -p "Average frame rate (FPS): " FRAME_RATE
read -p "Frozen frames (%): " FROZEN_FRAMES

echo ""

# Analyze metrics
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3. Performance Analysis"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Analyze app startup time
if (( $(echo "$APP_STARTUP < 2" | bc -l) )); then
    echo -e "${GREEN}✓${NC} App startup time: ${APP_STARTUP}s (GOOD)"
elif (( $(echo "$APP_STARTUP < 3" | bc -l) )); then
    echo -e "${YELLOW}⚠${NC} App startup time: ${APP_STARTUP}s (WARNING)"
else
    echo -e "${RED}✗${NC} App startup time: ${APP_STARTUP}s (CRITICAL)"
fi

# Analyze slow screens
if [ "$SLOW_SCREENS" -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Slow screens: $SLOW_SCREENS (GOOD)"
elif [ "$SLOW_SCREENS" -lt 3 ]; then
    echo -e "${YELLOW}⚠${NC} Slow screens: $SLOW_SCREENS (WARNING)"
else
    echo -e "${RED}✗${NC} Slow screens: $SLOW_SCREENS (CRITICAL)"
fi

# Analyze network time
if [ "$AVG_NETWORK_TIME" -lt 1000 ]; then
    echo -e "${GREEN}✓${NC} Network request time: ${AVG_NETWORK_TIME}ms (GOOD)"
elif [ "$AVG_NETWORK_TIME" -lt 2000 ]; then
    echo -e "${YELLOW}⚠${NC} Network request time: ${AVG_NETWORK_TIME}ms (WARNING)"
else
    echo -e "${RED}✗${NC} Network request time: ${AVG_NETWORK_TIME}ms (CRITICAL)"
fi

# Analyze network failures
if [ "$NETWORK_FAILURES" -lt 5 ]; then
    echo -e "${GREEN}✓${NC} Network failures: ${NETWORK_FAILURES}% (GOOD)"
elif [ "$NETWORK_FAILURES" -lt 10 ]; then
    echo -e "${YELLOW}⚠${NC} Network failures: ${NETWORK_FAILURES}% (WARNING)"
else
    echo -e "${RED}✗${NC} Network failures: ${NETWORK_FAILURES}% (CRITICAL)"
fi

# Analyze frame rate
if [ "$FRAME_RATE" -ge 50 ]; then
    echo -e "${GREEN}✓${NC} Frame rate: ${FRAME_RATE} FPS (GOOD)"
elif [ "$FRAME_RATE" -ge 40 ]; then
    echo -e "${YELLOW}⚠${NC} Frame rate: ${FRAME_RATE} FPS (WARNING)"
else
    echo -e "${RED}✗${NC} Frame rate: ${FRAME_RATE} FPS (CRITICAL)"
fi

# Analyze frozen frames
if [ "$FROZEN_FRAMES" -lt 1 ]; then
    echo -e "${GREEN}✓${NC} Frozen frames: ${FROZEN_FRAMES}% (GOOD)"
elif [ "$FROZEN_FRAMES" -lt 3 ]; then
    echo -e "${YELLOW}⚠${NC} Frozen frames: ${FROZEN_FRAMES}% (WARNING)"
else
    echo -e "${RED}✗${NC} Frozen frames: ${FROZEN_FRAMES}% (CRITICAL)"
fi

echo ""

# Generate recommendations
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "4. Optimization Recommendations"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

CRITICAL_ISSUES=0

if (( $(echo "$APP_STARTUP >= 3" | bc -l) )); then
    echo -e "${RED}CRITICAL:${NC} App startup too slow"
    echo "  → Profile startup with Android Studio Profiler"
    echo "  → Defer non-critical initialization"
    echo "  → Use lazy loading for heavy components"
    echo ""
    CRITICAL_ISSUES=$((CRITICAL_ISSUES + 1))
fi

if [ "$SLOW_SCREENS" -ge 3 ]; then
    echo -e "${RED}CRITICAL:${NC} Multiple slow screens detected"
    echo "  → Profile slow screens with Profiler"
    echo "  → Optimize RecyclerView with DiffUtil"
    echo "  → Move heavy operations to background threads"
    echo ""
    CRITICAL_ISSUES=$((CRITICAL_ISSUES + 1))
fi

if [ "$AVG_NETWORK_TIME" -ge 2000 ]; then
    echo -e "${RED}CRITICAL:${NC} Network requests too slow"
    echo "  → Implement request caching"
    echo "  → Optimize Firestore queries"
    echo "  → Add appropriate indexes"
    echo ""
    CRITICAL_ISSUES=$((CRITICAL_ISSUES + 1))
fi

if [ "$FRAME_RATE" -lt 40 ]; then
    echo -e "${RED}CRITICAL:${NC} Frame rate too low"
    echo "  → Profile rendering with GPU Profiler"
    echo "  → Simplify complex layouts"
    echo "  → Optimize image loading"
    echo ""
    CRITICAL_ISSUES=$((CRITICAL_ISSUES + 1))
fi

if [ "$CRITICAL_ISSUES" -eq 0 ]; then
    echo -e "${GREEN}✓${NC} No critical performance issues detected"
    echo ""
    echo "Routine optimization:"
    echo "  → Continue monitoring performance"
    echo "  → Profile on low-end devices"
    echo "  → Optimize based on user feedback"
fi

echo ""

# Save report
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "5. Save Report"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

REPORT_FILE="performance-report-$(date +%Y%m%d-%H%M%S).txt"

{
    echo "TeamSync Performance Monitoring Report"
    echo "Date: $(date '+%Y-%m-%d %H:%M:%S')"
    echo ""
    echo "Metrics:"
    echo "  App startup time: ${APP_STARTUP}s"
    echo "  Slow screens: $SLOW_SCREENS"
    echo "  Network request time: ${AVG_NETWORK_TIME}ms"
    echo "  Network failures: ${NETWORK_FAILURES}%"
    echo "  Frame rate: ${FRAME_RATE} FPS"
    echo "  Frozen frames: ${FROZEN_FRAMES}%"
    echo ""
} > "$REPORT_FILE"

echo "Report saved to: $REPORT_FILE"
echo ""

# Summary
echo "========================================"
echo "Monitoring Complete"
echo "========================================"
echo ""
echo "Next steps:"
echo "1. Address critical performance issues"
echo "2. Profile slow operations with Android Studio"
echo "3. Implement optimizations"
echo "4. Test on multiple devices"
echo "5. Run this script again after optimizations"
echo ""
