# Requirements Document - Firebase Rules Audit & Fixes

## Introduction

This specification addresses gaps and potential issues in the current Firebase Security Rules based on analysis of all existing Kiro specs for the TeamSync Collaboration app. While the current rules are mostly functional, there are several areas that need adjustment to fully support all planned features and prevent potential security issues.

## Glossary

- **System**: The Firebase Security Rules engine
- **User**: An authenticated user of the TeamSync application
- **Participant**: A user who is a member of a chat conversation
- **Member**: A user who belongs to a group
- **Owner**: The user who created a resource (group, task, etc.)

## Requirements

### Requirement 1: Fix Chat Message Participants Denormalization

**User Story:** As a user, I want to send and receive chat messages reliably, so that I can communicate with my team members.

#### Acceptance Criteria

1. WHEN a message is created in a chat THEN the System SHALL verify the user is in the parent chat's participants array
2. WHEN reading messages THEN the System SHALL verify access through the parent chat document, not require participants in each message
3. WHEN the chat document is queried THEN the System SHALL use `get()` to fetch parent chat participants for message access control
4. WHEN messages are written THEN the System SHALL enforce that senderId matches the authenticated user
5. WHEN the current rule requires participants in message documents THEN it SHALL be updated to check the parent chat document instead

### Requirement 2: Add Support for Public Group Discovery

**User Story:** As a user, I want to discover and join public groups, so that I can find study partners with similar interests.

#### Acceptance Criteria

1. WHEN a group has `isPublic` set to true THEN the System SHALL allow any authenticated user to read the group metadata
2. WHEN querying public groups THEN the System SHALL allow reads with `.where('isPublic', '==', true)` queries
3. WHEN a user wants to join a public group THEN they SHALL be able to read the group details before joining
4. WHEN a group is private THEN the System SHALL only allow members to read it
5. WHEN the groups collection is queried THEN the System SHALL support both member-based and public-based read access

### Requirement 3: Add Profile Picture URL Validation

**User Story:** As a developer, I want to ensure profile picture URLs are valid Firebase Storage URLs, so that we prevent unauthorized external image hosting.

#### Acceptance Criteria

1. WHEN a user updates their profileImageUrl THEN the System SHALL verify it starts with the Firebase Storage domain or is empty
2. WHEN validation fails THEN the System SHALL reject the write operation
3. WHEN the URL is from Firebase Storage THEN the System SHALL allow the update
4. WHEN the URL is empty or null THEN the System SHALL allow it (for default avatars)
5. WHEN external URLs are provided THEN the System SHALL reject them to prevent abuse

### Requirement 4: Add Task Group Membership Validation

**User Story:** As a user, I want to ensure group tasks are only visible to group members, so that my team's work remains private.

#### Acceptance Criteria

1. WHEN a task has a groupId THEN the System SHALL verify the user is a member of that group before allowing read access
2. WHEN a task is personal (no groupId) THEN the System SHALL allow access based on userId or assignedTo
3. WHEN creating a group task THEN the System SHALL verify the creator is a member of the specified group
4. WHEN updating a task's groupId THEN the System SHALL verify membership in both old and new groups
5. WHEN querying tasks THEN the System SHALL support filtering by both personal and group-based access

### Requirement 5: Add Typing Status Participants Check

**User Story:** As a user, I want typing indicators to work correctly, so that I know when others are composing messages.

#### Acceptance Criteria

1. WHEN a user updates typing status THEN the System SHALL verify they are a participant in the parent chat
2. WHEN reading typing status THEN the System SHALL verify the reader is a participant in the chat
3. WHEN the typing_status document is accessed THEN the System SHALL use `get()` to fetch the parent chat's participants
4. WHEN participants are not denormalized in typing_status THEN the System SHALL check the parent chat document
5. WHEN the current rule requires participants in typing_status documents THEN it SHALL be updated to check the parent chat

### Requirement 6: Add File Size and Type Validation in Storage Rules

**User Story:** As a user, I want to upload images and documents safely, so that the system prevents malicious or oversized files.

#### Acceptance Criteria

1. WHEN a user uploads a profile picture THEN the System SHALL verify the file size is under 5MB
2. WHEN a user uploads a chat image THEN the System SHALL verify the file size is under 5MB
3. WHEN a user uploads a chat document THEN the System SHALL verify the file size is under 10MB
4. WHEN a file is uploaded THEN the System SHALL verify the content type is in the allowed list
5. WHEN profile pictures are uploaded THEN the System SHALL only allow image MIME types (image/jpeg, image/png, image/webp)
6. WHEN chat documents are uploaded THEN the System SHALL allow common document types (pdf, doc, docx, txt, etc.)
7. WHEN invalid file types are uploaded THEN the System SHALL reject the operation

### Requirement 7: Add Chat Participant Limit Validation

**User Story:** As a developer, I want to prevent abuse of the chat system, so that users cannot create chats with excessive participants.

#### Acceptance Criteria

1. WHEN a direct chat is created THEN the System SHALL verify participants array has exactly 2 members
2. WHEN a group chat is created THEN the System SHALL verify participants array has at least 2 and at most 100 members
3. WHEN participants are added to a chat THEN the System SHALL enforce the maximum limit
4. WHEN chat type is DIRECT THEN the System SHALL enforce exactly 2 participants
5. WHEN chat type is GROUP THEN the System SHALL allow 2-100 participants

### Requirement 8: Add Group Member Limit Validation

**User Story:** As a developer, I want to prevent groups from becoming too large, so that the app remains performant.

#### Acceptance Criteria

1. WHEN a group is created THEN the System SHALL verify memberIds array has at least 1 member (the creator)
2. WHEN members are added to a group THEN the System SHALL enforce a maximum of 100 members
3. WHEN the owner is set THEN the System SHALL verify the owner is also in the memberIds array
4. WHEN memberIds is updated THEN the System SHALL verify the owner remains in the array
5. WHEN the maximum is reached THEN the System SHALL reject additional member additions

### Requirement 9: Add Notification Write Restrictions

**User Story:** As a developer, I want to ensure only the system can create notifications, so that users cannot spam themselves or others.

#### Acceptance Criteria

1. WHEN a notification is created THEN the System SHALL only allow writes from Cloud Functions (not client SDK)
2. WHEN a user updates a notification THEN they SHALL only be able to mark it as read, not create new ones
3. WHEN the read field is updated THEN the System SHALL allow it for the notification owner
4. WHEN other fields are updated THEN the System SHALL reject client-side updates
5. WHEN Cloud Functions write notifications THEN they SHALL bypass client restrictions

### Requirement 10: Add Task Assignment Validation

**User Story:** As a user, I want to ensure tasks are only assigned to valid users, so that assignments don't get lost.

#### Acceptance Criteria

1. WHEN a task is created with assignedTo THEN the System SHALL verify the creator is in the assignedTo array
2. WHEN assignedTo is updated THEN the System SHALL verify all user IDs are valid (non-empty strings)
3. WHEN a task has no assignedTo THEN the System SHALL default to the creator's userId
4. WHEN assignedTo array is empty THEN the System SHALL reject the operation
5. WHEN assignedTo exceeds 50 users THEN the System SHALL reject the operation to prevent abuse

### Requirement 11: Add Message Content Validation

**User Story:** As a developer, I want to prevent empty or malicious messages, so that the chat system remains clean and functional.

#### Acceptance Criteria

1. WHEN a message is created THEN the System SHALL verify either text, imageUrl, or documentUrl is present
2. WHEN text is provided THEN the System SHALL verify it is not empty and under 10,000 characters
3. WHEN imageUrl is provided THEN the System SHALL verify it is a valid Firebase Storage URL
4. WHEN documentUrl is provided THEN the System SHALL verify it is a valid Firebase Storage URL
5. WHEN senderId is set THEN the System SHALL verify it matches the authenticated user's ID
6. WHEN timestamp is set THEN the System SHALL verify it is within 5 minutes of server time to prevent backdating

### Requirement 12: Add Group Activity Access Control

**User Story:** As a user, I want group activities to be private to group members, so that our team's work is not visible to outsiders.

#### Acceptance Criteria

1. WHEN group activities are queried THEN the System SHALL verify the user is a member of the associated group
2. WHEN memberIds are denormalized in activities THEN the System SHALL use them for access control
3. WHEN creating an activity THEN the System SHALL verify the creator is a member of the group
4. WHEN the groupId is set THEN the System SHALL use `get()` to verify group membership if memberIds is not denormalized
5. WHEN activities are read THEN the System SHALL support queries with `.where('groupId', '==', groupId)` for members only

