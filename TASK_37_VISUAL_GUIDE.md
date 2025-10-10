# Firestore Security Rules - Visual Guide

## 🗺️ Rules Architecture Overview

```
firestore.rules
│
├── Helper Functions
│   ├── isAuthenticated()
│   ├── isOwner(userId)
│   ├── isMember(groupId)
│   ├── isGroupAdmin(groupId)
│   ├── isParticipant(chatId)
│   └── isAssignedTo(taskData)
│
├── Users Collection
│   ├── Read: ✅ Any authenticated user
│   └── Write: ✅ Own profile only
│
├── Groups Collection
│   ├── Read: ✅ Members or public
│   ├── Create: ✅ Any authenticated
│   ├── Update: ✅ Admins/creator
│   └── Delete: ✅ Admins/creator
│
├── Tasks Collection
│   ├── Read: ✅ Creator or assigned
│   ├── Create: ✅ Any authenticated
│   ├── Update: ✅ Creator or assigned
│   └── Delete: ✅ Creator only
│
├── Chats Collection
│   ├── Read: ✅ Participants only
│   ├── Write: ✅ Participants only
│   │
│   └── Messages Subcollection
│       ├── Read: ✅ Participants
│       ├── Create: ✅ Participants
│       ├── Update: ✅ Sender only
│       └── Delete: ✅ Sender only
│
├── Typing Status Collection
│   ├── Read: ✅ Participants
│   └── Write: ✅ Participants
│
├── Notifications Collection
│   ├── Read: ✅ Own notifications
│   └── Write: ✅ Own notifications
│
└── Group Activities Collection
    ├── Read: ✅ Group members
    ├── Create: ✅ Group members
    ├── Update: ✅ Creator only
    └── Delete: ✅ Creator or admin
```

---

## 🔐 Access Control Matrix

### Users Collection (`/users/{userId}`)

| Operation | Authenticated | Own Profile | Other Profile |
|-----------|--------------|-------------|---------------|
| **Read**  | ✅ Allow     | ✅ Allow    | ✅ Allow      |
| **Create**| ✅ Allow     | ✅ Allow    | ❌ Deny       |
| **Update**| ✅ Allow     | ✅ Allow    | ❌ Deny       |
| **Delete**| ✅ Allow     | ✅ Allow    | ❌ Deny       |

**Why read is open**: Needed for user search, member lists, chat participants

---

### Groups Collection (`/groups/{groupId}`)

| Operation | Member | Non-Member (Public) | Non-Member (Private) | Admin |
|-----------|--------|---------------------|----------------------|-------|
| **Read**  | ✅ Allow | ✅ Allow          | ❌ Deny              | ✅ Allow |
| **Create**| ✅ Allow | ✅ Allow          | ✅ Allow             | ✅ Allow |
| **Update**| ❌ Deny  | ❌ Deny           | ❌ Deny              | ✅ Allow |
| **Delete**| ❌ Deny  | ❌ Deny           | ❌ Deny              | ✅ Allow |

**Admin includes**: Group creator and users in `admins` array

---

### Tasks Collection (`/tasks/{taskId}`)

| Operation | Creator | Assigned User | Unrelated User |
|-----------|---------|---------------|----------------|
| **Read**  | ✅ Allow | ✅ Allow     | ❌ Deny        |
| **Create**| ✅ Allow | ✅ Allow     | ✅ Allow       |
| **Update**| ✅ Allow | ✅ Allow     | ❌ Deny        |
| **Delete**| ✅ Allow | ❌ Deny      | ❌ Deny        |

**Note**: Assigned users can update but not delete tasks

---

### Chats Collection (`/chats/{chatId}`)

| Operation | Participant | Non-Participant |
|-----------|-------------|-----------------|
| **Read**  | ✅ Allow    | ❌ Deny         |
| **Write** | ✅ Allow    | ❌ Deny         |

**Participants**: Users in the `participants` array

---

### Messages Subcollection (`/chats/{chatId}/messages/{messageId}`)

| Operation | Participant (Sender) | Participant (Other) | Non-Participant |
|-----------|---------------------|---------------------|-----------------|
| **Read**  | ✅ Allow            | ✅ Allow            | ❌ Deny         |
| **Create**| ✅ Allow            | ✅ Allow            | ❌ Deny         |
| **Update**| ✅ Allow            | ❌ Deny             | ❌ Deny         |
| **Delete**| ✅ Allow            | ❌ Deny             | ❌ Deny         |

**Privacy**: Only sender can modify their messages

---

## 🔄 Rule Evaluation Flow

### Example: User A tries to read User B's profile

```
1. Request arrives
   ↓
2. Check: isAuthenticated()?
   ├─ No → ❌ DENY
   └─ Yes → Continue
   ↓
3. Check: Users collection rule
   ├─ Rule: allow read if isAuthenticated()
   └─ Result: ✅ ALLOW
```

### Example: User A tries to update User B's profile

```
1. Request arrives
   ↓
2. Check: isAuthenticated()?
   ├─ No → ❌ DENY
   └─ Yes → Continue
   ↓
3. Check: Users collection rule
   ├─ Rule: allow write if isAuthenticated() && isOwner(userId)
   ├─ isOwner(userB)? → User A != User B
   └─ Result: ❌ DENY
```

### Example: User A tries to read messages in Chat X

```
1. Request arrives
   ↓
2. Check: isAuthenticated()?
   ├─ No → ❌ DENY
   └─ Yes → Continue
   ↓
3. Check: isParticipant(chatX)?
   ├─ Fetch chat document
   ├─ Check if User A in participants array
   ├─ Yes → ✅ ALLOW
   └─ No → ❌ DENY
```

---

## 🎨 Collection Relationships

```
users/
  └── {userId}
       ├── Used by: All collections for user info
       └── Referenced in: groups.members, chats.participants, tasks.assignedTo

groups/
  └── {groupId}
       ├── Has: members[], admins[]
       ├── Referenced by: tasks.groupId, chats.groupId
       └── Checked by: isMember(), isGroupAdmin()

tasks/
  └── {taskId}
       ├── Has: createdBy, assignedTo[]
       ├── Belongs to: groups (optional)
       └── Checked by: isOwner(), isAssignedTo()

chats/
  └── {chatId}
       ├── Has: participants[]
       ├── Contains: messages/ subcollection
       ├── Related: typing_status/
       └── Checked by: isParticipant()
       
       messages/
         └── {messageId}
              ├── Has: senderId
              └── Checked by: isOwner()
```

---

## 🛡️ Security Layers

### Layer 1: Authentication
```
All requests must be authenticated
↓
isAuthenticated() returns true
```

### Layer 2: Ownership
```
User must own the resource
↓
isOwner(userId) checks request.auth.uid == userId
```

### Layer 3: Membership
```
User must be a member/participant
↓
isMember(groupId) or isParticipant(chatId)
```

### Layer 4: Role-Based
```
User must have specific role
↓
isGroupAdmin(groupId) checks admins array
```

---

## 📊 Permission Hierarchy

```
Most Restrictive
    ↓
┌─────────────────────────┐
│  Message Sender Only    │  ← Update/Delete own messages
├─────────────────────────┤
│  Chat Participants      │  ← Read/Send messages
├─────────────────────────┤
│  Group Members          │  ← Read group data
├─────────────────────────┤
│  Group Admins           │  ← Update/Delete group
├─────────────────────────┤
│  Task Creator           │  ← Delete tasks
├─────────────────────────┤
│  Assigned Users         │  ← Update tasks
├─────────────────────────┤
│  Authenticated Users    │  ← Read user profiles
└─────────────────────────┘
    ↓
Most Permissive
```

---

## 🔍 Common Scenarios

### Scenario 1: Sending a Message
```
User A wants to send message in Chat X

1. ✅ User A is authenticated
2. ✅ User A is in Chat X participants
3. ✅ Create operation allowed
4. ✅ Message created successfully
```

### Scenario 2: Viewing Group Tasks
```
User B wants to view tasks for Group Y

1. ✅ User B is authenticated
2. ✅ User B is member of Group Y
3. ✅ Tasks have groupId = Y
4. ✅ User B can read tasks
```

### Scenario 3: Deleting Someone's Message
```
User C wants to delete User A's message

1. ✅ User C is authenticated
2. ✅ User C is participant in chat
3. ❌ User C is not the sender
4. ❌ Delete operation denied
```

### Scenario 4: Joining a Public Group
```
User D wants to read public Group Z

1. ✅ User D is authenticated
2. ❌ User D is not a member
3. ✅ Group Z is public (isPublic = true)
4. ✅ Read operation allowed
```

---

## 🎯 Key Takeaways

### ✅ DO
- Always authenticate users
- Verify ownership before writes
- Check membership for group operations
- Validate participants for chats
- Use helper functions for reusability

### ❌ DON'T
- Allow unauthenticated access
- Let users modify others' data
- Expose private chats to non-participants
- Allow non-members to access group data
- Skip permission checks

---

## 📈 Performance Considerations

### Document Reads in Rules
```
isMember(groupId) → Reads 1 document (groups/{groupId})
isParticipant(chatId) → Reads 1 document (chats/{chatId})
isGroupAdmin(groupId) → Reads 1 document (groups/{groupId})
```

**Impact**: Each rule evaluation that uses `get()` counts as a document read

**Optimization**: Firestore caches these reads within a single request

---

## 🔄 Rule Update Process

```
1. Edit firestore.rules
   ↓
2. Test locally (optional)
   firebase emulators:start
   ↓
3. Deploy to Firebase
   firebase deploy --only firestore:rules
   ↓
4. Verify in Console
   Check Rules tab
   ↓
5. Test with app
   Try various operations
   ↓
6. Monitor usage
   Check for denied requests
```

---

## ✨ Summary

Your Firestore is now protected with comprehensive security rules that:

- ✅ Require authentication for all operations
- ✅ Enforce ownership for personal data
- ✅ Verify membership for group operations
- ✅ Protect chat privacy with participant checks
- ✅ Ensure message integrity with sender verification
- ✅ Control task access based on creation and assignment

**Next**: Deploy these rules to Firebase Console and test thoroughly!
