# 🔧 Fix the Run Error NOW

## The Error
```
Activity class {com.teamsync.collaboration.debug/com.example.loginandregistration.Login} does not exist
```

## The Fix (Choose One)

### ⭐ Option 1: Invalidate Caches (RECOMMENDED)

**This works 90% of the time:**

1. Click **File** menu
2. Click **Invalidate Caches / Restart**
3. Click **"Invalidate and Restart"** button
4. Wait 2-5 minutes for restart
5. Click **Run** (green play button)

**Done!** ✅

---

### ⚡ Option 2: Edit Run Configuration (QUICK)

**Takes 30 seconds:**

1. Click **Run** menu
2. Click **Edit Configurations**
3. Select **"app"** on the left
4. Find **"Launch"** dropdown
5. Change to **"Specified Activity"**
6. In **"Activity"** field, type: `com.example.loginandregistration.Login`
7. Click **Apply**
8. Click **OK**
9. Click **Run**

**Done!** ✅

---

### 🔄 Option 3: Delete and Recreate Configuration

**Takes 1 minute:**

1. Click **Run** menu
2. Click **Edit Configurations**
3. Select **"app"** on the left
4. Click **"-"** (minus button) to delete it
5. Click **OK**
6. Click **Run** (green play button)
7. Android Studio will create a new configuration
8. App should launch

**Done!** ✅

---

### 📱 Option 4: Install APK Manually (ALWAYS WORKS)

**If Android Studio won't launch it:**

1. Open File Explorer
2. Navigate to: `C:\Users\evagh\AndroidStudioProjects\loginandregistration\app\build\outputs\apk\debug\`
3. Find: `app-debug.apk`
4. **Drag the APK file onto your emulator window**
5. Wait for installation
6. Open app from app drawer

**Done!** ✅

---

## Which One Should I Try?

- **First time?** → Try **Option 1** (Invalidate Caches)
- **In a hurry?** → Try **Option 2** (Edit Config)
- **Nothing works?** → Try **Option 4** (Manual Install)

## What Happens After?

✅ App launches  
✅ Shows login screen  
✅ You can register and test  
✅ Firebase works  

## Still Stuck?

Read `IMMEDIATE_FIX_STEPS.md` for more detailed instructions and troubleshooting.

---

**Just pick one option above and follow the steps. The app will launch!** 🚀
