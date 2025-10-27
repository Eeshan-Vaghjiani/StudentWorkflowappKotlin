# Task 14: AI Assistant UI - Quick Reference

## How to Use the AI Assistant

### Accessing the AI Assistant
1. Open the app and navigate to the **Tasks** tab
2. Scroll down to the **Quick Actions** section
3. Tap the **AI Assistant** button
4. The AI Assistant chat interface will open

### Sending Messages
1. Type your message in the input field at the bottom
2. Tap the send button (blue FAB) or press Enter
3. Wait for the AI to respond (loading indicator will show)
4. The AI's response will appear in the chat

### Creating Assignments with AI
1. Ask the AI to create an assignment, for example:
   - "Create a Math homework assignment due next Friday"
   - "I need to create an assignment for Science project"
   - "Make a task for English essay due in 3 days"

2. The AI will respond with assignment details
3. An **"Create Assignment"** button will appear
4. Tap the button to create the task
5. The task will be saved and appear in your Tasks list

## Configuration

### Setting Up API Key
Before using the AI Assistant, you need to configure your Gemini API key:

1. Get an API key from [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Open `local.properties` in your project root
3. Add this line:
   ```properties
   GEMINI_API_KEY=your_actual_api_key_here
   ```
4. Rebuild the project

### If API Key is Missing
- The app will show a toast message: "Please configure Gemini API key in local.properties"
- The AI Assistant will not work until the key is configured

## UI Components

### Main Screen Elements
- **Toolbar**: Shows "AI Assistant" title with back button
- **Messages Area**: Displays conversation history
- **Empty State**: Shows when no messages (AI brain icon with instructions)
- **Loading Indicator**: Shows "AI is thinking..." during API calls
- **Message Input**: Text field for typing messages
- **Send Button**: Blue FAB to send messages

### Message Types
- **User Messages**: Right-aligned, blue background, white text
- **AI Messages**: Left-aligned, gray background, dark text
- **Action Buttons**: Appear on AI messages when task creation is suggested

## Key Files

### Activity
- `AIAssistantActivity.kt` - Main activity for AI chat

### ViewModel
- `AIAssistantViewModel.kt` - Manages conversation state and AI interactions

### Adapter
- `AIMessageAdapter.kt` - Displays messages in RecyclerView

### Layouts
- `activity_ai_assistant.xml` - Main activity layout
- `item_ai_message_user.xml` - User message item
- `item_ai_message_assistant.xml` - AI message item

## Common Issues and Solutions

### Issue: "Please configure Gemini API key"
**Solution**: Add your API key to `local.properties` and rebuild

### Issue: AI not responding
**Solution**: 
- Check internet connection
- Verify API key is correct
- Check Gemini API quota/limits

### Issue: Task not created
**Solution**:
- Check Firestore permissions
- Verify user is authenticated
- Check app logs for errors

### Issue: Messages not displaying
**Solution**:
- Check if RecyclerView is visible
- Verify adapter is set correctly
- Check for layout issues

## Example Conversations

### Creating a Simple Assignment
```
User: Create a Math homework assignment
AI: I'll help you create that assignment. What's the due date?
User: Due next Friday
AI: [Shows assignment details with Create Assignment button]
[User taps button]
AI: Great! I've created the assignment "Math homework" for you.
```

### Getting Study Help
```
User: How should I organize my study schedule?
AI: Here are some tips for organizing your study schedule:
1. Prioritize tasks by due date
2. Break large assignments into smaller tasks
3. Use the calendar view to visualize deadlines
...
```

### Creating Multiple Assignments
```
User: I need to create 3 assignments
AI: I can help with that! Let's start with the first one. What subject is it for?
User: Science project due Monday
AI: [Shows assignment details with Create Assignment button]
```

## Tips for Best Results

### Be Specific
- Include subject, due date, and priority when asking for assignments
- Provide clear details about what you need

### Use Natural Language
- Talk to the AI naturally
- No need for special commands or syntax

### One Task at a Time
- Create one assignment per conversation
- Wait for confirmation before creating the next

### Review Before Creating
- Check the AI's suggested details
- Make sure due date and priority are correct

## Features

### ✅ Implemented
- Chat interface with message history
- Real-time AI responses
- Loading indicators
- Task creation from AI suggestions
- Error handling
- Empty state
- Dark mode support

### 🚧 Future Enhancements
- Conversation history persistence
- Voice input
- Suggested prompts
- Message editing
- Conversation export

## Architecture

### MVVM Pattern
```
AIAssistantActivity
    ↓
AIAssistantViewModel
    ↓
GeminiAssistantService
    ↓
Gemini API
```

### Data Flow
```
User Input → ViewModel → Service → API
                ↓
            LiveData
                ↓
            Activity → UI Update
```

## Performance

### Optimizations
- DiffUtil for efficient RecyclerView updates
- Coroutines for async operations
- ViewHolder pattern for view recycling
- Proper lifecycle management

### Memory Management
- ViewModel survives configuration changes
- Proper cleanup in onViewRecycled
- No memory leaks

## Accessibility

- Content descriptions for images
- Proper touch targets (48dp)
- Readable text sizes
- High contrast colors
- Keyboard navigation support

## Testing

### Manual Test Steps
1. Launch AI Assistant
2. Send a test message
3. Verify AI responds
4. Ask to create assignment
5. Verify action button appears
6. Create the assignment
7. Check Tasks list for new task

### Test Scenarios
- ✅ Normal conversation
- ✅ Task creation
- ✅ Error handling
- ✅ Loading states
- ✅ Empty state
- ✅ Dark mode
- ✅ Keyboard behavior

## Support

### Getting Help
- Check app logs for errors
- Verify API key configuration
- Test internet connection
- Review Firestore rules

### Reporting Issues
Include:
- Steps to reproduce
- Expected behavior
- Actual behavior
- Screenshots if applicable
- Device and Android version

## Summary

The AI Assistant provides an intuitive chat interface for:
- Creating assignments with natural language
- Getting study tips and organization help
- Managing tasks more efficiently

Key benefits:
- Easy to use
- Natural conversation
- Quick task creation
- Integrated with existing features
