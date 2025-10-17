# Task 11: Visual Testing Guide

## 🎨 Visual Testing Reference

This guide provides visual references and UI expectations for testing.

---

## 1. Authentication Screens

### Login Screen
**Expected UI Elements**:
```
┌─────────────────────────────────────┐
│         [App Logo/Icon]             │
│                                     │
│         Welcome Back!               │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ Email                         │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ Password              [👁]    │ │
│  └───────────────────────────────┘ │
│                                     │
│  [Forgot Password?]                 │
│                                     │
│  ┌───────────────────────────────┐ │
│  │        LOGIN                  │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │  [G] Sign in with Google      │ │
│  └───────────────────────────────┘ │
│                                     │
│  Don't have an account? [Sign Up]  │
└─────────────────────────────────────┘
```

**Test Points**:
- [ ] Logo displays correctly
- [ ] Email field has proper hint
- [ ] Password field has visibility toggle
- [ ] Google button has Google icon
- [ ] Colors match mockup (Primary Blue #007AFF)
- [ ] Proper spacing (16dp margins)

### Register Screen
**Expected UI Elements**:
```
┌─────────────────────────────────────┐
│         Create Account              │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ Full Name                     │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ Email                         │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ Password              [👁]    │ │
│  └───────────────────────────────┘ │
│  Password Strength: [████░░] Medium│
│                                     │
│  ┌───────────────────────────────┐ │
│  │ Confirm Password      [👁]    │ │
│  └───────────────────────────────┘ │
│                                     │
│  ☑ I agree to Terms & Conditions   │
│                                     │
│  ┌───────────────────────────────┐ │
│  │      CREATE ACCOUNT           │ │
│  └───────────────────────────────┘ │
│                                     │
│  Already have an account? [Login]  │
└─────────────────────────────────────┘
```

**Test Points**:
- [ ] Password strength indicator works
- [ ] Confirm password validation
- [ ] Terms checkbox required
- [ ] Real-time validation
- [ ] Error messages inline

---

## 2. Dashboard Screen

### Dashboard with Data
**Expected UI Elements**:
```
┌─────────────────────────────────────┐
│  ☰  Dashboard            [Profile]  │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────┐  ┌─────────────┐  │
│  │   Tasks     │  │   Groups    │  │
│  │     12      │  │      3      │  │
│  │  ↑ 2 today  │  │  2 active   │  │
│  └─────────────┘  └─────────────┘  │
│                                     │
│  ┌─────────────┐  ┌─────────────┐  │
│  │  Sessions   │  │  AI Usage   │  │
│  │   45 min    │  │    5/10     │  │
│  │  ↑ 3 today  │  │  prompts    │  │
│  └─────────────┘  └─────────────┘  │
│                                     │
│  Recent Activity                    │
│  ┌───────────────────────────────┐ │
│  │ 📝 Math Assignment due today  │ │
│  │ 👥 New message in Study Group │ │
│  │ ✅ Completed Physics Quiz     │ │
│  └───────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

**Test Points**:
- [ ] Real numbers (not 0 or demo data)
- [ ] Stats update in real-time
- [ ] Cards have proper elevation
- [ ] Icons display correctly
- [ ] Recent activity shows actual events

### Dashboard Empty State
**Expected UI Elements**:
```
┌─────────────────────────────────────┐
│  ☰  Dashboard            [Profile]  │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────┐  ┌─────────────┐  │
│  │   Tasks     │  │   Groups    │  │
│  │      0      │  │      0      │  │
│  │  No tasks   │  │  No groups  │  │
│  └─────────────┘  └─────────────┘  │
│                                     │
│         [Illustration]              │
│                                     │
│      Get Started!                   │
│   Create your first task or         │
│   join a study group                │
│                                     │
│  ┌───────────────────────────────┐ │
│  │      CREATE TASK              │ │
│  └───────────────────────────────┘ │
│  ┌───────────────────────────────┐ │
│  │      JOIN GROUP               │ │
│  └───────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

**Test Points**:
- [ ] Shows 0 values
- [ ] Empty state message displays
- [ ] CTA buttons present
- [ ] No demo data shown

---

## 3. Groups Screen

### Groups List
**Expected UI Elements**:
```
┌─────────────────────────────────────┐
│  ←  Groups              [+ Create]  │
├─────────────────────────────────────┤
│                                     │
│  ┌───────────────────────────────┐ │
│  │ 📚 Study Group A              │ │
│  │ Mathematics • 5 members       │ │
│  │ Last active: 2 hours ago      │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ 🔬 Physics Lab Team           │ │
│  │ Physics • 8 members           │ │
│  │ Last active: 1 day ago        │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ 💻 CS Project Group           │ │
│  │ Computer Science • 4 members  │ │
│  │ Last active: 3 hours ago      │ │
│  └───────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

**Test Points**:
- [ ] All user's groups display
- [ ] Group info accurate
- [ ] Pull-to-refresh works
- [ ] Tap opens group details

### Groups Empty State
**Expected UI Elements**:
```
┌─────────────────────────────────────┐
│  ←  Groups              [+ Create]  │
├─────────────────────────────────────┤
│                                     │
│                                     │
│         [Illustration]              │
│                                     │
│      No Groups Yet                  │
│   Join or create a study group      │
│   to collaborate with others        │
│                                     │
│  ┌───────────────────────────────┐ │
│  │      CREATE GROUP             │ │
│  └───────────────────────────────┘ │
│  ┌───────────────────────────────┐ │
│  │      JOIN BY CODE             │ │
│  └───────────────────────────────┘ │
│                                     │
│                                     │
└─────────────────────────────────────┘
```

**Test Points**:
- [ ] Empty state displays
- [ ] CTA buttons work
- [ ] No demo groups shown

---

## 4. Tasks Screen

### Tasks List
**Expected UI Elements**:
```
┌─────────────────────────────────────┐
│  ←  Tasks               [+ Add]     │
├─────────────────────────────────────┤
│  [All] [Personal] [Group] [Assign]  │
├─────────────────────────────────────┤
│                                     │
│  ┌───────────────────────────────┐ │
│  │ ☐ Math Assignment             │ │
│  │ Due: Today, 5:00 PM  🔴 High  │ │
│  │ Study Group A                 │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ ☐ Read Chapter 5              │ │
│  │ Due: Tomorrow  🟡 Medium      │ │
│  │ Personal                      │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ ☑ Physics Quiz                │ │
│  │ Completed  🟢 Low             │ │
│  │ Physics Lab Team              │ │
│  └───────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

**Test Points**:
- [ ] All assigned tasks display
- [ ] Category filters work
- [ ] Priority colors correct
- [ ] Due dates accurate
- [ ] Checkbox toggles status

---

## 5. Calendar Screen

### Calendar with Tasks
**Expected UI Elements**:
```
┌─────────────────────────────────────┐
│  ←  Calendar                        │
├─────────────────────────────────────┤
│         October 2025                │
│  Su Mo Tu We Th Fr Sa               │
│              1  2  3  4             │
│   5  6  7  8  9 10 11               │
│  12 13 14 15 [16]17 18              │
│  19 20 21 22 23 24 25               │
│  26 27 28 29 30 31                  │
│   •     •  •     •                  │
├─────────────────────────────────────┤
│  Tasks for October 16               │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ 📝 Math Assignment            │ │
│  │ 5:00 PM • High Priority       │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ 📚 Study Session              │ │
│  │ 7:00 PM • Medium Priority     │ │
│  └───────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

**Test Points**:
- [ ] Current date highlighted
- [ ] Dots show on dates with tasks
- [ ] Selected date shows tasks
- [ ] Month navigation works
- [ ] Task list updates on date change

---

## 6. Chat Screen

### Chat List
**Expected UI Elements**:
```
┌─────────────────────────────────────┐
│  ←  Chats               [+ New]     │
├─────────────────────────────────────┤
│                                     │
│  ┌───────────────────────────────┐ │
│  │ [👤] John Doe                 │ │
│  │ Hey, did you finish the...    │ │
│  │ 2:30 PM                    [1]│ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ [👥] Study Group A            │ │
│  │ Sarah: Meeting at 5pm         │ │
│  │ Yesterday                  [3]│ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ [👤] Jane Smith               │ │
│  │ Thanks for the notes!         │ │
│  │ Oct 14                        │ │
│  └───────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

**Test Points**:
- [ ] All chats display
- [ ] Last message preview
- [ ] Unread count badge
- [ ] Timestamps accurate
- [ ] Profile pictures load

### Chat Room
**Expected UI Elements**:
```
┌─────────────────────────────────────┐
│  ←  John Doe            [⋮]         │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────┐       │
│  │ Hey, did you finish     │       │
│  │ the assignment?         │       │
│  │                   2:30 PM       │
│  └─────────────────────────┘       │
│                                     │
│       ┌─────────────────────────┐  │
│       │ Yes, just submitted it! │  │
│       │ 2:32 PM                 │  │
│       └─────────────────────────┘  │
│                                     │
│  ┌─────────────────────────┐       │
│  │ Great! Want to study    │       │
│  │ for the quiz tomorrow?  │       │
│  │                   2:35 PM       │
│  └─────────────────────────┘       │
│                                     │
├─────────────────────────────────────┤
│  [+] Type a message...      [Send]  │
└─────────────────────────────────────┘
```

**Test Points**:
- [ ] Messages display chronologically
- [ ] Sent messages align right
- [ ] Received messages align left
- [ ] Timestamps show
- [ ] New messages appear in real-time
- [ ] Send button works

---

## 7. Error States

### Network Error
**Expected UI Elements**:
```
┌─────────────────────────────────────┐
│                                     │
│         [No Connection Icon]        │
│                                     │
│      No Internet Connection         │
│   Please check your connection      │
│   and try again                     │
│                                     │
│  ┌───────────────────────────────┐ │
│  │         RETRY                 │ │
│  └───────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

### Form Validation Error
**Expected UI Elements**:
```
┌─────────────────────────────────────┐
│  ┌───────────────────────────────┐ │
│  │ Email                         │ │
│  └───────────────────────────────┘ │
│  ⚠ Invalid email format            │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ Password              [👁]    │ │
│  └───────────────────────────────┘ │
│  ⚠ Password must be at least 8     │
│    characters                       │
└─────────────────────────────────────┘
```

**Test Points**:
- [ ] Error icon displays
- [ ] Error message clear
- [ ] Red color for errors
- [ ] Retry button available

---

## 8. Loading States

### Skeleton Loader
**Expected UI Elements**:
```
┌─────────────────────────────────────┐
│  ┌───────────────────────────────┐ │
│  │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ │ │
│  │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ │ │
│  │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ │ │
│  └───────────────────────────────┘ │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ │ │
│  │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ │ │
│  │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ │ │
│  └───────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

### Progress Indicator
**Expected UI Elements**:
```
┌─────────────────────────────────────┐
│                                     │
│         [Circular Progress]         │
│                                     │
│         Loading...                  │
│                                     │
└─────────────────────────────────────┘
```

**Test Points**:
- [ ] Skeleton shows while loading
- [ ] Smooth transition to content
- [ ] Progress indicator animates
- [ ] Loading text displays

---

## 9. Success Feedback

### Toast Message
```
┌─────────────────────────────────────┐
│                                     │
│  ┌───────────────────────────────┐ │
│  │ ✓ Task created successfully   │ │
│  └───────────────────────────────┘ │
│                                     │
└─────────────────────────────────────┘
```

### Snackbar
```
┌─────────────────────────────────────┐
│                                     │
│                                     │
│  ┌───────────────────────────────┐ │
│  │ ✓ Message sent      [UNDO]    │ │
│  └───────────────────────────────┘ │
└─────────────────────────────────────┘
```

**Test Points**:
- [ ] Success icon shows
- [ ] Message is clear
- [ ] Green color for success
- [ ] Auto-dismisses after 3-5 seconds

---

## 10. Testing Activity

### Testing UI
**Expected UI Elements**:
```
┌─────────────────────────────────────┐
│  App Testing & Verification         │
├─────────────────────────────────────┤
│                                     │
│  Comprehensive Testing Suite        │
│                                     │
│  This will test all critical app   │
│  features including authentication, │
│  data fetching, groups, tasks, and  │
│  chat functionality.                │
│                                     │
│  ┌───────────────────────────────┐ │
│  │      RUN ALL TESTS            │ │
│  └───────────────────────────────┘ │
│                                     │
│  [Progress Bar]                     │
│                                     │
├─────────────────────────────────────┤
│  Test Results:                      │
│                                     │
│  ✓ PASS - Authentication Flow       │
│    User authenticated: John Doe     │
│                                     │
│  ✓ PASS - Dashboard Data Sources    │
│    Tasks: 12, Groups: 3, AI: 5/10   │
│                                     │
│  ✓ PASS - Groups Functionality      │
│    3 groups found                   │
│                                     │
│  [Scrollable results...]            │
│                                     │
└─────────────────────────────────────┘
```

**Test Points**:
- [ ] Button launches tests
- [ ] Progress bar shows
- [ ] Results display clearly
- [ ] Pass/fail indicators correct
- [ ] Scrollable results area

---

## Color Reference

### Primary Colors
- **Primary Blue**: #007AFF
- **Success Green**: #34C759
- **Warning Orange**: #FF9500
- **Danger Red**: #FF3B30

### Text Colors
- **Text Primary**: #1D1D1F
- **Text Secondary**: #8E8E93

### Background Colors
- **Background**: #F2F2F7
- **Border**: #E5E5EA

---

## Spacing Guidelines

- **Screen Padding**: 16dp
- **Card Margin**: 12dp
- **Element Spacing**: 8dp
- **Section Spacing**: 24dp
- **Button Height**: 48dp
- **Input Height**: 56dp

---

## Typography

- **Heading 1**: 24sp, Bold
- **Heading 2**: 20sp, Bold
- **Body**: 16sp, Regular
- **Caption**: 14sp, Regular
- **Small**: 12sp, Regular

---

## Testing Checklist

Use this visual guide to verify:

- [ ] All screens match expected layouts
- [ ] Colors match specifications
- [ ] Spacing is consistent
- [ ] Icons display correctly
- [ ] Text is readable
- [ ] Buttons are properly sized
- [ ] Loading states show correctly
- [ ] Error states display properly
- [ ] Empty states are helpful
- [ ] Success feedback is clear

---

## 📸 Screenshot Recommendations

Take screenshots of:
1. Login screen
2. Dashboard with data
3. Dashboard empty state
4. Groups list
5. Tasks list with filters
6. Calendar with tasks
7. Chat room
8. Error states
9. Loading states
10. Testing activity results

---

**Use this guide alongside the comprehensive testing guide for complete verification.**
