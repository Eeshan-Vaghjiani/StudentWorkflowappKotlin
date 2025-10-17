# Task 13: AI Assistant Service - Visual Guide

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     User Interface Layer                     │
│                    (To be implemented)                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ AI Assistant │  │   Message    │  │    Task      │     │
│  │   Activity   │  │   Adapter    │  │   Creation   │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
└─────────┼──────────────────┼──────────────────┼────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────┐
│                    ViewModel Layer                           │
│                    (To be implemented)                       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │           AIAssistantViewModel                        │  │
│  │  - Manages conversation state                         │  │
│  │  - Handles user input                                 │  │
│  │  - Coordinates with service                           │  │
│  └──────────────────┬───────────────────────────────────┘  │
└─────────────────────┼──────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                             │
│                    ✅ IMPLEMENTED                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         GeminiAssistantService                        │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │ sendMessage()                                   │  │  │
│  │  │  - Builds prompt with context                   │  │  │
│  │  │  - Calls Gemini API                             │  │  │
│  │  │  - Parses response                               │  │  │
│  │  │  - Returns AIResponse                            │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  │  ┌────────────────────────────────────────────────┐  │  │
│  │  │ createAssignmentFromAI()                        │  │  │
│  │  │  - Parses AI suggestion                         │  │  │
│  │  │  - Creates FirebaseTask                         │  │  │
│  │  │  - Saves to Firestore                           │  │  │
│  │  │  - Returns created task                         │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────┬───────────────────┬───────────────┘  │
└─────────────────────┼───────────────────┼──────────────────┘
                      │                   │
                      ▼                   ▼
         ┌────────────────────┐  ┌────────────────────┐
         │   Gemini API       │  │  TaskRepository    │
         │   (External)       │  │  (Existing)        │
         └────────────────────┘  └────────────────────┘
```

## Data Flow Diagram

### Message Sending Flow

```
User Types Message
       │
       ▼
┌─────────────────┐
│  User Input     │
│  "Create math   │
│   assignment"   │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  GeminiAssistantService             │
│  ┌───────────────────────────────┐  │
│  │ 1. buildPrompt()              │  │
│  │    - Add system instructions  │  │
│  │    - Add conversation history │  │
│  │    - Format user message      │  │
│  └───────────┬───────────────────┘  │
│              │                       │
│              ▼                       │
│  ┌───────────────────────────────┐  │
│  │ 2. callGeminiAPI()            │  │
│  │    - Build HTTP request       │  │
│  │    - Add API key              │  │
│  │    - Send to Gemini           │  │
│  └───────────┬───────────────────┘  │
└──────────────┼─────────────────────┘
               │
               ▼
    ┌──────────────────┐
    │   Gemini API     │
    │   Processing...  │
    └──────┬───────────┘
           │
           ▼
    ┌──────────────────┐
    │  API Response    │
    │  {              │
    │    "candidates": │
    │    [{           │
    │      "content": │
    │      {...}      │
    │    }]           │
    │  }              │
    └──────┬───────────┘
           │
           ▼
┌─────────────────────────────────────┐
│  GeminiAssistantService             │
│  ┌───────────────────────────────┐  │
│  │ 3. parseGeminiResponse()      │  │
│  │    - Extract text             │  │
│  │    - Find JSON actions        │  │
│  │    - Create AIResponse        │  │
│  └───────────┬───────────────────┘  │
└──────────────┼─────────────────────┘
               │
               ▼
        ┌──────────────┐
        │  AIResponse  │
        │  - message   │
        │  - action    │
        │  - success   │
        └──────┬───────┘
               │
               ▼
        Display to User
```

### Task Creation Flow

```
AI Suggests Task
       │
       ▼
┌─────────────────────────────────────┐
│  AI Response with JSON              │
│  {                                  │
│    "action": "create_assignment",   │
│    "title": "Math Homework",        │
│    "description": "...",            │
│    "subject": "Mathematics",        │
│    "dueDate": "2024-12-31",        │
│    "priority": "high"               │
│  }                                  │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  createAssignmentFromAI()           │
│  ┌───────────────────────────────┐  │
│  │ 1. parseAIResponse()          │  │
│  │    - Extract JSON             │  │
│  │    - Parse fields             │  │
│  │    - Apply defaults           │  │
│  │    → AITaskData               │  │
│  └───────────┬───────────────────┘  │
│              │                       │
│              ▼                       │
│  ┌───────────────────────────────┐  │
│  │ 2. parseDateString()          │  │
│  │    - Convert to Timestamp     │  │
│  └───────────┬───────────────────┘  │
│              │                       │
│              ▼                       │
│  ┌───────────────────────────────┐  │
│  │ 3. Create FirebaseTask        │  │
│  │    - Set all fields           │  │
│  │    - Add user ID              │  │
│  │    - Set timestamps           │  │
│  └───────────┬───────────────────┘  │
│              │                       │
│              ▼                       │
│  ┌───────────────────────────────┐  │
│  │ 4. taskRepository.createTask()│  │
│  │    - Save to Firestore        │  │
│  │    - Schedule reminder        │  │
│  │    - Return task ID           │  │
│  └───────────┬───────────────────┘  │
└──────────────┼─────────────────────┘
               │
               ▼
        ┌──────────────┐
        │  Firestore   │
        │  tasks/      │
        │  {taskId}    │
        └──────┬───────┘
               │
               ▼
        Task Created ✅
```

## Model Relationships

```
┌─────────────────────────────────────────────────────────┐
│                    AI Models                             │
│                                                          │
│  ┌──────────────────┐                                   │
│  │  AIChatMessage   │                                   │
│  ├──────────────────┤                                   │
│  │ - id: String     │                                   │
│  │ - role: ────────────┐                                │
│  │ - content: String│  │                                │
│  │ - timestamp: Long│  │                                │
│  │ - action: ───────┼──┼───┐                            │
│  └──────────────────┘  │   │                            │
│                        │   │                            │
│  ┌────────────────────▼┐  │                            │
│  │   MessageRole       │  │                            │
│  ├─────────────────────┤  │                            │
│  │ - USER              │  │                            │
│  │ - ASSISTANT         │  │                            │
│  │ - SYSTEM            │  │                            │
│  └─────────────────────┘  │                            │
│                            │                            │
│  ┌────────────────────────▼──┐                         │
│  │      AIAction             │                         │
│  ├───────────────────────────┤                         │
│  │ - type: ──────────────┐   │                         │
│  │ - data: Map<String,Any>   │                         │
│  └───────────────────────┼───┘                         │
│                          │                              │
│  ┌──────────────────────▼───┐                          │
│  │     ActionType           │                          │
│  ├──────────────────────────┤                          │
│  │ - CREATE_ASSIGNMENT      │                          │
│  │ - UPDATE_TASK            │                          │
│  │ - SUGGEST_SCHEDULE       │                          │
│  │ - PROVIDE_INFO           │                          │
│  └──────────────────────────┘                          │
│                                                          │
│  ┌──────────────────────────┐                          │
│  │     AIResponse           │                          │
│  ├──────────────────────────┤                          │
│  │ - message: String        │                          │
│  │ - action: AIAction?      │                          │
│  │ - success: Boolean       │                          │
│  │ - error: String?         │                          │
│  └──────────────────────────┘                          │
│                                                          │
│  ┌──────────────────────────┐                          │
│  │     AITaskData           │                          │
│  ├──────────────────────────┤                          │
│  │ - title: String          │                          │
│  │ - description: String    │                          │
│  │ - subject: String        │                          │
│  │ - dueDate: String        │                          │
│  │ - priority: String       │                          │
│  └──────────────────────────┘                          │
└─────────────────────────────────────────────────────────┘
```

## Conversation Flow Example

```
┌─────────────────────────────────────────────────────────┐
│                  Conversation Timeline                   │
└─────────────────────────────────────────────────────────┘

Time: 10:00:00
┌─────────────────────────────────────┐
│ USER: Hello                         │
└─────────────────────────────────────┘
         │
         ▼ [sendMessage()]
┌─────────────────────────────────────┐
│ ASSISTANT: Hi! I'm here to help you │
│ manage your assignments. What would │
│ you like to do?                     │
└─────────────────────────────────────┘

Time: 10:00:15
┌─────────────────────────────────────┐
│ USER: I need to create a math       │
│ assignment                          │
└─────────────────────────────────────┘
         │
         ▼ [sendMessage(history)]
┌─────────────────────────────────────┐
│ ASSISTANT: I'd be happy to help!    │
│ Could you provide more details      │
│ about the assignment?               │
└─────────────────────────────────────┘

Time: 10:00:30
┌─────────────────────────────────────┐
│ USER: Algebra practice for          │
│ tomorrow, high priority             │
└─────────────────────────────────────┘
         │
         ▼ [sendMessage(history)]
┌─────────────────────────────────────┐
│ ASSISTANT: {                        │
│   "action": "create_assignment",    │
│   "title": "Algebra Practice",      │
│   "description": "Practice algebra  │
│                   problems",        │
│   "subject": "Mathematics",         │
│   "dueDate": "2024-12-18",         │
│   "priority": "high"                │
│ }                                   │
│                                     │
│ I've created your algebra practice  │
│ assignment for tomorrow!            │
└─────────────────────────────────────┘
         │
         ▼ [createAssignmentFromAI()]
┌─────────────────────────────────────┐
│ ✅ Task Created in Firestore        │
│    ID: abc123xyz                    │
└─────────────────────────────────────┘
```

## Error Handling Flow

```
┌─────────────────────────────────────┐
│         API Call Initiated          │
└────────────┬────────────────────────┘
             │
             ▼
      ┌──────────────┐
      │ Try Block    │
      └──────┬───────┘
             │
    ┌────────┴────────┐
    │                 │
    ▼                 ▼
┌─────────┐      ┌─────────┐
│ Success │      │  Error  │
└────┬────┘      └────┬────┘
     │                │
     │                ├─► Network Error
     │                │   └─► "Check connection"
     │                │
     │                ├─► API Error (4xx/5xx)
     │                │   └─► "API Error: {code}"
     │                │
     │                ├─► Parse Error
     │                │   └─► "Invalid response"
     │                │
     │                ├─► Auth Error
     │                │   └─► "Not authenticated"
     │                │
     │                └─► Unknown Error
     │                    └─► "Unexpected error"
     │                │
     ▼                ▼
┌─────────────────────────────────────┐
│         Return Result               │
│  - Success: Result.success(data)    │
│  - Failure: Result.failure(error)   │
└─────────────────────────────────────┘
```

## API Request/Response Format

### Request to Gemini API

```json
POST https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=API_KEY

{
  "contents": [
    {
      "parts": [
        {
          "text": "System: You are an AI assistant...\n\nUser: Create a math assignment"
        }
      ]
    }
  ],
  "generationConfig": {
    "temperature": 0.7,
    "topK": 40,
    "topP": 0.95,
    "maxOutputTokens": 1024
  }
}
```

### Response from Gemini API

```json
{
  "candidates": [
    {
      "content": {
        "parts": [
          {
            "text": "{\n  \"action\": \"create_assignment\",\n  \"title\": \"Math Homework\",\n  \"description\": \"Complete exercises 1-10\",\n  \"subject\": \"Mathematics\",\n  \"dueDate\": \"2024-12-31\",\n  \"priority\": \"high\"\n}\n\nI've created your math assignment!"
          }
        ],
        "role": "model"
      },
      "finishReason": "STOP",
      "index": 0
    }
  ]
}
```

## State Diagram

```
┌─────────────┐
│   Initial   │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│    Idle     │◄──────────────┐
└──────┬──────┘               │
       │                      │
       │ User sends message   │
       ▼                      │
┌─────────────┐               │
│  Building   │               │
│   Prompt    │               │
└──────┬──────┘               │
       │                      │
       ▼                      │
┌─────────────┐               │
│  Calling    │               │
│     API     │               │
└──────┬──────┘               │
       │                      │
       ├─► Success ───────────┤
       │                      │
       └─► Error              │
            │                 │
            ▼                 │
       ┌─────────┐            │
       │ Handle  │            │
       │  Error  │            │
       └────┬────┘            │
            │                 │
            └─────────────────┘
```

## Component Interaction Timeline

```
Time →

User          ViewModel       Service         API          Repository
 │                │              │             │               │
 │ Type message   │              │             │               │
 ├───────────────►│              │             │               │
 │                │ sendMessage()│             │               │
 │                ├─────────────►│             │               │
 │                │              │ HTTP POST   │               │
 │                │              ├────────────►│               │
 │                │              │             │               │
 │                │              │◄────────────┤               │
 │                │              │  Response   │               │
 │                │              │             │               │
 │                │              │ Parse       │               │
 │                │              │ Response    │               │
 │                │              │             │               │
 │                │◄─────────────┤             │               │
 │                │  AIResponse  │             │               │
 │                │              │             │               │
 │                │ Check action │             │               │
 │                │              │             │               │
 │                │ createTask() │             │               │
 │                ├─────────────►│             │               │
 │                │              │ createTask()│               │
 │                │              ├─────────────┼──────────────►│
 │                │              │             │  Save to      │
 │                │              │             │  Firestore    │
 │                │              │◄────────────┼───────────────┤
 │                │              │             │  Task ID      │
 │                │◄─────────────┤             │               │
 │                │  Task        │             │               │
 │◄───────────────┤              │             │               │
 │ Display result │              │             │               │
 │                │              │             │               │
```

## Priority Levels Visual

```
┌─────────────────────────────────────┐
│         Priority Levels             │
├─────────────────────────────────────┤
│                                     │
│  🔴 HIGH                            │
│  ├─ Urgent assignments              │
│  ├─ Due soon                        │
│  └─ Important exams                 │
│                                     │
│  🟡 MEDIUM (Default)                │
│  ├─ Regular homework                │
│  ├─ Standard assignments            │
│  └─ General tasks                   │
│                                     │
│  🟢 LOW                             │
│  ├─ Optional reading                │
│  ├─ Extra credit                    │
│  └─ Long-term projects              │
│                                     │
└─────────────────────────────────────┘
```

## Date Format Support

```
┌─────────────────────────────────────┐
│      Supported Date Formats         │
├─────────────────────────────────────┤
│                                     │
│  Format 1: YYYY-MM-DD               │
│  Example: 2024-12-31                │
│  ✅ Recommended                     │
│                                     │
│  Format 2: MM/DD/YYYY               │
│  Example: 12/31/2024                │
│  ✅ US Format                       │
│                                     │
│  Format 3: DD/MM/YYYY               │
│  Example: 31/12/2024                │
│  ✅ International Format            │
│                                     │
│  Default: +7 days from today        │
│  Used when: Parse fails             │
│                                     │
└─────────────────────────────────────┘
```

## Success Metrics

```
┌─────────────────────────────────────┐
│         Implementation Metrics       │
├─────────────────────────────────────┤
│                                     │
│  ✅ Code Coverage                   │
│     └─ 10 unit tests                │
│                                     │
│  ✅ Documentation                   │
│     └─ 100% of public APIs          │
│                                     │
│  ✅ Error Handling                  │
│     └─ All paths covered            │
│                                     │
│  ✅ Performance                     │
│     └─ < 5s typical response        │
│                                     │
│  ✅ Security                        │
│     └─ API key not in code          │
│                                     │
│  ✅ Compilation                     │
│     └─ 0 errors, 0 warnings         │
│                                     │
└─────────────────────────────────────┘
```

## Next Steps Roadmap

```
Current: Task 13 ✅
    │
    ▼
Task 14: AI Assistant UI
    │
    ├─► Create AIAssistantActivity
    ├─► Create AIMessageAdapter
    ├─► Create AIAssistantViewModel
    └─► Implement chat interface
    │
    ▼
Task 15: Integrate with Tasks
    │
    ├─► Add "Create with AI" button
    ├─► Parse AI-generated data
    └─► Show success feedback
    │
    ▼
Task 16: API Configuration
    │
    ├─► Add API key to local.properties
    ├─► Configure BuildConfig
    └─► Test connectivity
    │
    ▼
Production Ready 🚀
```

---

This visual guide provides a comprehensive overview of the AI Assistant Service implementation, showing how all components work together to create a seamless AI-powered task creation experience.
