# Task 34: Animations and Transitions - Quick Reference

## 🚀 Quick Start

### Using AnimationUtils

```kotlin
import com.example.loginandregistration.utils.AnimationUtils

// Fade in a view
AnimationUtils.fadeIn(myView)

// Fade out a view
AnimationUtils.fadeOut(myView)

// Button press animation
button.setOnClickListener {
    AnimationUtils.buttonPress(it)
    // Your action here
}

// Slide up (for bottom sheets)
AnimationUtils.slideUp(bottomSheetView)

// Smooth scroll to bottom
AnimationUtils.smoothScrollToBottom(recyclerView)
```

### Using View Extensions

```kotlin
import com.example.loginandregistration.utils.fadeIn
import com.example.loginandregistration.utils.animatePress

// Fade in a view
myView.fadeIn()

// Button press animation
button.setOnClickListener {
    it.animatePress()
    // Your action here
}

// Shake for error
errorView.shake()
```

## 📚 Animation Methods

### Fade Animations
```kotlin
AnimationUtils.fadeIn(view, duration = 300, onEnd = { })
AnimationUtils.fadeOut(view, duration = 300, onEnd = { })
AnimationUtils.crossFade(viewOut, viewIn, duration = 300)
```

### Slide Animations
```kotlin
AnimationUtils.slideUp(view, duration = 300, onEnd = { })
AnimationUtils.slideDown(view, duration = 300, onEnd = { })
AnimationUtils.slideInFromLeft(view, duration = 300, onEnd = { })
AnimationUtils.slideInFromRight(view, duration = 300, onEnd = { })
```

### Scale Animations
```kotlin
AnimationUtils.scaleIn(view, duration = 200, onEnd = { })
AnimationUtils.scaleOut(view, duration = 200, onEnd = { })
AnimationUtils.buttonPress(view)
AnimationUtils.pulse(view)
```

### Special Effects
```kotlin
AnimationUtils.shake(view)              // Error feedback
AnimationUtils.bounce(view)             // Playful effect
AnimationUtils.rotate(view, degrees = 360f, duration = 300)
AnimationUtils.circularReveal(view, centerX, centerY, duration = 400)
```

### RecyclerView Helpers
```kotlin
AnimationUtils.smoothScrollToPosition(recyclerView, position, smooth = true)
AnimationUtils.smoothScrollToBottom(recyclerView)
```

### Dimension Animations
```kotlin
AnimationUtils.animateHeight(view, targetHeight, duration = 300)
AnimationUtils.animateWidth(view, targetWidth, duration = 300)
AnimationUtils.animateElevation(view, targetElevation, duration = 300)
```

### Ripple Effects
```kotlin
AnimationUtils.addRippleEffect(view)           // Standard ripple
AnimationUtils.addCircularRippleEffect(view)   // Borderless ripple
```

## 🎯 Common Use Cases

### 1. New Message Appears
```kotlin
// In MessageAdapter.onBindViewHolder()
AnimationUtils.fadeIn(holder.itemView, duration = 200)
```

### 2. Button Click Feedback
```kotlin
button.setOnClickListener {
    AnimationUtils.buttonPress(it)
    performAction()
}
```

### 3. Bottom Sheet Appears
```kotlin
// In BottomSheetDialogFragment.onViewCreated()
AnimationUtils.slideUp(binding.root, duration = 250)
```

### 4. Scroll to New Message
```kotlin
// When new message arrives
AnimationUtils.smoothScrollToBottom(messagesRecyclerView)
```

### 5. Image Loads
```kotlin
imageView.load(url) {
    listener(
        onSuccess = { _, _ ->
            AnimationUtils.fadeIn(imageView, duration = 300)
        }
    )
}
```

### 6. Error Feedback
```kotlin
if (error) {
    errorView.shake()
    errorView.visibility = View.VISIBLE
}
```

### 7. Show/Hide Views
```kotlin
// Show
AnimationUtils.fadeIn(view) {
    // Animation complete
}

// Hide
AnimationUtils.fadeOut(view) {
    // Animation complete, view is now GONE
}
```

### 8. Attention Grabber
```kotlin
// Draw attention to a view
AnimationUtils.pulse(importantView)
```

## ⏱️ Default Durations

| Animation Type | Default Duration | Recommended Range |
|---------------|------------------|-------------------|
| Button Press  | 200ms            | 100-200ms         |
| Fade In/Out   | 300ms            | 200-400ms         |
| Slide Up/Down | 300ms            | 250-400ms         |
| Scale In/Out  | 200ms            | 150-250ms         |
| Shake         | 500ms            | 400-600ms         |
| Pulse         | 400ms            | 300-500ms         |
| Bounce        | 500ms            | 400-600ms         |
| Rotate        | 300ms            | 200-400ms         |

## 🎨 Interpolators Used

| Interpolator              | Use Case                    |
|--------------------------|------------------------------|
| DecelerateInterpolator   | Content entering (fade in)   |
| AccelerateInterpolator   | Content exiting (fade out)   |
| AccelerateDecelerate     | Symmetric animations         |
| OvershootInterpolator    | Bouncy, playful effects      |
| LinearInterpolator       | Constant speed (shake)       |

## 📱 Where Animations Are Used

### Chat Room Activity
- ✅ New message fade-in
- ✅ Send button press animation
- ✅ Attachment button press animation
- ✅ Smooth scroll to bottom

### Attachment Bottom Sheet
- ✅ Slide-up animation on open
- ✅ Camera button press animation
- ✅ Gallery button press animation
- ✅ Document button press animation

### Image Viewer Activity
- ✅ Image fade-in when loaded
- ✅ Close button press animation
- ✅ Download button press animation

### Message Adapter
- ✅ Message item fade-in

## 🔧 Customization Examples

### Custom Duration
```kotlin
AnimationUtils.fadeIn(view, duration = 500)
```

### With Callback
```kotlin
AnimationUtils.fadeOut(view) {
    // Do something after animation
    loadNewContent()
}
```

### Chain Animations
```kotlin
AnimationUtils.fadeOut(oldView) {
    AnimationUtils.fadeIn(newView)
}
```

### Conditional Animation
```kotlin
if (shouldAnimate) {
    AnimationUtils.slideUp(view)
} else {
    view.visibility = View.VISIBLE
}
```

## ⚡ Performance Tips

1. **Use ViewPropertyAnimator** (already done in AnimationUtils)
   - Hardware accelerated
   - Optimized for performance

2. **Animate Transform Properties**
   - ✅ alpha, translationX, translationY, scaleX, scaleY, rotation
   - ❌ Avoid animating width, height, margins (causes layout)

3. **Keep Durations Short**
   - 200-400ms feels responsive
   - >500ms feels sluggish

4. **Use Appropriate Interpolators**
   - Decelerate for entering
   - Accelerate for exiting
   - AccelerateDecelerate for symmetric

5. **Avoid Overdoing It**
   - Not every action needs animation
   - Use animations purposefully

## 🐛 Troubleshooting

### Animation Not Visible
```kotlin
// Make sure view is visible
view.visibility = View.VISIBLE
AnimationUtils.fadeIn(view)
```

### Animation Feels Slow
```kotlin
// Reduce duration
AnimationUtils.fadeIn(view, duration = 150)
```

### Animation Feels Abrupt
```kotlin
// Increase duration or change interpolator
AnimationUtils.fadeIn(view, duration = 400)
```

### Multiple Animations Conflict
```kotlin
// Cancel existing animations first
view.animate().cancel()
AnimationUtils.fadeIn(view)
```

### View Disappears After Animation
```kotlin
// Use callback to set final state
AnimationUtils.fadeOut(view) {
    view.visibility = View.GONE
}
```

## 📖 Additional Resources

### Files to Reference
- `AnimationUtils.kt` - Main animation utility class
- `ViewExtensions.kt` - Kotlin extension functions
- `MessageAdapter.kt` - Example of fade-in usage
- `AttachmentBottomSheet.kt` - Example of slide-up and button press
- `ChatRoomActivity.kt` - Example of smooth scrolling
- `ImageViewerActivity.kt` - Example of image fade-in

### Documentation
- See `TASK_34_IMPLEMENTATION_SUMMARY.md` for complete details
- See `TASK_34_TESTING_GUIDE.md` for testing procedures
- See `TASK_34_VISUAL_GUIDE.md` for visual examples

## 🎉 Quick Tips

1. **Start Simple** - Use default durations first
2. **Be Consistent** - Use similar animations for similar actions
3. **Test on Device** - Emulator may not show true performance
4. **Respect Accessibility** - Animations should enhance, not hinder
5. **Profile Performance** - Use GPU rendering profiler to check frame rate

## 📞 Need Help?

If you encounter issues:
1. Check the implementation summary for detailed explanations
2. Review the testing guide for common scenarios
3. Look at existing usage in the codebase
4. Verify view visibility before animating
5. Check animation duration and interpolator

---

**Remember:** Animations should enhance the user experience, not distract from it. Use them purposefully and consistently throughout the app! ✨
