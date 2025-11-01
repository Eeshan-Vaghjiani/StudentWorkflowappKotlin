# Group Creation Permission Diagnosis

## Steps to Diagnose

### 1. Check User Authentication
- Verify the user is logged in
- Check if `auth.currentUser` is not null
- Verify `auth.currentUser.uid` matches the expected user ID

### 2. Check Firestore Rules
The rules now require:
```javascript
isAuthenticated()                                    // User must be logged in
&& request.resource.data.owner == request.auth.uid   // Owner must be current user
&& request.auth.uid in request.resource.data.memberIds  // User must be in memberIds
&& validStringLength(request.resource.data.name, 1, 100)  // Name: 1-100 chars
&& validStringLength(request.resource.data.description, 0, 500)  // Description: 0-500 chars
&& validStringLength(request.resource.data.get('subject', ''), 0, 100)  // Subject: 0-100 chars
&& validArraySize(request.resource.data.memberIds, 1, 100)  // MemberIds: 1-100 items
&& request.resource.data.isActive == true  // isActive must be true
&& request.resource.data.keys().hasAll(['name', 'description', 'owner', 'memberIds', 'isActive'])  // Required fields
```

### 3. Verify Data Being Sent
Check that the group object contains:
- ✓ name (string, 1-100 chars)
- ✓ description (string, 0-500 chars)
- ✓ subject (string, 0-100 chars)
- ✓ owner (string, equals current user ID)
- ✓ memberIds (array, contains current user ID, 1-100 items)
- ✓ isActive (boolean, true)

### 4. Common Issues

#### Issue 1: User Not Authenticated
**Symptom:** `auth.currentUser` is null
**Solution:** Ensure user is logged in before creating group

#### Issue 2: Token Expired
**Symptom:** Permission denied even though user is logged in
**Solution:** Force token refresh:
```kotlin
FirebaseAuth.getInstance().currentUser?.getIdToken(true)
```

#### Issue 3: Field Validation Failure
**Symptom:** One of the validation checks fails
**Solution:** Check field lengths and types

#### Issue 4: Missing Required Fields
**Symptom:** Rules check for required fields fails
**Solution:** Ensure all required fields are present

## Quick Fix

Try logging out and back in to refresh the authentication token:
1. Sign out from the app
2. Sign back in
3. Try creating a group again

## Testing in Firebase Console

You can test the rules directly in the Firebase Console:
1. Go to Firestore Rules
2. Click "Rules Playground"
3. Test with this data:
```json
{
  "name": "Test Group",
  "description": "Test Description",
  "subject": "Test Subject",
  "owner": "YOUR_USER_ID",
  "joinCode": "ABC123",
  "createdAt": {"_seconds": 1234567890, "_nanoseconds": 0},
  "updatedAt": {"_seconds": 1234567890, "_nanoseconds": 0},
  "members": [],
  "memberIds": ["YOUR_USER_ID"],
  "tasks": [],
  "settings": {"isPublic": false},
  "isActive": true
}
```
