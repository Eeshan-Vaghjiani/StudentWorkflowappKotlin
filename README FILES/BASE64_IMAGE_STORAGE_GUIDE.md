# Base64 Image Storage Guide

## Overview

This application stores images as Base64-encoded strings directly in Firestore documents instead of using Firebase Storage. This approach was chosen for simplicity and to avoid additional storage costs.

## Why Base64 in Firestore?

### Advantages
1. **Simplicity**: No need to manage separate Storage buckets and security rules
2. **Atomic Operations**: Images are stored with their associated data in a single document
3. **Offline Support**: Images are automatically cached with Firestore offline persistence
4. **No Additional Costs**: Firestore storage is included in the free tier
5. **Easier Security**: Security is handled entirely through Firestore rules

### Disadvantages
1. **Size Limitations**: Firestore documents have a 1MB limit
2. **Bandwidth**: Base64 encoding increases data size by ~33%
3. **Performance**: Large Base64 strings can slow down queries
4. **Not Ideal for Large Images**: Best for small images only

## Implementation Details

### Size Limits

We enforce strict size limits to ensure images fit in Firestore documents:

- **Profile Pictures**: 200KB (Base64 encoded)
- **Chat Images**: 1MB (Base64 encoded)

These limits ensure:
- Documents stay well under Firestore's 1MB limit
- Fast loading and good performance
- Reasonable bandwidth usage

### Compression Strategy

Images are automatically compressed before encoding:

1. **Profile Pictures**:
   - Max dimensions: 400x400 pixels
   - JPEG quality: 70%
   - Target size: < 200KB

2. **Chat Images**:
   - Max dimensions: 800x800 pixels
   - JPEG quality: 70%
   - Target size: < 1MB

### Utilities

#### Base64Helper

Main utility for encoding/decoding images:

```kotlin
// Encode image to Base64
val result = Base64Helper.encodeImageToBase64(
    context = context,
    uri = imageUri,
    maxSizeBytes = Base64Helper.MAX_BASE64_SIZE_BYTES
)

// Decode Base64 to Bitmap
val bitmap = Base64Helper.decodeBase64ToImage(base64String)
```

#### Base64Validator

Validates Base64 data before saving:

```kotlin
// Validate profile image
val validation = Base64Validator.validateProfileImage(base64Data)
if (validation.isError()) {
    // Handle error
}

// Validate chat image
val validation = Base64Validator.validateChatImage(base64Data)
if (validation.isError()) {
    // Handle error
}
```

#### ImageCompressor

Compresses images before encoding:

```kotlin
val compressedFile = ImageCompressor.compressImageForBase64(
    context = context,
    uri = imageUri,
    maxWidth = 800,
    maxHeight = 800,
    quality = 70
)
```

## Usage Examples

### Upload Profile Picture

```kotlin
suspend fun uploadProfilePicture(context: Context, imageUri: Uri, userId: String) {
    // 1. Encode to Base64
    val encodeResult = Base64Helper.encodeImageToBase64(
        context = context,
        uri = imageUri,
        maxSizeBytes = Base64Helper.MAX_PROFILE_BASE64_SIZE_BYTES
    )
    
    if (encodeResult.isFailure) {
        // Handle error
        return
    }
    
    val base64Data = encodeResult.getOrThrow()
    
    // 2. Validate
    val validation = Base64Validator.validateProfileImage(base64Data)
    if (validation.isError()) {
        // Handle error
        return
    }
    
    // 3. Save to Firestore
    firestore.collection("users")
        .document(userId)
        .update("profileImageBase64", base64Data)
        .await()
}
```

### Send Chat Image

```kotlin
suspend fun sendImageMessage(context: Context, chatId: String, imageUri: Uri) {
    // 1. Encode to Base64
    val encodeResult = Base64Helper.encodeImageToBase64(
        context = context,
        uri = imageUri,
        maxSizeBytes = Base64Helper.MAX_BASE64_SIZE_BYTES
    )
    
    if (encodeResult.isFailure) {
        // Handle error
        return
    }
    
    val base64Data = encodeResult.getOrThrow()
    
    // 2. Validate
    val validation = Base64Validator.validateChatImage(base64Data)
    if (validation.isError()) {
        // Handle error
        return
    }
    
    // 3. Create message with Base64 data
    val message = Message(
        id = messageId,
        chatId = chatId,
        senderId = currentUserId,
        base64ImageData = base64Data,
        timestamp = Date()
    )
    
    // 4. Save to Firestore
    firestore.collection("chats")
        .document(chatId)
        .collection("messages")
        .document(messageId)
        .set(message)
        .await()
}
```

### Display Image from Base64

```kotlin
fun displayImage(imageView: ImageView, base64Data: String) {
    if (base64Data.isEmpty()) {
        // Show placeholder
        return
    }
    
    // Decode Base64 to Bitmap
    val result = Base64Helper.decodeBase64ToImage(base64Data)
    
    result.onSuccess { bitmap ->
        imageView.setImageBitmap(bitmap)
    }.onFailure { error ->
        // Show error placeholder
    }
}
```

### Using Coil with Base64

```kotlin
// Load Base64 image with Coil
imageView.load(base64Data) {
    decoder(Base64Decoder())
    transformations(CircleCropTransformation())
    placeholder(R.drawable.placeholder)
    error(R.drawable.error)
}
```

## Data Model Examples

### User Document with Profile Picture

```kotlin
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageBase64: String = "", // Base64 encoded image
    val photoUrl: String = "", // Legacy field (if using Storage)
    val isOnline: Boolean = false,
    val lastActive: Date = Date()
)
```

### Message Document with Image

```kotlin
data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val base64ImageData: String? = null, // Base64 encoded image
    val imageUrl: String? = null, // Legacy field (if using Storage)
    val timestamp: Date = Date(),
    val status: MessageStatus = MessageStatus.SENT
)
```

## Firestore Security

Since images are stored in Firestore documents, security is handled by Firestore rules:

```javascript
// User can only update their own profile image
match /users/{userId} {
  allow read: if request.auth != null;
  allow update: if request.auth.uid == userId;
}

// Only chat participants can send images
match /chats/{chatId}/messages/{messageId} {
  allow read: if request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.participants;
  allow create: if request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.participants;
}
```

## Performance Considerations

### Best Practices

1. **Always validate size before saving**
   - Use `Base64Validator` to check size limits
   - Reject images that are too large

2. **Compress aggressively**
   - Use lower quality settings (70% or less)
   - Resize to smaller dimensions
   - Users won't notice quality difference on mobile screens

3. **Cache decoded bitmaps**
   - Use Coil's memory cache
   - Avoid decoding the same image multiple times

4. **Lazy load images**
   - Only decode when needed
   - Use pagination for message lists

5. **Monitor document sizes**
   - Log warnings for large documents
   - Consider alternative storage if sizes grow

### When to Use Firebase Storage Instead

Consider using Firebase Storage if:
- Images are frequently larger than 500KB
- You need to store high-resolution images
- You have many images per document
- You need CDN delivery for images
- You want to serve images via direct URLs

## Migration Path

If you later decide to use Firebase Storage:

1. Keep the `profileImageBase64` and `base64ImageData` fields
2. Add new fields: `profileImageUrl` and `imageUrl`
3. Gradually migrate existing Base64 data to Storage
4. Update UI to check both fields (Base64 first, then URL)
5. Eventually remove Base64 fields once migration is complete

## Troubleshooting

### Image Too Large Error

**Problem**: "Image is too large: X KB exceeds limit of Y KB"

**Solutions**:
1. Reduce image dimensions in `ImageCompressor`
2. Lower JPEG quality setting
3. Ask user to choose a smaller image
4. Consider using Firebase Storage for this image

### Firestore Document Size Error

**Problem**: "Document size exceeds 1MB"

**Solutions**:
1. Ensure validation is working correctly
2. Check if other fields are also large
3. Split data into multiple documents
4. Use Firebase Storage instead

### Slow Performance

**Problem**: App is slow when loading images

**Solutions**:
1. Implement pagination for message lists
2. Use Coil's caching more effectively
3. Decode images on background thread
4. Consider reducing image quality further

## Summary

Base64 storage in Firestore is a simple, cost-effective solution for small images. By following the size limits and compression strategies outlined in this guide, you can ensure good performance while keeping implementation simple.

For larger images or high-volume applications, consider migrating to Firebase Storage in the future.
