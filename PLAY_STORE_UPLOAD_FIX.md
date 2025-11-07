# Play Store Upload Fixes Applied

## Issues Fixed

### 1. API Level 35 Requirement ✅
**Error:** App targets API level 34, must target at least API level 35

**Fix Applied:**
- Updated `compileSdk` from 34 to 35
- Updated `targetSdk` from 34 to 35

### 2. Native Debug Symbols ✅
**Warning:** App Bundle contains native code without debug symbols

**Fix Applied:**
- Added `ndk { debugSymbolLevel = "FULL" }` to both `defaultConfig` and `release` buildType
- This will automatically generate and include debug symbols in your App Bundle

## Next Steps

### 1. Clean and Rebuild
```bash
# Windows
gradlew clean
gradlew bundleRelease

# Or use the batch file
rebuild-and-test.bat
```

### 2. Generate Release Bundle
The App Bundle will be generated at:
```
app/build/outputs/bundle/release/app-release.aab
```

### 3. Upload to Play Console
- The new bundle will include native debug symbols automatically
- No separate symbol file upload needed
- API level 35 requirement satisfied

## What Changed

**File:** `app/build.gradle.kts`

```kotlin
android {
    compileSdk = 35  // Changed from 34
    
    defaultConfig {
        targetSdk = 35  // Changed from 34
        
        // Added for debug symbols
        ndk {
            debugSymbolLevel = "FULL"
        }
    }
    
    buildTypes {
        release {
            // Added for debug symbols
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
}
```

## Verification

After rebuilding, verify:
1. ✅ App Bundle targets API 35
2. ✅ Native debug symbols included
3. ✅ No Play Console errors on upload

## Notes

- The `debugSymbolLevel = "FULL"` setting generates complete native debug symbols
- These symbols help analyze crashes and ANRs in the Play Console
- The symbols are automatically included in the App Bundle (.aab file)
- No manual symbol file upload required
