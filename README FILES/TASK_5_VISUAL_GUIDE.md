# Task 5: Profile Picture Upload - Visual Guide

## User Interface Flow

### 1. Profile Screen (Initial State)
```
┌─────────────────────────────────────┐
│         Profile                     │
├─────────────────────────────────────┤
│                                     │
│         ┌─────────────┐             │
│         │             │             │
│         │   [Photo]   │ 📷          │
│         │             │             │
│         └─────────────┘             │
│                                     │
│         John Doe                    │
│                                     │
│         Email:                      │
│         john.doe@example.com        │
│                                     │
│         User ID:                    │
│         abc123xyz789                │
│                                     │
│         [ Logout ]                  │
│                                     │
└─────────────────────────────────────┘
```

### 2. Bottom Sheet (After Tapping Camera FAB)
```
┌─────────────────────────────────────┐
│                                     │
│   Change Profile Picture            │
│                                     │
│   📷  Take Photo                    │
│                                     │
│   🖼️  Choose from Gallery           │
│                                     │
│   ❌  Cancel                        │
│                                     │
└─────────────────────────────────────┘
```

### 3. Upload Progress
```
┌─────────────────────────────────────┐
│         Profile                     │
├─────────────────────────────────────┤
│                                     │
│         ┌─────────────┐             │
│         │             │             │
│         │   [Photo]   │ 📷 (disabled)
│         │             │             │
│         └─────────────┘             │
│                                     │
│   [████████░░░░░░░░░░] 45%         │
│   Uploading... 45%                  │
│                                     │
│         John Doe                    │
│                                     │
└─────────────────────────────────────┘
```

### 4. Upload Complete
```
┌─────────────────────────────────────┐
│         Profile                     │
├─────────────────────────────────────┤
│                                     │
│         ┌─────────────┐             │
│         │             │             │
│         │  [New Pic]  │ 📷          │
│         │             │             │
│         └─────────────┘             │
│                                     │
│         John Doe                    │
│                                     │
│   ✅ Profile picture updated        │
│      successfully                   │
│                                     │
└─────────────────────────────────────┘
```

## Component Breakdown

### Profile Picture Container
```xml
<FrameLayout>
    <MaterialCardView (120dp x 120dp, circular)>
        <ImageView (profile picture)>
    </MaterialCardView>
    
    <FloatingActionButton (camera icon, bottom-right)>
</FrameLayout>
```

### Progress Indicators
```xml
<ProgressBar (horizontal, initially hidden)>
<TextView (status text, initially hidden)>
```

## State Diagram

```
┌─────────────┐
│   Initial   │
│   State     │
└──────┬──────┘
       │
       │ User taps FAB
       ▼
┌─────────────┐
│   Bottom    │
│   Sheet     │
└──────┬──────┘
       │
       ├─── Take Photo ───┐
       │                  │
       └─ Choose Gallery ─┤
                          │
                          ▼
                   ┌─────────────┐
                   │  Selecting  │
                   │   Image     │
                   └──────┬──────┘
                          │
                          ▼
                   ┌─────────────┐
                   │ Uploading   │
                   │ (Progress)  │
                   └──────┬──────┘
                          │
                          ├─── Success ───┐
                          │               │
                          └─── Failure ───┤
                                          │
                                          ▼
                                   ┌─────────────┐
                                   │   Final     │
                                   │   State     │
                                   └─────────────┘
```

## Data Flow

```
┌──────────────┐
│     User     │
│   Selects    │
│    Image     │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ ProfileFrag  │
│ receives URI │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│   Storage    │
│  Repository  │
│  - Compress  │
│  - Upload    │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│   Firebase   │
│   Storage    │
│  (Save file) │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  Get URL     │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  Firestore   │
│  Update      │
│  photoUrl    │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│    Coil      │
│   Loads &    │
│  Displays    │
└──────────────┘
```

## File Structure

```
app/src/main/
├── java/.../
│   ├── ProfileFragment.kt
│   │   ├── pickImageFromGallery (ActivityResultContract)
│   │   ├── takePicture (ActivityResultContract)
│   │   ├── requestCameraPermission (ActivityResultContract)
│   │   ├── showProfilePictureOptions()
│   │   ├── uploadProfilePicture(uri)
│   │   ├── updateUserPhotoUrl(userId, url)
│   │   └── displayProfilePicture(url)
│   │
│   ├── ProfilePictureBottomSheet.kt
│   │   ├── onTakePhoto callback
│   │   ├── onChooseGallery callback
│   │   └── UI bindings
│   │
│   └── repository/
│       └── StorageRepository.kt
│           ├── uploadProfilePicture(uri, userId, onProgress)
│           ├── Image compression
│           ├── Progress tracking
│           └── Error handling
│
└── res/
    ├── layout/
    │   ├── fragment_profile.xml
    │   │   ├── ImageView (profile picture)
    │   │   ├── FAB (camera button)
    │   │   ├── ProgressBar (upload progress)
    │   │   └── TextView (status text)
    │   │
    │   └── bottom_sheet_profile_picture.xml
    │       ├── Take Photo button
    │       ├── Choose Gallery button
    │       └── Cancel button
    │
    └── xml/
        └── file_paths.xml (FileProvider config)
```

## Permission Flow

```
┌─────────────────┐
│  User taps      │
│  "Take Photo"   │
└────────┬────────┘
         │
         ▼
    ┌─────────┐
    │ Check   │
    │ Camera  │
    │ Perm    │
    └────┬────┘
         │
    ┌────┴────┐
    │         │
Granted   Not Granted
    │         │
    │         ▼
    │    ┌─────────┐
    │    │ Request │
    │    │ Perm    │
    │    └────┬────┘
    │         │
    │    ┌────┴────┐
    │    │         │
    │  Grant    Deny
    │    │         │
    │    │         ▼
    │    │    ┌─────────┐
    │    │    │  Show   │
    │    │    │  Toast  │
    │    │    └─────────┘
    │    │
    └────┴────┐
              │
              ▼
         ┌─────────┐
         │ Launch  │
         │ Camera  │
         └─────────┘
```

## Error Handling Flow

```
┌─────────────┐
│   Upload    │
│   Starts    │
└──────┬──────┘
       │
       ▼
   ┌───────┐
   │  Try  │
   └───┬───┘
       │
   ┌───┴───┐
   │       │
Success  Failure
   │       │
   │       ├─── No Auth ────┐
   │       │                │
   │       ├─── No Network ─┤
   │       │                │
   │       ├─── File Too Big┤
   │       │                │
   │       └─── Other Error ┤
   │                        │
   │                        ▼
   │                 ┌─────────────┐
   │                 │ Show Error  │
   │                 │   Toast     │
   │                 └─────────────┘
   │                        │
   │                        ▼
   │                 ┌─────────────┐
   │                 │ Hide        │
   │                 │ Progress    │
   │                 └─────────────┘
   │                        │
   │                        ▼
   │                 ┌─────────────┐
   │                 │ Re-enable   │
   │                 │   FAB       │
   │                 └─────────────┘
   │
   ▼
┌─────────────┐
│ Update UI   │
│ Show Toast  │
└─────────────┘
```

## Storage Structure

```
Firebase Storage
└── profile_images/
    └── {userId}/
        ├── 1234567890.jpg (old)
        ├── 1234567891.jpg (older)
        └── 1234567892.jpg (current)
```

## Firestore Structure

```
users (collection)
└── {userId} (document)
    ├── uid: "abc123"
    ├── email: "user@example.com"
    ├── displayName: "John Doe"
    ├── photoUrl: "https://firebasestorage.googleapis.com/.../1234567892.jpg"
    ├── lastActive: Timestamp
    └── isOnline: true
```

## Image Processing Pipeline

```
Original Image
     │
     ▼
┌─────────────┐
│  Read from  │
│     URI     │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Decode     │
│  Bitmap     │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Resize to  │
│  800x800    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Compress   │
│  85% JPEG   │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Write to   │
│  Temp File  │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Upload to  │
│  Firebase   │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Delete     │
│  Temp File  │
└─────────────┘
```

## UI States

| State | FAB | Progress Bar | Status Text | Toast |
|-------|-----|--------------|-------------|-------|
| Initial | Enabled | Hidden | Hidden | None |
| Uploading | Disabled | Visible (0-100%) | "Uploading... X%" | None |
| Success | Enabled | Hidden | Hidden | "Profile picture updated successfully" |
| Error | Enabled | Hidden | Hidden | "Upload failed: {error}" |

## Color Scheme

```
Profile Picture Border: colorPrimary
FAB Background: colorPrimary
FAB Icon: white
Progress Bar: colorPrimary
Success Toast: green
Error Toast: red
```

## Accessibility

```
ImageView:
  contentDescription: "Profile Picture"

FAB:
  contentDescription: "Edit Profile Picture"

Bottom Sheet Buttons:
  - "Take Photo"
  - "Choose from Gallery"
  - "Cancel"
```

## Testing Scenarios

### Happy Path
1. User taps FAB
2. Selects "Choose from Gallery"
3. Picks an image
4. Progress shows 0% → 100%
5. Image displays
6. Success toast appears

### Error Path
1. User taps FAB
2. Selects "Take Photo"
3. Denies camera permission
4. Toast: "Camera permission is required"
5. No crash

### Edge Case
1. User starts upload
2. Presses home button
3. Returns to app
4. Upload continues or shows error
5. No crash
