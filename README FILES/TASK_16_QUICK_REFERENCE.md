# Task 16: Gemini API Configuration - Quick Reference

## 🚀 Quick Setup (3 Steps)

### 1. Get API Key
```
https://makersuite.google.com/app/apikey
```

### 2. Add to local.properties
```properties
GEMINI_API_KEY=your_actual_api_key_here
```

### 3. Sync & Test
```bash
./gradlew test --tests GeminiAPIConnectivityTest
```

## 📁 Key Files

| File | Purpose |
|------|---------|
| `local.properties` | Store API key (not in git) |
| `app/build.gradle.kts` | Read API key to BuildConfig |
| `GeminiAssistantService.kt` | Use API key for requests |
| `GeminiAPIConnectivityTest.kt` | Test API connectivity |

## 💻 Code Usage

### Access API Key
```kotlin
val apiKey = BuildConfig.GEMINI_API_KEY
```

### Initialize Service
```kotlin
val geminiService = GeminiAssistantService(BuildConfig.GEMINI_API_KEY)
```

### Make API Call
```kotlin
val result = geminiService.sendMessage("Hello", emptyList())
result.onSuccess { response ->
    println(response.content)
}
```

## 🧪 Testing Commands

```bash
# All tests
./gradlew test

# API tests only
./gradlew test --tests GeminiAPIConnectivityTest

# Specific test
./gradlew test --tests GeminiAPIConnectivityTest."test API connectivity with simple request"
```

## 🔧 Dependencies

```kotlin
// Already added in build.gradle.kts
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
implementation("com.google.code.gson:gson:2.10.1")
```

## 🐛 Common Issues

| Issue | Solution |
|-------|----------|
| API key not found | Add to `local.properties` and sync Gradle |
| 401 Unauthorized | Check API key is correct and enabled |
| 429 Rate limit | Wait and retry, implement throttling |
| BuildConfig error | Clean build: `./gradlew clean build` |

## 🔒 Security Checklist

- [x] API key in `local.properties` (not committed)
- [x] `local.properties` in `.gitignore`
- [x] No hardcoded API keys in code
- [x] BuildConfig used for access
- [x] Error handling implemented

## 📊 API Information

- **Endpoint**: `https://generativelanguage.googleapis.com/v1beta`
- **Model**: `gemini-pro`
- **Free Tier**: Yes (with rate limits)
- **Documentation**: https://ai.google.dev/docs

## ✅ Verification

```bash
# 1. Check API key configured
grep GEMINI_API_KEY local.properties

# 2. Run tests
./gradlew test --tests GeminiAPIConnectivityTest

# 3. Check BuildConfig
# In Android Studio: Build > Make Project
# Verify no errors
```

## 🎯 Task Status

**All sub-tasks completed:**
- ✅ API key in local.properties
- ✅ BuildConfig reads API key
- ✅ OkHttp dependency added
- ✅ Gson dependency added
- ✅ API connectivity tested

**Ready for**: Task 17 (Update Message model for attachments)
