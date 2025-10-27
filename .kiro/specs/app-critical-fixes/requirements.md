# Requirements Document - App Critical Fixes

## Introduction

This document outlines the requirements for fixing critical issues in the Study Planner Android app. The app currently has multiple broken features including authentication UI, data fetching, group management, assignments display, and chat functionality. These fixes will ensure the app matches the mockup specifications and properly integrates with Firebase Firestore for all data operations.

## Requirements

### Requirement 1: Authentication UI Improvements

**User Story:** As a user, I want a polished and intuitive login/registration experience that matches the app mockups, so that I can easily access the app with confidence in its quality.

#### Acceptance Criteria

1. WHEN the user opens the login screen THEN the system SHALL display a UI that matches the mockup design in `mobile-app-mockups/pages/login.html` with proper spacing, colors, and Material Design components
2. WHEN the user opens the registration screen THEN the system SHALL display a UI that matches the mockup design in `mobile-app-mockups/pages/register.html` with all required fields and validation
3. WHEN the user enters invalid credentials THEN the system SHALL display clear, inline error messages without crashing
4. WHEN the user successfully logs in THEN the system SHALL navigate to the dashboard with a smooth transition
5. IF the user has previously logged in THEN the system SHALL automatically authenticate them on app launch

### Requirement 2: Google Sign-In Integration

**User Story:** As a user, I want to sign in with my Google account, so that I can quickly access the app without creating a new password.

#### Acceptance Criteria

1. WHEN the user clicks the "Sign in with Google" button THEN the system SHALL launch the Google Sign-In flow
2. WHEN the user successfully authenticates with Google THEN the system SHALL create or update their user profile in Firestore
3. WHEN the user completes Google Sign-In THEN the system SHALL navigate to the dashboard
4. IF Google Sign-In fails THEN the system SHALL display a user-friendly error message and allow retry
5. WHEN the user's Google account is linked THEN the system SHALL store their profile picture URL and display name in Firestore

### Requirement 3: Dashboard Stats from Firestore

**User Story:** As a user, I want to see accurate statistics on my dashboard, so that I can track my progress and activity.

#### Acceptance Criteria

1. WHEN the dashboard loads THEN the system SHALL fetch real-time task counts from Firestore (total, completed, pending)
2. WHEN the dashboard loads THEN the system SHALL fetch the user's group count from Firestore
3. WHEN the dashboard loads THEN the system SHALL calculate and display study session statistics from Firestore
4. WHEN the dashboard loads THEN the system SHALL display AI usage statistics from the user's Firestore document
5. IF no data exists THEN the system SHALL display zero values with appropriate empty state messages
6. WHEN data changes in Firestore THEN the system SHALL update the dashboard stats in real-time using Firestore listeners

### Requirement 4: Groups Display and Management

**User Story:** As a user, I want to see all my groups and manage them effectively, so that I can collaborate with my study partners.

#### Acceptance Criteria

1. WHEN the user opens the Groups screen THEN the system SHALL fetch and display all groups where the user is a member from Firestore
2. WHEN groups exist in Firestore THEN the system SHALL display each group with its name, subject, member count, and recent activity
3. WHEN the user clicks on a group THEN the system SHALL navigate to the group details screen showing members and assignments
4. WHEN the user creates a new group THEN the system SHALL save it to Firestore and add the creator as an admin
5. WHEN the user deletes a group THEN the system SHALL remove it from Firestore and update all members' group lists
6. IF the user has no groups THEN the system SHALL display an empty state with a "Create Group" or "Join Group" call-to-action
7. WHEN the user joins a group via code THEN the system SHALL add them to the group's members array in Firestore

### Requirement 5: Assignments Display in Tasks and Calendar

**User Story:** As a user, I want to see all my assignments in the tasks list and calendar, so that I can manage my deadlines effectively.

#### Acceptance Criteria

1. WHEN the user opens the Tasks screen THEN the system SHALL fetch and display all assignments from Firestore where the user is assigned
2. WHEN assignments exist THEN the system SHALL display them with title, due date, priority, and associated group
3. WHEN the user opens the Calendar screen THEN the system SHALL display assignment due dates as events on the calendar
4. WHEN the user selects a date with assignments THEN the system SHALL display a list of assignments due on that date below the calendar
5. WHEN the user clicks an assignment THEN the system SHALL navigate to the assignment details screen
6. IF no assignments exist THEN the system SHALL display an appropriate empty state message
7. WHEN an assignment is added or updated in Firestore THEN the system SHALL reflect the changes in real-time on both Tasks and Calendar screens

### Requirement 6: Chat Functionality Fixes

**User Story:** As a user, I want to start new chats and send messages without errors, so that I can communicate with my study group members.

#### Acceptance Criteria

1. WHEN the user clicks "New Chat" THEN the system SHALL display a user selection dialog without errors
2. WHEN the user selects a contact to chat with THEN the system SHALL create or retrieve a chat document in Firestore
3. WHEN the user sends a message THEN the system SHALL save it to Firestore under the correct chat document
4. WHEN a message is sent THEN the system SHALL update the chat's lastMessage and lastMessageTime fields
5. IF Firestore security rules block the operation THEN the system SHALL update the rules to allow authenticated users to create chats and send messages
6. WHEN the user opens a chat THEN the system SHALL display all messages in chronological order from Firestore
7. WHEN new messages arrive THEN the system SHALL update the chat view in real-time using Firestore listeners

### Requirement 7: Firestore Security Rules Update

**User Story:** As a developer, I want properly configured Firestore security rules, so that users can perform necessary operations while maintaining data security.

#### Acceptance Criteria

1. WHEN a user is authenticated THEN the system SHALL allow them to read their own user document
2. WHEN a user is authenticated THEN the system SHALL allow them to create and update their own user document
3. WHEN a user is a group member THEN the system SHALL allow them to read group data and messages
4. WHEN a user creates a chat THEN the system SHALL allow them to write to the chats collection if they are a participant
5. WHEN a user sends a message THEN the system SHALL allow them to write to the messages subcollection if they are a chat participant
6. WHEN a user is assigned to a task THEN the system SHALL allow them to read and update that task
7. IF a user is not authenticated THEN the system SHALL deny all read and write operations

### Requirement 8: Remove Demo Data Dependencies

**User Story:** As a user, I want to see real data from the database, so that the app reflects my actual activity and information.

#### Acceptance Criteria

1. WHEN any screen loads THEN the system SHALL fetch data exclusively from Firestore, not from hardcoded demo data
2. WHEN the app initializes THEN the system SHALL remove all demo data constants and mock data generators
3. WHEN a screen has no data THEN the system SHALL display an appropriate empty state instead of demo data
4. WHEN data is needed for testing THEN the system SHALL use Firestore emulator or test data in Firestore, not hardcoded values
5. IF a screen previously used demo data THEN the system SHALL be refactored to use repository pattern with Firestore queries

### Requirement 9: Error Handling and User Feedback

**User Story:** As a user, I want clear feedback when something goes wrong, so that I understand what happened and how to fix it.

#### Acceptance Criteria

1. WHEN a network error occurs THEN the system SHALL display a user-friendly error message with a retry option
2. WHEN a Firestore operation fails THEN the system SHALL log the error and display an appropriate message to the user
3. WHEN loading data THEN the system SHALL display loading indicators (progress bars or skeletons)
4. WHEN an operation succeeds THEN the system SHALL provide visual feedback (toast message or success indicator)
5. IF the user is offline THEN the system SHALL display an offline indicator and queue operations for later
6. WHEN form validation fails THEN the system SHALL display inline error messages on the relevant fields
7. IF an unexpected error occurs THEN the system SHALL log it to Firebase Crashlytics and show a generic error message

### Requirement 10: Data Consistency and Real-time Updates

**User Story:** As a user, I want the app to stay up-to-date with changes, so that I always see the latest information.

#### Acceptance Criteria

1. WHEN data changes in Firestore THEN the system SHALL update the UI in real-time using Firestore snapshot listeners
2. WHEN the user creates, updates, or deletes data THEN the system SHALL immediately reflect the change in the UI
3. WHEN multiple users are in the same group THEN the system SHALL show updates from other users in real-time
4. WHEN the user navigates between screens THEN the system SHALL maintain data consistency across the app
5. IF a listener fails THEN the system SHALL attempt to reconnect and notify the user of connection issues
6. WHEN the app comes back online THEN the system SHALL sync any pending changes with Firestore
