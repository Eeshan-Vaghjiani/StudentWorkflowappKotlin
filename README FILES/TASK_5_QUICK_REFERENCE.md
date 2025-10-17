# Task 5: Profile Picture Upload - Quick Reference

## Overview
ProfileFragment now supports profile picture uploads via camera or gallery with real-time progress tracking.

## Key Files

| File | Purpose |
|------|---------|
| `ProfileFragment.kt` | Main implementation with upload logic |
| `ProfilePictureBottomSheet.kt` | UI for selecting camera/gallery |
| `StorageRepository.kt` | Firebase Storage upload handling |
| `fragment_profile.xml` | Layout with progress indicators |
| `bottom_sheet_profile_picture.xml` | Bottom sheet layout |

## How It Works

```
User taps FAB → Bottom Sheet → Choose Source → Upload → Update Firestore → Display
```

## Code Snippets

### Trigger Upload Flow
```kotlin
binding.fabEditProfilePicture.setOnClickListener {
    showProfilePictureOptions()
}
```

### Upload with Progress
```kotlin
val result = storageRepository.uploadProfilePicture(
    uri = uri,
    userId = userId,
    onProgress = { progress ->
        binding.progressUpload.progress = progress
        binding.tvUploadStatus.text = "Uploading... $progress%"
    }
)
```

### Display with Coil
```kotlin
binding.ivProfilePicture.load(photoUrl) {
    transformations(CircleCropTransformation())
    placeholder(R.drawable.ic_person)
    error(R.drawable.ic_person)
}
```

## Storage Path
```
profile_images/{userId}/{timestamp}.jpg
```

## Firestore Update
```
users/{userId}
  └─ photoUrl: "https://firebasestorage.googleapis.com/..."
```

## Image Specs
- Max dimensions: 800x800
- Quality: 85%
- Max size: 500KB
- Format: JPEG

## Permissions Required
- `CAMERA` - For taking photos
- `READ_MEDIA_IMAGES` - For gallery (Android 13+)
- `READ_EXTERNAL_STORAGE` - For gallery (Android 12-)

## Dependencies
```kotlin
implementation("io.coil-kt:coil:2.7.0")
implementation("com.google.firebase:firebase-storage-ktx")
implementation("androidx.exifinterface:exifinterface:1.3.7")
```

## Testing Commands

### Build and Install
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Check Logs
```bash
adb logcat -s ProfileFragment StorageRepository
```

## Common Customizations

### Change Image Quality
```kotlin
// In StorageRepository.uploadProfilePicture()
val compressedFile = ImageCompressor.compressImage(
    context = context,
    uri = uri,
    maxWidth = 1024,  // Change this
    maxHeight = 1024, // Change this
    quality = 90      // Change this (0-100)
)
```

### Change Max File Size
```kotlin
// In StorageRepository.uploadProfilePicture()
if (fileSize > 1024 * 1024) { // Change to 1MB
    return Result.failure(Exception("File too large"))
}
```

### Change Storage Path
```kotlin
// In StorageRepository
private const val PROFILE_IMAGES_PATH = "user_avatars" // Change this
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Camera won't open | Check CAMERA permission granted |
| Gallery won't open | Check READ_MEDIA_IMAGES permission |
| Upload fails | Check internet connection and Firebase rules |
| Image not displaying | Verify Coil dependency and URL validity |
| Progress not showing | Check layout has progress_upload view |

## Requirements Satisfied
- ✅ 4.1: Profile picture upload to Firebase Storage
- ✅ 4.3: Error messages on storage failures
- ✅ 4.5: Images load from Firebase Storage URLs

## Related Tasks
- **Task 4**: StorageRepository implementation (prerequisite)
- **Task 6**: Chat attachments (similar upload flow)
- **Task 19**: Default avatar generation (fallback for no photo)
