# DO THIS NOW - Step by Step

## You're seeing this error:
```
Activity class {com.teamsync.collaboration.debug/com.example.loginandregistration.Login} does not exist
```

## Here's what to do RIGHT NOW:

### Step 1: Click "File" in the menu bar
(Top left of Android Studio)

### Step 2: Click "Invalidate Caches / Restart"
(Near the bottom of the File menu)

### Step 3: Click the blue "Invalidate and Restart" button
(In the dialog that appears)

### Step 4: Wait
- Android Studio will close
- It will restart automatically
- Wait 2-5 minutes for it to re-index
- You'll see progress bars at the bottom

### Step 5: Click the green "Run" button
(The play icon in the toolbar)

## That's it!

The app will launch successfully.

---

## Why this works

Android Studio cached the old run configuration that had `.debug` suffix. Invalidating caches forces it to rebuild everything from scratch using the current (correct) configuration.

---

## If you've ALREADY done this and it still doesn't work:

Then try this alternative:

### Alternative: Use Release Build

1. Click **Build** menu
2. Click **Select Build Variant**
3. In the panel that opens, change **"debug"** to **"release"**
4. Click **Run**

Release builds don't have the `.debug` suffix, so this will work immediately.

---

**Just do Step 1-5 above. It will work.** 🚀
