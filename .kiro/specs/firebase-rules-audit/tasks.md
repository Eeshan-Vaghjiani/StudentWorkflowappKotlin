# Implementation Plan

- [x] 1. Add enhanced helper functions to Firestore rules





  - Add `isValidStorageUrl()` helper function for URL validation
  - Add `isGroupMember()` helper function for group membership checks
  - Add `isChatParticipant()` helper function for chat participant checks
  - Add `isValidArraySize()` helper function for array size validation
  - Add `isValidStringLength()` helper function for string length validation
  - Add `isRecentTimestamp()` helper function for timestamp validation
  - _Requirements: 1, 3, 4, 5, 6, 7, 8, 10, 11_

- [x] 2. Fix messages subcollection rules





  - [x] 2.1 Update read rule to use `isChatParticipant(chatId)` instead of checking message participants


    - Remove requirement for participants field in message documents
    - Use parent chat document for access control
    - _Requirements: 1_

  - [x] 2.2 Add message content validation to create rule

    - Validate at least one of text, imageUrl, or documentUrl is present
    - Add text length validation (max 10,000 characters)
    - Add URL validation for imageUrl and documentUrl
    - Add timestamp validation (within 5 minutes of server time)
    - _Requirements: 11_

  - [x] 2.3 Update update and delete rules to use `isChatParticipant(chatId)`

    - Ensure sender can only update/delete their own messages
    - Verify sender is still a chat participant
    - _Requirements: 1_
-

- [x] 3. Fix typing status subcollection rules




  - Remove requirement for participants field in typing_status documents
  - Update read rule to use `isChatParticipant(chatId)`
  - Update write rule to use `isChatParticipant(chatId)`
  - Simplify rule logic while maintaining security
  - _Requirements: 5_
-

- [x] 4. Enhance users collection rules




  - Add profile picture URL validation to create rule
  - Add profile picture URL validation to update rule
  - Ensure URLs are either empty or valid Firebase Storage URLs
  - _Requirements: 3_

- [x] 5. Enhance groups collection rules






  - [x] 5.1 Add public group discovery support

    - Update read rule to allow reading public groups
    - Support queries with `.where('isPublic', '==', true)`
    - Maintain existing member-based access control
    - _Requirements: 2_
  - [x] 5.2 Add member limit validation

    - Enforce 1-100 member limit on create
    - Enforce 1-100 member limit on update
    - Validate owner is in memberIds array
    - _Requirements: 8_


- [x] 6. Enhance tasks collection rules





  - [x] 6.1 Add group membership validation for group tasks


    - Update read rule to allow group members to read group tasks
    - Validate group membership on create if groupId is set
    - Validate group membership on update if groupId changes
    - _Requirements: 4_

  - [x] 6.2 Add task assignment validation


    - Enforce 1-50 user limit on assignedTo array
    - Validate assignedTo is not empty
    - Ensure creator is in assignedTo array on create
    - _Requirements: 10_

- [x] 7. Enhance chats collection rules





  - Add participant limit validation based on chat type
  - Enforce exactly 2 participants for DIRECT chats
  - Enforce 2-100 participants for GROUP chats
  - Prevent modifying participants array via update
  - _Requirements: 7_
-

- [x] 8. Enhance notifications collection rules




  - Block client-side notification creation (allow create: if false)
  - Restrict updates to only the 'read' field
  - Maintain read and delete permissions for notification owners
  - Document that Cloud Functions use admin SDK to create notifications
  - _Requirements: 9_
- [x] 9. Enhance group activities collection rules
- [x] 9. Enhance group activities collection rules

  - Add fallback to `isGroupMember()` for access control
  - Validate group membership on create
  - Maintain existing memberIds-based access control
  - Support both denormalized and non-denormalized access patterns
  - _Requirements: 12_

- [x] 10. Update storage rules with file validation





  - [x] 10.1 Add file type validation helpers

    - Add `isValidImageType()` helper function
    - Add `isValidDocumentType()` helper function
    - Support common image types (jpeg, png, webp)
    - Support common document types (pdf, doc, docx, txt)
    - _Requirements: 6_

  - [x] 10.2 Update profile pictures rules





    - Add image type validation
    - Maintain 5MB size limit
    - Ensure only images can be uploaded
    - _Requirements: 6_

  - [x] 10.3 Update chat attachments rules






    - Add document type validation
    - Maintain 10MB size limit
    - Allow both images and documents
    - _Requirements: 6_

- [x] 11. Deploy updated Firestore rules




  - Backup current rules before deployment
  - Deploy new rules to Firebase project
  - Monitor Firebase Console for any permission errors
  - Verify deployment was successful
  - _Requirements: All_


- [x] 12. Deploy updated Storage rules




  - Backup current storage rules before deployment
  - Deploy new storage rules to Firebase project
  - Monitor Firebase Console for any storage errors
  - Verify deployment was successful
  - _Requirements: 6_


- [x] 13. Test rules with Firebase Emulator




  - [x] 13.1 Set up Firebase Emulator for testing


    - Install Firebase CLI if not already installed
    - Initialize emulator configuration
    - Start Firestore and Storage emulators
    - _Requirements: All_
  - [x] 13.2 Write unit tests for authentication rules






    - Test unauthenticated access is denied
    - Test authenticated users can access their own data
    - Test users cannot access other users' private data
    - _Requirements: All_
  - [x] 13.3 Write unit tests for groups rules


    - Test members can read group data
    - Test non-members cannot read private groups
    - Test anyone can read public groups
    - Test member limit enforcement
    - _Requirements: 2, 8_
  - [x] 13.4 Write unit tests for tasks rules


    - Test users can read their own tasks
    - Test users can read assigned tasks
    - Test group members can read group tasks
    - Test assignment limit enforcement
    - _Requirements: 4, 10_
  - [x] 13.5 Write unit tests for chats and messages rules


    - Test participants can read/write chats
    - Test non-participants cannot access chats
    - Test participant limit enforcement
    - Test message content validation
    - _Requirements: 1, 7, 11_
  - [x] 13.6 Write unit tests for storage rules


    - Test file type validation
    - Test file size limits
    - Test user can upload to their own folders
    - Test invalid uploads are rejected
    - _Requirements: 6_
-

- [x] 14. Perform integration testing








  - [x] 14.1 Test group workflow


    - Create group → Create group task → Verify member access
    - Create public group → Verify non-member can discover
    - Add user to group → Verify they can access group data
    - _Requirements: 2, 4, 8_

  - [x] 14.2 Test chat workflow

    - Create chat → Send message → Verify participant access
    - Test typing indicators work correctly
    - Test message validation prevents invalid content
    - _Requirements: 1, 5, 11_


  - [ ] 14.3 Test file upload workflow
    - Upload profile picture → Verify URL validation
    - Upload chat attachment → Verify type and size validation
    - Test invalid uploads are rejected with clear errors
    - _Requirements: 3, 6_
-

- [x] 15. Update client-side error handling








  - [x] 15.1 Add error messages for new validation rules

    - Add "Maximum participants reached" message
    - Add "Invalid image URL" message
    - Add "Message too long" message
    - Add "File type not supported" message
    - _Requirements: All_
  - [x] 15.2 Add client-side validation to prevent rule violations


    - Validate participant limits before creating chats
    - Validate message length before sending
    - Validate file types before uploading
    - Validate group membership before creating group tasks
    - _Requirements: All_
-
-

- [x] 16. Create documentation







  - [x] 16.1 Document new rule features

    - Document public group discovery
    - Document validation limits (participants, assignments, message length)
    - Document file type restrictions
    - _Requirements: All_

  - [x] 16.2 Create migration guide

    - Document backward compatibility
    - List any data cleanup needed
    - Provide rollback instructions
    - _Requirements: All_
  - [x] 16.3 Update API documentation


    - Update data model documentation with new fields
    - Document validation requirements for each collection
    - Provide code examples for common operations
    - _Requirements: All_

