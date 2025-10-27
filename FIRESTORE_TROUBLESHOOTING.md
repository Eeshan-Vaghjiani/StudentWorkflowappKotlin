# Firestore Permission Errors Troubleshooting Guide

## Overview

This guide helps diagnose and resolve Firestore permission errors and related issues. Use this when encountering problems with data access, queries, or security rules.

**Last Updated:** October 20, 2025  
**Rules Version:** 2.0

## Quick Diagnosis

### Step 1: Identify the Error

Check your logs for error messages:

```
PERMISSION_DENIED: Missing or insufficient permissions
FAILED_PRECONDITION: The query requires an index
UNAVAILABLE: The service is currently unavailable
UNAUTHENTICATED: The request does not have valid authentication credentials
```

### Step 2: Locate the Problem

- **Where:** Which screen/feature is affected?
- **When:** Does it happen on app start, or during specific actions?
- **Who:** Does it affect all users or specific users?
- **What:** Which collection/query is failing?

### Step 3: Check Common Causes

1. Query missing required filter
2. User not authenticated
3. User not in required array (memberIds, participants, etc.)
4. Denormalized data not updated
5. Rules not deployed correctly


## Common Issues and Solutions

### Issue 1: PERMISSION_DENIED on Groups Query

**Symptoms:**
```
FirebaseFirestoreException: PERMISSION_DENIED: Missing or insufficient permissions
at com.example.loginandregistration.repository.GroupRepository.getUserGroups
```

**Possible Causes:**

1. **Query missing filter**
   ```kotlin
   // WRONG
   db.collection("groups").get()
   
   // CORRECT
   db.collection("groups")
       .whereArrayContains("memberIds", userId)
       .get()
   ```

2. **User not authenticated**
   ```kotlin
   // Check authentication
   val currentUser = Firebase.auth.currentUser
   if (currentUser == null) {
       // User not logged in
       // Redirect to login screen
   }
   ```

3. **User not in memberIds array**
   ```kotlin
   // Verify user is actually a member
   db.collection("groups")
       .document(groupId)
       .get()
       .addOnSuccessListener { doc ->
           val memberIds = doc.get("memberIds") as? List<String>
           if (memberIds?.contains(userId) != true) {
               // User is not a member
           }
       }
   ```

**Solution:**
- Ensure query includes `whereArrayContains("memberIds", userId)`
- Verify user is authenticated before querying
- Check that user was properly added to group when created

