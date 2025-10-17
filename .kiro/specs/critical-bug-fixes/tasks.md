# Implementation Plan

- [ ] 1. Fix Firestore Security Rules
  - Update security rules file with correct field names
  - Replace `members` with `memberIds` in all group-related rules
  - Fix chat participants array checks
  - Add typing_status subcollection rules
  - Fix group_activities collection rules
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 10.1, 10.2, 10.3_

- [ ] 2. Create Firebase Storage security rules file
  - Create storage.rules file with authentication checks
  - Add profile_pictures folder rules
  - Add chat_attachments folder rules
  - Add file size limits (10MB)
  - _Requirements: 4.4, 12.1, 12.2, 12.3, 12.4, 12.5_

- [ ] 3. Fix RecyclerView crash in MessageAdapter
  - Create MessageDiffCallback class for efficient updates
  - Update MessageAdapter to use DiffUtil
  - Add proper onViewRecycled cleanup
  - Remove manual view attachment logic
  - Test with rapid message updates
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [ ] 4. Implement StorageRepository for file uploads
  - Create StorageRepository.kt class
  - Implement uploadProfilePicture method
  - Implement uploadChatAttachment method
  - Add progress tracking for uploads
  - Implement error handling for storage operations
  - _Requirements: 4.1, 4.2, 4.3, 4.5, 4.6_

- [ ] 5. Update ProfileActivity to support profile picture upload
  - Add image picker functionality
  - Integrate StorageRepository for uploads
  - Show upload progress indicator
  - Update user document with photo URL
  - Display uploaded image with Coil
  - _Requirements: 4.1, 4.3, 4.5_

- [ ] 6. Update ChatRoomActivity to support file attachments
  - Add attachment button to chat input
  - Implement file picker for documents and images
  - Integrate StorageRepository for uploads
  - Update Message model to include attachment fields
  - Display attachments in message bubbles
  - _Requirements: 4.2, 4.3, 4.5, 4.6, 9.1_

- [ ] 7. Fix theme and color system
  - Update themes.xml for light mode with Material 3 colors
  - Create themes.xml for dark mode (values-night)
  - Update colors.xml with consistent color palette
  - Apply theme colors to all activities
  - Test both light and dark modes
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

- [ ] 8. Implement error handling framework
  - Create AppError sealed class
  - Create ErrorHandler utility class
  - Add safeFirestoreCall extension function
  - Update all repositories to use error handling
  - Update ViewModels to propagate errors to UI
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

- [ ] 9. Fix Google Sign-In flow
  - Update LoginActivity error handling
  - Add proper FCM token saving after sign-in
  - Handle sign-in cancellation gracefully
  - Initialize user document with all required fields
  - Show appropriate error messages
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ] 10. Fix group creation and display
  - Update CreateGroupActivity to use correct field names
  - Ensure memberIds array is properly initialized
  - Add real-time listener for groups list
  - Fix GroupRepository query to match security rules
  - Test group creation and immediate display
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 10.1_

- [ ] 11. Fix task creation and display
  - Update CreateTaskActivity with proper field initialization
  - Add real-time listener for tasks list
  - Fix TaskRepository query to match security rules
  - Ensure tasks appear in calendar view
  - Test task creation and immediate display
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5_

- [ ] 12. Fix chat message sending and reading
  - Update ChatRepository to handle permission errors
  - Fix message read status updates
  - Implement offline message queue properly
  - Add retry logic for failed messages
  - Test message sending reliability
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6_

- [ ] 13. Create AI Assistant Service
  - Create GeminiAssistantService class
  - Implement sendMessage method with API integration
  - Implement createAssignmentFromAI method
  - Add prompt engineering for assignment creation
  - Parse AI responses to extract task data
  - Handle API errors gracefully
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7_

- [ ] 14. Create AI Assistant UI
  - Create AIAssistantActivity layout
  - Create AIMessageAdapter for chat interface
  - Create AIAssistantViewModel
  - Implement message sending UI
  - Show loading states during API calls
  - Display AI responses with proper formatting
  - _Requirements: 8.1, 8.2, 8.6_

- [ ] 15. Integrate AI Assistant with Task creation
  - Add "Create with AI" button to tasks screen
  - Parse AI-generated task data
  - Create Task object from AI response
  - Save task to Firestore
  - Show success/error feedback
  - _Requirements: 8.2, 8.3, 8.4, 8.5_

- [ ] 16. Add Gemini API configuration
  - Add API key to local.properties
  - Read API key in BuildConfig
  - Add OkHttp dependency for API calls
  - Add Gson for JSON parsing
  - Test API connectivity
  - _Requirements: 8.5, 8.6_

- [ ] 17. Update Message model for attachments
  - Add attachment fields to Message data class
  - Update Firestore serialization
  - Update message display logic for attachments
  - Add attachment preview in chat
  - _Requirements: 4.2, 4.5, 9.1_

- [ ] 18. Fix typing status functionality
  - Update typing status security rules
  - Fix ChatRepository typing status methods
  - Test real-time typing indicators
  - Handle permission errors gracefully
  - _Requirements: 9.4, 9.5_

- [ ] 19. Comprehensive testing of all fixes
  - Test Google Sign-In flow
  - Test group creation and display
  - Test task creation and display
  - Test chat messaging with attachments
  - Test profile picture upload
  - Test AI assistant
  - Test in both light and dark modes
  - Test offline scenarios
  - _Requirements: All_

- [ ] 20. Create comprehensive Firebase setup guide
  - Analyze all Firebase configuration files in codebase
  - Document current Firebase project structure
  - Create step-by-step guide for new Firebase project creation
  - Include Authentication setup (Google Sign-In)
  - Include Firestore vs Realtime Database comparison
  - Include Firestore setup with security rules deployment
  - Include Storage setup with security rules
  - Include Cloud Messaging (FCM) configuration
  - List all files that need updating (google-services.json, etc.)
  - Provide Firebase CLI commands for rules deployment
  - Create verification checklist for new project
  - Include troubleshooting section for common issues
  - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5, 13.6, 13.7, 13.8, 13.9, 13.10_
