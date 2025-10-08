# Task 2: Chat List Screen - User Guide

## What Was Built

A fully functional chat list screen that displays all your conversations with real-time updates, search, and filtering capabilities.

## Features

### 1. Chat List Display
- **Profile Pictures**: Shows user profile pictures or generated avatars with initials
- **Chat Names**: Displays group names or participant names for direct chats
- **Last Message**: Preview of the most recent message
- **Timestamps**: Smart formatting (e.g., "2:30 PM", "Yesterday", "Monday", "Jan 15")
- **Unread Badges**: Blue circular badges showing unread message count

### 2. Search Bar
- Located at the top of the screen
- Search by chat name or message content
- Real-time filtering as you type
- Clear icon to reset search

### 3. Filter Tabs
Three tabs to filter your chats:
- **All**: Shows all conversations
- **Groups**: Shows only group chats
- **Direct**: Shows only one-on-one conversations

### 4. Pull-to-Refresh
- Pull down on the chat list to refresh
- Shows a loading indicator while refreshing
- Updates with latest data from server

### 5. Empty State
When you have no chats, you'll see:
- A friendly icon
- "No chats yet" message
- Helpful text explaining how to start a conversation

### 6. New Chat Button
- Blue floating action button in the bottom-right corner
- Tap to start a new conversation (feature coming in Task 5)

## How to Use

### Viewing Chats
1. Open the app and navigate to the "Chat" tab
2. Your conversations will appear in a list, sorted by most recent
3. Scroll through your chats

### Searching for Chats
1. Tap the search bar at the top
2. Type a name or message content
3. The list filters in real-time
4. Clear the search to see all chats again

### Filtering by Type
1. Tap one of the three tabs:
   - "All" - See everything
   - "Groups" - See only group conversations
   - "Direct" - See only one-on-one chats
2. The list updates immediately

### Refreshing
1. Pull down on the chat list
2. Release to refresh
3. Wait for the loading indicator to disappear

### Opening a Chat
1. Tap any chat in the list
2. The chat room will open (implemented in Task 3)

## Visual Elements

### Chat Item Layout
```
┌─────────────────────────────────────────┐
│  [Avatar]  John Doe          2:30 PM    │
│            Hey, how are you?      [3]   │
└─────────────────────────────────────────┘
```

- **Avatar**: Circle with profile picture or colored initials
- **Name**: Bold text showing chat name
- **Timestamp**: Right-aligned, shows when last message was sent
- **Message Preview**: Gray text showing last message
- **Unread Badge**: Blue circle with number (only if unread messages exist)

### Color-Coded Avatars
When users don't have profile pictures, the app generates colorful avatars:
- Each user gets a consistent color based on their ID
- Shows first letters of their name
- 10 different colors for variety

### Timestamp Formatting
- **Today**: Shows time (e.g., "2:30 PM")
- **Yesterday**: Shows "Yesterday"
- **This Week**: Shows day name (e.g., "Monday")
- **This Year**: Shows date (e.g., "Jan 15")
- **Older**: Shows full date (e.g., "Jan 15, 2024")

## Technical Details

### Real-Time Updates
- Chats update automatically when new messages arrive
- No need to manually refresh
- Uses Firestore real-time listeners

### Performance
- Efficient list updates (only changed items refresh)
- Image caching for fast loading
- Smooth scrolling even with many chats

### Offline Support
- Cached chats display when offline
- Updates sync when connection restored
- (Full offline support in Task 6)

## What's Next

### Task 3: Chat Room Screen
- Open individual chats
- Send and receive messages
- View message history

### Task 5: User Search
- Search for users to start new chats
- Create direct message conversations

## Troubleshooting

### No Chats Showing
- Make sure you're logged in
- Check your internet connection
- Try pull-to-refresh
- Verify you have chats in Firestore

### Search Not Working
- Make sure you're typing in the search bar
- Check that you have chats matching your search
- Clear the search and try again

### Images Not Loading
- Check internet connection
- Images are cached after first load
- Default avatars show if no profile picture

## Developer Notes

### Files Modified/Created
- `ChatFragment.kt` - Main fragment
- `ChatAdapter.kt` - RecyclerView adapter
- `ChatViewModel.kt` - ViewModel for state management
- `fragment_chat.xml` - Layout file
- `item_chat.xml` - Chat item layout
- `circle_background.xml` - Avatar drawable

### Dependencies Used
- Kotlin Coroutines & Flow
- Coil for image loading
- Material Design Components
- SwipeRefreshLayout
- RecyclerView with ListAdapter

### Architecture
- MVVM pattern
- Repository pattern for data access
- Lifecycle-aware components
- Real-time data with Firestore listeners
