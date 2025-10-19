# Requirements Document - Production Critical Fixes

## Introduction

This specification addresses all critical production issues preventing the TeamSync Collaboration app from functioning properly. Based on the logcat analysis, the app is experiencing:
- Firestore permission denied errors across all collections (groups, tasks, chats, activities)
- Missing Firestore indexes causing query failures
- Gemini AI API 404 errors (model not found)
- Dashboard displaying demo data instead of real Firestore data
- Groups and tasks not displaying despite existing in database
- Calendar not showing assignments
- Chat functionality partially broken

This spec will systematically fix all identified issues to make the app fully functional.

## Requirements

### Requirement 1: Fix Firestore Security Rules for All Collections

**User Story:** As a user, I want to access all my data without permission errors, so that I can use all app features normally.

#### Acceptance Criteria

1. WHEN a user queries their groups THEN the system SHALL allow read access for groups where `memberIds` array contains the user's ID
2. WHEN a user queries group activities THEN the system SHALL allow read access for activities in groups where the user is a member
3. WHEN a user queries tasks THEN the system SHALL allow read/write access for tasks where `userId` matches the authenticated user
4. WHEN a user queries chats THEN the system SHALL allow read/write access for chats where `participants` array contains the user's ID
5. WHEN a user creates a new group THEN the system SHALL allow write access and automatically add the creator to `memberIds`
6. WHEN a user creates a new task THEN the system SHALL allow write access with proper `userId` validation
7. WHEN a user sends a chat message THEN the system SHALL allow write access if the user is in the chat's `participants` array
8. WHEN security rules are deployed THEN all PERMISSION_DENIED errors SHALL be resolved

### Requirement 2: Create Required Firestore Indexes

**User Story:** As a user, I want my tasks to load quickly and correctly, so that I can view them sorted by due date.

#### Acceptance Criteria

1. WHEN a user queries tasks by userId and dueDate THEN the system SHALL have a composite index for `userId` (ascending) and `dueDate` (ascending)
2. WHEN the index is created THEN the FAILED_PRECONDITION error for tasks query SHALL be resolved
3. WHEN indexes are deployed THEN all queries SHALL execute without index-related errors
4. WHEN new queries are added THEN the system SHALL identify and create required indexes proactively
5. WHEN the firestore.indexes.json file is updated THEN it SHALL include all necessary composite indexes

### Requirement 3: Fix Gemini AI Integration

**User Story:** As a user, I want to use the AI assistant to help with my studies, so that I can get intelligent assistance.

#### Acceptance Criteria

1. WHEN the AI assistant is invoked THEN the system SHALL use the correct Gemini API model name (e.g., `gemini-1.5-flash` or `gemini-1.5-pro`)
2. WHEN the API call is made THEN it SHALL use the correct API version endpoint
3. WHEN the AI responds THEN messages SHALL display correctly in the chat interface
4. WHEN API errors occur THEN the system SHALL display user-friendly error messages with retry options
5. WHEN the Gemini API key is configured THEN it SHALL be properly secured and not exposed in logs
6. IF the model name is incorrect THEN the system SHALL update to use an available model from the current Gemini API

### Requirement 4: Replace Demo Data with Real Firestore Data

**User Story:** As a user, I want to see my actual data on the dashboard, so that I can track my real progress and statistics.

#### Acceptance Criteria

1. WHEN the dashboard loads THEN the system SHALL fetch task counts from Firestore (total, completed, pending, overdue)
2. WHEN the dashboard loads THEN the system SHALL fetch the user's actual group count from Firestore
3. WHEN the dashboard loads THEN the system SHALL fetch real assignment counts from Firestore
4. WHEN the dashboard loads THEN the system SHALL fetch actual AI usage statistics from the user's Firestore document
5. WHEN no data exists THEN the system SHALL display zero values with appropriate empty state messages
6. WHEN data changes in Firestore THEN the system SHALL update the dashboard in real-time using snapshot listeners
7. WHEN the dashboard displays stats THEN it SHALL NOT show any hardcoded demo values

### Requirement 5: Fix Groups Display and Real-time Updates

**User Story:** As a user, I want to see all my groups with accurate information, so that I can manage my collaborations effectively.

#### Acceptance Criteria

1. WHEN the Groups screen loads THEN the system SHALL fetch all groups where the user's ID is in the `memberIds` array
2. WHEN groups exist THEN the system SHALL display each group with name, subject, member count, and last activity
3. WHEN a new group is created THEN it SHALL appear immediately in the groups list
4. WHEN group data changes THEN the UI SHALL update in real-time via Firestore listeners
5. WHEN the user has no groups THEN the system SHALL display an empty state with a "Create Group" button
6. WHEN permission errors occur THEN the system SHALL log them and display a user-friendly message
7. WHEN the user navigates away and back THEN groups SHALL reload correctly without errors

### Requirement 6: Fix Tasks Display with Proper Indexing

**User Story:** As a user, I want to see all my tasks sorted by due date, so that I can prioritize my work effectively.

#### Acceptance Criteria

1. WHEN the Tasks screen loads THEN the system SHALL query tasks where `userId` equals the authenticated user's ID
2. WHEN tasks are queried THEN they SHALL be ordered by `dueDate` ascending
3. WHEN the composite index exists THEN the query SHALL execute without FAILED_PRECONDITION errors
4. WHEN tasks exist THEN the system SHALL display them with title, due date, priority, and completion status
5. WHEN a new task is created THEN it SHALL appear immediately in the tasks list
6. WHEN task statistics are calculated THEN they SHALL show accurate counts for overdue, due today, and completed tasks
7. WHEN the user has no tasks THEN the system SHALL display an appropriate empty state

### Requirement 7: Fix Calendar to Display Assignments

**User Story:** As a user, I want to see my assignments on the calendar, so that I can visualize my deadlines.

#### Acceptance Criteria

1. WHEN the Calendar screen loads THEN the system SHALL fetch all tasks/assignments for the current user from Firestore
2. WHEN assignments have due dates THEN they SHALL appear as events on the corresponding calendar dates
3. WHEN a user selects a date THEN the system SHALL display all assignments due on that date below the calendar
4. WHEN assignments are added or updated THEN the calendar SHALL refresh to show the changes
5. WHEN no assignments exist for a date THEN the calendar SHALL show the date without event markers
6. WHEN the user navigates between months THEN assignments SHALL load correctly for the visible date range
7. WHEN clicking an assignment in the calendar THEN the system SHALL navigate to the assignment details screen

### Requirement 8: Fix Chat Functionality and Permissions

**User Story:** As a user, I want to send and receive messages without errors, so that I can communicate with my study partners.

#### Acceptance Criteria

1. WHEN a user opens a chat THEN the system SHALL load messages without permission errors
2. WHEN a user sends a message THEN it SHALL be saved to Firestore successfully
3. WHEN messages are received THEN they SHALL display in chronological order
4. WHEN the user is not in the chat's participants array THEN appropriate access control SHALL be enforced
5. WHEN group chats are created THEN they SHALL be properly initialized with all group members as participants
6. WHEN the "initials" field warning appears THEN the UserInfo model SHALL be updated to include this field or the warning SHALL be suppressed
7. WHEN chat operations fail THEN errors SHALL be logged and user-friendly messages SHALL be displayed

### Requirement 9: Implement Comprehensive Error Handling

**User Story:** As a user, I want clear feedback when errors occur, so that I understand what went wrong and how to proceed.

#### Acceptance Criteria

1. WHEN a Firestore permission error occurs THEN the system SHALL display a message like "Unable to load data. Please check your connection."
2. WHEN a network error occurs THEN the system SHALL show a retry button
3. WHEN an index is missing THEN the system SHALL log the index creation URL and notify the developer
4. WHEN the Gemini API fails THEN the system SHALL display "AI assistant is temporarily unavailable"
5. WHEN loading data THEN the system SHALL show loading indicators (progress bars or skeleton screens)
6. WHEN operations succeed THEN the system SHALL provide subtle confirmation (e.g., toast messages)
7. WHEN unexpected errors occur THEN they SHALL be logged to Firebase Crashlytics for debugging

### Requirement 10: Fix OnBackInvokedCallback Warning

**User Story:** As a developer, I want to use modern Android back navigation, so that the app follows current best practices.

#### Acceptance Criteria

1. WHEN the AndroidManifest.xml is updated THEN it SHALL include `android:enableOnBackInvokedCallback="true"` in the application tag
2. WHEN the app runs on Android 13+ THEN it SHALL use the predictive back gesture API
3. WHEN the back button is pressed THEN navigation SHALL work correctly without warnings
4. WHEN the app targets Android 13+ THEN it SHALL handle back navigation using OnBackInvokedCallback
5. WHEN the manifest is updated THEN the warning SHALL no longer appear in logcat

### Requirement 11: Optimize Main Thread Performance

**User Story:** As a user, I want smooth UI interactions without lag, so that the app feels responsive.

#### Acceptance Criteria

1. WHEN the app performs Firestore operations THEN they SHALL execute on background threads
2. WHEN the UI updates THEN it SHALL happen on the main thread without blocking operations
3. WHEN the "Skipped frames" warning appears THEN heavy operations SHALL be moved off the main thread
4. WHEN images are loaded THEN they SHALL use efficient loading libraries (e.g., Glide or Coil)
5. WHEN lists are displayed THEN RecyclerView SHALL use proper view recycling and DiffUtil
6. WHEN the app starts THEN initialization SHALL be optimized to reduce startup time
7. WHEN performance issues are detected THEN they SHALL be profiled and optimized

### Requirement 12: Create Deployment Checklist and Verification

**User Story:** As a developer, I want a clear checklist to verify all fixes, so that I can confirm the app is fully functional.

#### Acceptance Criteria

1. WHEN all fixes are implemented THEN a verification checklist SHALL be created
2. WHEN Firestore rules are deployed THEN they SHALL be tested with actual user operations
3. WHEN indexes are created THEN they SHALL be verified in the Firebase Console
4. WHEN the Gemini API is fixed THEN it SHALL be tested with sample queries
5. WHEN the dashboard is updated THEN it SHALL display real data from Firestore
6. WHEN groups and tasks are tested THEN they SHALL load and display correctly
7. WHEN the calendar is tested THEN assignments SHALL appear on the correct dates
8. WHEN all features are verified THEN the app SHALL be ready for production use

