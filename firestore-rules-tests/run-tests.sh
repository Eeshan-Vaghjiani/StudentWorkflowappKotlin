#!/bin/bash

echo "Starting Firebase Emulator and running tests..."
echo ""

echo "Step 1: Starting Firebase Emulator in background..."
firebase emulators:start --only firestore --project test-project &
EMULATOR_PID=$!

echo "Waiting for emulator to start (10 seconds)..."
sleep 10

echo ""
echo "Step 2: Running tests..."
npm test
TEST_EXIT_CODE=$?

echo ""
echo "Step 3: Stopping emulator..."
kill $EMULATOR_PID 2>/dev/null

echo ""
echo "Done!"
exit $TEST_EXIT_CODE
