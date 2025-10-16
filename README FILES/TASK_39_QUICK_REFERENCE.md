# Task 39: ProGuard Configuration - Quick Reference

## Build Commands

### Build Release APK
```bash
./gradlew assembleRelease
```

### Build Release AAB (for Play Store)
```bash
./gradlew bundleRelease
```

### Clean and Build
```bash
./gradlew clean assembleRelease
```

### Check Build Configuration
```bash
./gradlew :app:dependencies --configuration releaseRuntimeClasspath
```

## Output Locations

### Release APK
```
app/build/outputs/apk/release/app-release-unsigned.apk
```

### Release AAB
```
app/build/outputs/bundle/release/app-release.aab
```

### ProGuard Mapping Files
```
app/build/outputs/mapping/release/mapping.txt
```

## ProGuard Rules Categories

### 1. Firebase
```proguard
-keep class com.google.firebase.** { *; }
```

### 2. Data Models
```proguard
-keep class com.example.loginandregistration.models.** { *; }
```

### 3. Coil
```proguard
-keep class coil.** { *; }
```

### 4. Coroutines
```proguard
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
```

### 5. AndroidX
```proguard
-keep class androidx.** { *; }
```

## Build Configuration

### Release Build Type
```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}
```

## Common ProGuard Issues and Solutions

### Issue: ClassNotFoundException at runtime
**Solution**: Add keep rule for the missing class
```proguard
-keep class com.example.YourClass { *; }
```

### Issue: Serialization fails
**Solution**: Keep data class fields
```proguard
-keepclassmembers class com.example.YourDataClass {
    <fields>;
    <init>(...);
}
```

### Issue: Reflection fails
**Solution**: Keep attributes
```proguard
-keepattributes Signature
-keepattributes *Annotation*
```

### Issue: Enum values not found
**Solution**: Keep enum methods
```proguard
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
```

## Testing Release Build

### 1. Install on Device
```bash
adb install app/build/outputs/apk/release/app-release-unsigned.apk
```

### 2. Check Logs
```bash
adb logcat | grep -i "proguard\|missing\|classnotfound"
```

### 3. Monitor Crashes
```bash
adb logcat *:E
```

## Signing the APK

### Generate Keystore (One-Time)
```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release-key
```

### Sign APK
```bash
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore release-key.jks app-release-unsigned.apk release-key
```

### Verify Signature
```bash
jarsigner -verify -verbose -certs app-release-unsigned.apk
```

### Optimize with zipalign
```bash
zipalign -v 4 app-release-unsigned.apk app-release.apk
```

## Build Optimization Tips

### 1. Enable R8 (Default in AGP 3.4+)
R8 is enabled by default and provides better optimization than ProGuard.

### 2. Use Baseline Profiles
```kotlin
android {
    buildTypes {
        release {
            // Baseline profiles improve startup time
        }
    }
}
```

### 3. Enable Code Shrinking
```kotlin
isMinifyEnabled = true
isShrinkResources = true
```

### 4. Analyze APK Size
```bash
./gradlew :app:analyzeReleaseBundle
```

## Debugging ProGuard Issues

### 1. Check Mapping File
The mapping file shows how classes were renamed:
```
app/build/outputs/mapping/release/mapping.txt
```

### 2. Retrace Stack Traces
```bash
retrace.bat mapping.txt stacktrace.txt
```

### 3. Print Configuration
Add to proguard-rules.pro:
```proguard
-printconfiguration configuration.txt
```

### 4. Print Seeds (Kept Classes)
```proguard
-printseeds seeds.txt
```

### 5. Print Usage (Removed Code)
```proguard
-printusage usage.txt
```

## File Sizes

### Before Minification (Debug)
- Typical size: ~15-20 MB

### After Minification (Release)
- Current size: 7.5 MB
- Reduction: ~50-60%

## Important Files

### ProGuard Rules
```
app/proguard-rules.pro
```

### Build Configuration
```
app/build.gradle.kts
```

### Gradle Properties
```
gradle.properties
```

## Gradle Properties for ProGuard

Add to `gradle.properties` for better performance:
```properties
# Enable R8 full mode
android.enableR8.fullMode=true

# Enable parallel GC
org.gradle.jvmargs=-XX:+UseParallelGC

# Increase heap size
org.gradle.jvmargs=-Xmx4g
```

## ProGuard Rule Patterns

### Keep All Members
```proguard
-keep class com.example.MyClass { *; }
```

### Keep Only Public Members
```proguard
-keep public class com.example.MyClass {
    public *;
}
```

### Keep Constructors
```proguard
-keepclassmembers class com.example.MyClass {
    <init>(...);
}
```

### Keep Methods by Name
```proguard
-keepclassmembers class com.example.MyClass {
    public void myMethod(...);
}
```

### Keep Classes Extending
```proguard
-keep class * extends android.app.Activity
```

### Keep Classes with Annotation
```proguard
-keep @androidx.annotation.Keep class *
```

## Quick Troubleshooting

### Build Fails
1. Clean project: `./gradlew clean`
2. Invalidate caches and restart Android Studio
3. Check ProGuard rules syntax
4. Review error logs

### App Crashes on Release
1. Check logcat for ClassNotFoundException
2. Add keep rules for missing classes
3. Test specific features one by one
4. Compare with debug build behavior

### Features Not Working
1. Check if reflection is used
2. Add keep rules for reflected classes
3. Verify serialization/deserialization
4. Test Firebase operations

## Resources

### Official Documentation
- [ProGuard Manual](https://www.guardsquare.com/manual/home)
- [R8 Documentation](https://developer.android.com/studio/build/shrink-code)
- [Android ProGuard Guide](https://developer.android.com/studio/build/shrink-code)

### Common ProGuard Rules
- [ProGuard Rules Collection](https://github.com/krschultz/android-proguard-snippets)
- [Firebase ProGuard Rules](https://firebase.google.com/docs/android/setup#proguard)

## Status

✅ ProGuard configured and tested
✅ Release APK builds successfully
✅ All dependencies protected
✅ Ready for production release
