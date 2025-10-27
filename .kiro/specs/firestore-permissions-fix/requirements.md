# Requirements Document

## Introduction

The application is experiencing critical crashes due to Firestore permission errors. Users are unable to access the groups collection, which causes the app to crash with `PERMISSION_DENIED: Missing or insufficient permissions` errors. This is blocking all group-related functionality and causing a poor user experience with repeated app crashes.

The root cause is that the Firestore security rules have circular dependencies in their helper functions, particularly `isMember()` and `isGroupAdmin()`, which attempt to read group data to verify permissions, but those reads themselves require the same permissions they're trying to verify.

## Requirements

### Requirement 1: Fix Firestore Security Rules Circular Dependencies

**User Story:** As a user, I want to access group data without permission errors, so that the app doesn't crash when I navigate to the Groups screen.

#### Acceptance Criteria

1. WHEN a user queries the groups collection WHERE they are a member THEN the query SHALL succeed without permission errors
2. WHEN a user queries group activities THEN the query SHALL succeed for groups they belong to
3. WHEN security rules evaluate membership THEN they SHALL NOT create circular permission checks
4. IF a user is not a member of a group THEN they SHALL receive an empty result set, not a permission error
5. WHEN a user creates a new group THEN they SHALL be automatically added as the owner and first member

### Requirement 2: Implement Array-Based Permission Checks

**User Story:** As a developer, I want security rules that use array membership checks instead of document reads, so that permission evaluation is efficient and doesn't cause circular dependencies.

#### Acceptance Criteria

1. WHEN checking group membership THEN the rule SHALL use `request.auth.uid in resource.data.memberIds` instead of `get()` calls
2. WHEN checking task assignment THEN the rule SHALL use direct array checks on the task document
3. WHEN checking chat participation THEN the rule SHALL use direct array checks on the chat document
4. IF a helper function requires a `get()` call THEN it SHALL only be used in contexts where the document is already accessible
5. WHEN rules are deployed THEN all existing functionality SHALL continue to work

### Requirement 3: Add Proper Query Filtering Support

**User Story:** As a user, I want my queries to return only the data I have access to, so that I don't see permission errors when browsing the app.

#### Acceptance Criteria

1. WHEN a user queries groups with `where('memberIds', 'array-contains', userId)` THEN the security rule SHALL allow the read
2. WHEN a user queries tasks with `where('userId', '==', userId)` THEN the security rule SHALL allow the read
3. WHEN a user queries their assigned tasks THEN the security rule SHALL allow reads for tasks where they are in the assignedTo array
4. IF a query doesn't match the user's permissions THEN it SHALL return an empty result, not throw an error
5. WHEN rules validate queries THEN they SHALL support common query patterns used in the app

### Requirement 4: Test and Validate Rules

**User Story:** As a developer, I want to verify that the new security rules work correctly, so that I can deploy them with confidence.

#### Acceptance Criteria

1. WHEN rules are updated THEN they SHALL be tested against the Firebase Emulator before deployment
2. WHEN testing rules THEN all critical user flows SHALL be validated (groups, tasks, chats)
3. IF a rule change breaks existing functionality THEN it SHALL be identified before deployment
4. WHEN rules are deployed THEN the deployment SHALL be monitored for errors
5. WHEN users access the app after deployment THEN they SHALL NOT experience permission errors

### Requirement 5: Update Application Error Handling

**User Story:** As a user, I want helpful error messages when something goes wrong, so that I understand what happened and what to do next.

#### Acceptance Criteria

1. WHEN a permission error occurs THEN the app SHALL display a user-friendly message
2. WHEN the app catches a Firestore error THEN it SHALL log the error details for debugging
3. IF a permission error is caught THEN the app SHALL NOT crash
4. WHEN displaying error messages THEN they SHALL provide actionable guidance (e.g., "Try logging out and back in")
5. WHEN errors are logged THEN they SHALL include enough context to diagnose the issue
