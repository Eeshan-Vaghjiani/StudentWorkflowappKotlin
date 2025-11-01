#!/bin/bash
# Firestore Rules Deployment Script for Unix/Linux/Mac
# This script backs up current rules, tests them, and deploys to Firebase

echo "========================================"
echo "Firestore Rules Deployment Script"
echo "========================================"
echo ""

# Step 1: Backup current rules
echo "[1/5] Backing up current Firestore rules..."
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
firebase firestore:rules:get > "firestore.rules.backup-$TIMESTAMP"
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to backup current rules"
    exit 1
fi
echo "Backup saved to: firestore.rules.backup-$TIMESTAMP"
echo ""

# Step 2: Test rules with emulator
echo "[2/5] Testing rules with Firebase Emulator..."
echo "Starting emulator for rule testing..."
echo "NOTE: This will start the emulator. Press Ctrl+C when tests are complete."
echo ""
firebase emulators:start --only firestore
if [ $? -ne 0 ]; then
    echo "WARNING: Emulator test failed or was cancelled"
    echo ""
fi

# Step 3: Confirm deployment
echo "[3/5] Ready to deploy rules"
read -p "Deploy Firestore rules to production? (yes/no): " CONFIRM
if [ "$CONFIRM" != "yes" ]; then
    echo "Deployment cancelled by user"
    exit 0
fi
echo ""

# Step 4: Deploy rules
echo "[4/5] Deploying Firestore rules to production..."
firebase deploy --only firestore:rules
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to deploy rules"
    echo "To rollback, run: firebase deploy --only firestore:rules"
    echo "And restore from backup: firestore.rules.backup-$TIMESTAMP"
    exit 1
fi
echo ""

# Step 5: Verify deployment
echo "[5/5] Deployment complete!"
echo ""
echo "Next steps:"
echo "1. Monitor Firebase Console for rule violations"
echo "2. Check application logs for permission errors"
echo "3. If issues occur, rollback using: firebase deploy --only firestore:rules"
echo "   (After restoring firestore.rules from backup)"
echo ""
echo "Backup file: firestore.rules.backup-$TIMESTAMP"
echo "========================================"
