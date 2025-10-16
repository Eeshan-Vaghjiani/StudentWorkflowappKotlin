# Task 43: Quick Reference - Firestore Indexes

## Quick Deploy Commands

### Deploy All Indexes
```bash
firebase deploy --only firestore:indexes
```

### Check Deployment Status
```bash
firebase firestore:indexes
```

### View in Console
https://console.firebase.google.com/ → Your Project → Firestore Database → Indexes

## Index Summary

| Collection | Fields | Purpose |
|------------|--------|---------|
| messages | chatId ↑, timestamp ↓ | Load chat messages |
| tasks | userId ↑, dueDate ↑ | User tasks by due date |
| tasks | groupId ↑, dueDate ↑ | Group tasks by due date |
| tasks | userId ↑, category ↑, dueDate ↑ | Filter by category |
| tasks | userId ↑, status ↑, dueDate ↑ | Filter by status |
| chats | participants (array), lastMessageTime ↓ | User chat list |

**Legend**: ↑ = Ascending, ↓ = Descending

## Quick Test

### 1. Run App
```bash
./gradlew installDebug
```

### 2. Check Logcat
```bash
adb logcat | grep -i "index"
```

### 3. Expected Result
- ✅ No "index required" errors
- ✅ Fast query performance (< 1 second)

## Common Issues

### "Index Required" Error
**Solution**: Click the link in the error message or deploy indexes manually

### Indexes Still Building
**Solution**: Wait 5-30 minutes, check Firebase Console for status

### Query Still Slow
**Solution**: 
1. Verify index is "Enabled" in Console
2. Check internet connection
3. Enable Firestore offline persistence

## Files Created
- ✅ `firestore.indexes.json` - Index definitions
- ✅ `TASK_43_FIRESTORE_INDEXES_GUIDE.md` - Detailed guide
- ✅ `TASK_43_QUICK_REFERENCE.md` - This file
- ✅ `TASK_43_VERIFICATION_CHECKLIST.md` - Testing checklist
