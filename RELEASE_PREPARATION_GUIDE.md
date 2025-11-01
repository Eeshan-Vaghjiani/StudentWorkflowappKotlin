# Release Preparation Guide

## Overview

This guide provides comprehensive instructions for preparing and releasing the TeamSync Android app to production.

## Prerequisites

- Android Studio installed and configured
- Gradle build system working
- Signing keystore configured (for production releases)
- Google Play Console access (for Play Store deployment)
- Firebase project configured
- All tests passing

## Release Checklist

### Pre-Release Checklist

- [ ] All critical bugs fixed
- [ ] All features tested and working
- [ ] Unit tests passing
- [ ] Integration tests passing
- [ ] UI/UX reviewed and approved
- [ ] Performance metrics acceptable
- [ ] Security audit completed
- [ ] Privacy policy updated
- [ ] Terms of service updated
- [ ] App store listing prepared

### Version Management

#### Version Numbering Scheme

Follow Semantic Versioning (SemVer):
- **Major.Minor.Patch** (e.g., 1.2.3)

**Version Code:**
- Integer that increments with each release
- Used by Android system to determine update eligibility
- Must always increase

**Version Name:**
- Human-readable version string
- Displayed to users in app and store listing

**Increment Guidelines:**
- **Patch (1.0.0 → 1.0.1)**: Bug fixes, minor improvements
- **Minor (1.0.0 → 1.1.0)**: New features, non-breaking changes
- **Major (1.0.0 → 2.0.0)**: Breaking changes, major redesign

#### Current Version

Check `app/build.gradle.kts`:
```kotlin
versionCode = 1
versionName = "1.0.0"
```

## Release Process

### Option 1: Automated Release (Recommended)

Use the provided release scripts:

**Windows:**
```cmd
prepare-release.bat
```

**Unix/Linux/Mac:**
```bash
chmod +x prepare-release.sh
./prepare-release.sh
```

The script will:
1. Display current version
2. Prompt for version increment
3. Update build.gradle.kts
4. Clean previous builds
5. Run tests (optional)
6. Build release APK
7. Build release AAB
8. Display build summary

### Option 2: Manual Release

#### Step 1: Update Version Numbers

Edit `app/build.gradle.kts`:

```kotlin
defaultConfig {
    applicationId = "com.teamsync.collaboration"
    minSdk = 23
    targetSdk = 34
    versionCode = 2  // Increment this
    versionName = "1.0.1"  // Update this
    // ...
}
```

#### Step 2: Configure Signing

**Create or update signing configuration:**

1. **Generate keystore (first time only):**
   ```bash
   keytool -genkey -v -keystore teamsync-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias teamsync
   ```

2. **Store credentials securely:**
   
   Add to `local.properties` (NOT committed to git):
   ```properties
   KEYSTORE_FILE=../teamsync-release.jks
   KEYSTORE_PASSWORD=your_keystore_password
   KEY_ALIAS=teamsync
   KEY_PASSWORD=your_key_password
   ```

3. **Update build.gradle.kts:**
   ```kotlin
   signingConfigs {
       create("release") {
           val properties = Properties()
           val localPropertiesFile = rootProject.file("local.properties")
           if (localPropertiesFile.exists()) {
               localPropertiesFile.inputStream().use { properties.load(it) }
           }
           
           storeFile = file(properties.getProperty("KEYSTORE_FILE") ?: "")
           storePassword = properties.getProperty("KEYSTORE_PASSWORD") ?: ""
           keyAlias = properties.getProperty("KEY_ALIAS") ?: ""
           keyPassword = properties.getProperty("KEY_PASSWORD") ?: ""
       }
   }
   
   buildTypes {
       release {
           signingConfig = signingConfigs.getByName("release")
           // ...
       }
   }
   ```

#### Step 3: Clean Build

```bash
./gradlew clean
```

#### Step 4: Run Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest
```

#### Step 5: Build Release APK

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

#### Step 6: Build Release AAB (for Play Store)

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

## Testing Release Build

### Install APK on Test Devices

```bash
adb install app/build/outputs/apk/release/app-release.apk
```

### Test on Multiple Devices

**Minimum test matrix:**
- [ ] Low-end device (Android 6.0, 1GB RAM)
- [ ] Mid-range device (Android 10, 3GB RAM)
- [ ] High-end device (Android 14, 6GB+ RAM)
- [ ] Tablet (10" screen)

**Test scenarios:**
- [ ] Fresh install
- [ ] Upgrade from previous version
- [ ] Sign in with Google
- [ ] Create tasks
- [ ] Create groups
- [ ] Send messages
- [ ] Upload images
- [ ] Offline functionality
- [ ] Push notifications
- [ ] Dark mode
- [ ] Accessibility features

### Verify Build

```bash
# Check APK signature
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# Check AAB contents
bundletool build-apks --bundle=app/build/outputs/bundle/release/app-release.aab --output=app.apks
bundletool get-size total --apks=app.apks
```

## Deployment to Google Play

### Step 1: Prepare Store Listing

**Required assets:**
- App icon (512x512 PNG)
- Feature graphic (1024x500 PNG)
- Screenshots (at least 2, up to 8)
  - Phone: 16:9 or 9:16 aspect ratio
  - Tablet: 16:9 or 9:16 aspect ratio
- App description (short and full)
- Privacy policy URL
- Category selection

### Step 2: Upload to Internal Testing

1. Go to Google Play Console
2. Select your app
3. Navigate to **Testing → Internal testing**
4. Click **Create new release**
5. Upload `app-release.aab`
6. Add release notes
7. Review and rollout

**Internal testing benefits:**
- Fast review (minutes)
- Test with real users
- Verify Play Store integration
- Check for crashes before production

### Step 3: Monitor Internal Testing

**Check these metrics:**
- [ ] Crash-free rate > 99%
- [ ] ANR rate < 0.5%
- [ ] Install success rate > 95%
- [ ] User feedback positive
- [ ] No critical bugs reported

**Monitoring period:** 24-48 hours minimum

### Step 4: Promote to Production

1. Navigate to **Production → Create new release**
2. Promote from internal testing or upload new AAB
3. Add release notes
4. Set rollout percentage (start with 10-20%)
5. Review and publish

**Staged rollout strategy:**
- Day 1: 10% of users
- Day 2: 25% of users (if no issues)
- Day 3: 50% of users (if no issues)
- Day 4: 100% of users (if no issues)

### Step 5: Monitor Production Release

**Critical metrics (first 24 hours):**
- Crash-free rate
- ANR rate
- User ratings
- User reviews
- Install/uninstall rates

**If issues detected:**
1. Halt rollout immediately
2. Investigate crash reports
3. Fix critical issues
4. Release hotfix
5. Resume rollout

## Post-Release Tasks

### Update Documentation

- [ ] Update CHANGELOG.md
- [ ] Update README.md with new version
- [ ] Document known issues
- [ ] Update API documentation (if applicable)

### Tag Release in Git

```bash
git tag -a v1.0.1 -m "Release version 1.0.1"
git push origin v1.0.1
```

### Notify Team

- [ ] Send release announcement
- [ ] Update project management tools
- [ ] Notify support team of changes
- [ ] Update internal documentation

### Monitor Metrics

**Week 1 monitoring:**
- Daily crash reports review
- User feedback monitoring
- Performance metrics tracking
- Error rate monitoring

**Week 2-4 monitoring:**
- Weekly metrics review
- User retention analysis
- Feature usage analytics
- Performance trends

## Rollback Procedure

### If Critical Issues Found

**Option 1: Halt Rollout**
1. Go to Google Play Console
2. Navigate to Production release
3. Click "Halt rollout"
4. Users on old version stay on old version
5. Users on new version stay on new version

**Option 2: Release Hotfix**
1. Fix critical issue
2. Increment version (e.g., 1.0.1 → 1.0.2)
3. Build and test hotfix
4. Deploy to internal testing
5. Fast-track to production

**Option 3: Rollback (Last Resort)**
1. Not directly supported by Play Store
2. Must release new version with fixes
3. Can't force users back to old version

## Common Issues and Solutions

### Issue 1: Build Fails

**Symptoms:**
- Gradle build errors
- Compilation errors
- Resource conflicts

**Solutions:**
- Clean build: `./gradlew clean`
- Invalidate caches in Android Studio
- Update dependencies
- Check for syntax errors

### Issue 2: Signing Fails

**Symptoms:**
- "Keystore not found" error
- "Invalid keystore format" error
- "Wrong password" error

**Solutions:**
- Verify keystore file path
- Check credentials in local.properties
- Regenerate keystore if corrupted
- Ensure keystore is not in .gitignore

### Issue 3: Upload Rejected

**Symptoms:**
- Play Console rejects AAB
- Version code conflict
- Signature mismatch

**Solutions:**
- Increment version code
- Use same signing key as previous releases
- Check for policy violations
- Review rejection email details

### Issue 4: High Crash Rate

**Symptoms:**
- Crash-free rate < 95%
- Specific crash patterns
- User complaints

**Solutions:**
- Check Firebase Crashlytics
- Identify crash patterns
- Reproduce locally
- Release hotfix ASAP

## Release Notes Template

```markdown
## Version 1.0.1 (Build 2)

### New Features
- Added dark mode support
- Implemented offline message queue
- Enhanced AI task creation

### Improvements
- Improved app performance
- Optimized RecyclerView rendering
- Enhanced error messages

### Bug Fixes
- Fixed Firestore permission errors
- Fixed typing indicator crashes
- Fixed image upload issues

### Known Issues
- None

### Upgrade Notes
- No breaking changes
- Automatic data migration
```

## Security Considerations

### Before Release

- [ ] Remove debug logging
- [ ] Obfuscate code with R8/ProGuard
- [ ] Validate all API keys are from local.properties
- [ ] Check for hardcoded credentials
- [ ] Review permissions in AndroidManifest.xml
- [ ] Test with security scanning tools
- [ ] Verify HTTPS for all network calls
- [ ] Check for SQL injection vulnerabilities
- [ ] Validate input sanitization

### Keystore Security

- [ ] Store keystore in secure location
- [ ] Never commit keystore to version control
- [ ] Use strong passwords (16+ characters)
- [ ] Backup keystore securely (encrypted)
- [ ] Document keystore location for team
- [ ] Restrict keystore access to authorized personnel

## Performance Optimization

### Before Release

- [ ] Enable R8 code shrinking
- [ ] Enable resource shrinking
- [ ] Optimize images (WebP format)
- [ ] Remove unused resources
- [ ] Minimize APK/AAB size
- [ ] Test app startup time
- [ ] Profile memory usage
- [ ] Check for memory leaks

### ProGuard/R8 Configuration

Check `app/proguard-rules.pro`:
```proguard
# Keep Firebase classes
-keep class com.google.firebase.** { *; }

# Keep data models
-keep class com.example.loginandregistration.models.** { *; }

# Keep Gson classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
```

## Support and Resources

### Documentation
- [Android Developer Guide](https://developer.android.com/guide)
- [Google Play Console Help](https://support.google.com/googleplay/android-developer)
- [Firebase Documentation](https://firebase.google.com/docs)

### Tools
- [Android Studio](https://developer.android.com/studio)
- [Firebase Console](https://console.firebase.google.com)
- [Google Play Console](https://play.google.com/console)
- [bundletool](https://github.com/google/bundletool)

### Monitoring
- Firebase Crashlytics
- Firebase Performance Monitoring
- Google Play Console Vitals
- Firebase Analytics

## Emergency Contacts

**For critical production issues:**
1. Check Firebase Status: https://status.firebase.google.com
2. Check Play Store Status: https://status.play.google.com
3. Contact team lead
4. Review incident response plan

## Appendix

### Build Variants

```kotlin
buildTypes {
    debug {
        isDebuggable = true
        applicationIdSuffix = ".debug"
        versionNameSuffix = "-debug"
    }
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        isDebuggable = false
        signingConfig = signingConfigs.getByName("release")
    }
}
```

### Useful Commands

```bash
# Check app size
./gradlew app:printApkSize

# Generate dependency report
./gradlew app:dependencies

# Run lint checks
./gradlew lint

# Generate test coverage report
./gradlew jacocoTestReport

# Build all variants
./gradlew build
```

### Version History

| Version | Code | Date | Notes |
|---------|------|------|-------|
| 1.0.0 | 1 | 2024-10-29 | Initial release |
| 1.0.1 | 2 | TBD | Bug fixes and improvements |
