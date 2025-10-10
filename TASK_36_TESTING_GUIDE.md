# Task 36: Dark Mode Support - Testing Guide

## Quick Test Steps

### 1. Enable Dark Mode
**On Android Device:**
1. Open Settings
2. Go to Display
3. Enable "Dark theme" or "Dark mode"
4. Return to the app

**Expected Result:**
- App immediately switches to dark theme
- All screens use dark colors
- Text remains readable

### 2. Test All Screens

#### Login/Register Screens
- [ ] Background is dark (#1C1C1E)
- [ ] Text is white and readable
- [ ] Input fields are visible
- [ ] Buttons are clearly visible
- [ ] Logo/branding looks good

#### Dashboard/Home Screen
- [ ] Background is dark
- [ ] Cards have proper elevation (#2C2C2E)
- [ ] Stats are visible and colorful
- [ ] Icons are visible
- [ ] Text is readable

#### Tasks Screen
- [ ] Task list displays correctly
- [ ] Task cards are visible
- [ ] Status colors are distinguishable:
  - Overdue: Bright red (#FF453A)
  - Due Today: Bright orange (#FF9F0A)
  - Completed: Bright green (#32D74B)
- [ ] Priority indicators are visible
- [ ] Empty state looks good
- [ ] Skeleton loaders work

#### Task Details Screen
- [ ] Background is dark
- [ ] Task title is readable
- [ ] Description text is clear
- [ ] Priority indicator is visible
- [ ] Status chip is readable
- [ ] Buttons are visible
- [ ] Dividers are subtle but visible

#### Groups Screen
- [ ] Group cards are visible
- [ ] Group names are readable
- [ ] Member counts are visible
- [ ] Icons have proper colors
- [ ] Empty state looks good
- [ ] Swipe refresh works

#### Group Details Screen
- [ ] Group info is readable
- [ ] Join code box is visible
- [ ] Member list is clear
- [ ] Admin badge is visible
- [ ] Action buttons work

#### Chat List Screen
- [ ] Chat items are visible
- [ ] Last messages are readable
- [ ] Timestamps are visible
- [ ] Unread counts stand out
- [ ] Profile pictures display
- [ ] Empty state looks good

#### Chat Room Screen
- [ ] Messages are readable
- [ ] Sent messages: Blue background (#0A84FF)
- [ ] Received messages: Dark gray (#3A3A3C)
- [ ] Text is white on both
- [ ] Timestamps are visible
- [ ] Input field is usable
- [ ] Attachment button visible
- [ ] Send button visible

#### Calendar Screen
- [ ] Calendar grid is visible
- [ ] Date numbers are readable
- [ ] Task dots are visible
- [ ] Selected date is highlighted
- [ ] Today is highlighted
- [ ] Task list below is readable

#### Profile Screen
- [ ] Profile picture displays
- [ ] User info is readable
- [ ] Settings options are visible
- [ ] Logout button is clear

### 3. Test UI Elements

#### Text Readability
- [ ] Primary text is white (#FFFFFF)
- [ ] Secondary text is gray (#8E8E93)
- [ ] All text has good contrast
- [ ] No text is invisible
- [ ] Links are visible and blue

#### Colors and Contrast
- [ ] Status colors are distinguishable
- [ ] Priority colors are clear
- [ ] Error messages are visible
- [ ] Success messages are clear
- [ ] Warning colors stand out

#### Icons and Images
- [ ] All icons are visible
- [ ] Profile pictures display correctly
- [ ] Default avatars look good
- [ ] Icon tints are appropriate
- [ ] No invisible icons

#### Cards and Surfaces
- [ ] Cards have proper elevation
- [ ] Background hierarchy is clear:
  - Base: #1C1C1E
  - Cards: #2C2C2E
  - Elevated: #3A3A3C
- [ ] Shadows are subtle
- [ ] Borders are visible

#### Interactive Elements
- [ ] Buttons are clearly visible
- [ ] Button text is readable
- [ ] Ripple effects work
- [ ] Touch targets are clear
- [ ] Disabled states are visible

#### Loading States
- [ ] Skeleton loaders are visible
- [ ] Shimmer effect works
- [ ] Progress indicators are clear
- [ ] Loading text is readable

#### Empty States
- [ ] Empty state icons are visible
- [ ] Empty state text is readable
- [ ] Action buttons are clear
- [ ] Overall appearance is good

### 4. Test Transitions

#### Theme Switching
1. Start app in light mode
2. Switch to dark mode in system settings
3. Return to app

**Expected:**
- [ ] App switches to dark theme
- [ ] No visual glitches
- [ ] All elements update correctly
- [ ] No crashes

#### Screen Navigation
- [ ] Navigate between all screens
- [ ] Check each screen in dark mode
- [ ] Verify animations work
- [ ] Check transitions are smooth

### 5. Test Edge Cases

#### Long Text
- [ ] Long task titles wrap correctly
- [ ] Long descriptions are readable
- [ ] Long messages display properly
- [ ] Text doesn't overflow

#### Many Items
- [ ] Long lists scroll smoothly
- [ ] Performance is good
- [ ] All items are visible
- [ ] No rendering issues

#### Different States
- [ ] Completed tasks look good
- [ ] Overdue tasks are clear
- [ ] Pending tasks are visible
- [ ] Empty lists look good

### 6. Accessibility Check

#### Contrast Ratios
- [ ] Text meets WCAG AA standards
- [ ] Important elements are distinguishable
- [ ] Color isn't the only indicator
- [ ] Icons have sufficient contrast

#### Readability
- [ ] Text is easy to read
- [ ] Font sizes are appropriate
- [ ] Line spacing is good
- [ ] No eye strain

### 7. Device Testing

#### Test on Multiple Devices
- [ ] Phone (small screen)
- [ ] Phone (large screen)
- [ ] Tablet (if available)
- [ ] Different Android versions

#### Test Different Scenarios
- [ ] Fresh install
- [ ] Existing user
- [ ] With data
- [ ] Without data

## Common Issues to Check

### Text Visibility
❌ **Problem:** Text is hard to read
✅ **Solution:** Check text color is #FFFFFF for primary, #8E8E93 for secondary

### Icon Visibility
❌ **Problem:** Icons are invisible
✅ **Solution:** Verify icon tints use theme colors

### Card Visibility
❌ **Problem:** Cards blend with background
✅ **Solution:** Check card background is #2C2C2E, not #1C1C1E

### Status Colors
❌ **Problem:** Status colors are too dark
✅ **Solution:** Verify using brightened colors (#FF453A, #FF9F0A, #32D74B)

### Message Bubbles
❌ **Problem:** Message text is hard to read
✅ **Solution:** Check sent messages use #0A84FF, received use #3A3A3C

## Performance Check

### Battery Impact
- [ ] Monitor battery usage
- [ ] Compare with light mode
- [ ] Check for excessive redraws

### Memory Usage
- [ ] Monitor memory consumption
- [ ] Check for leaks
- [ ] Verify cache is working

### Rendering Performance
- [ ] Smooth scrolling
- [ ] No lag when switching themes
- [ ] Animations are smooth

## Comparison Test

### Side-by-Side Comparison
1. Take screenshots in light mode
2. Switch to dark mode
3. Take screenshots in dark mode
4. Compare:
   - [ ] All elements are visible
   - [ ] Colors are appropriate
   - [ ] Layout is consistent
   - [ ] Nothing is missing

## User Experience Test

### Subjective Evaluation
- [ ] Dark mode looks polished
- [ ] Colors are pleasant
- [ ] Not too bright or too dark
- [ ] Comfortable to use
- [ ] Professional appearance

### Usability
- [ ] Easy to navigate
- [ ] Elements are findable
- [ ] Actions are clear
- [ ] No confusion

## Automated Checks

### Color Contrast Tool
Use Android Studio's Layout Inspector:
1. Open Layout Inspector
2. Select any screen
3. Check color contrast ratios
4. Verify WCAG compliance

### Accessibility Scanner
Use Google's Accessibility Scanner:
1. Install Accessibility Scanner
2. Scan each screen
3. Fix any issues found
4. Re-scan to verify

## Sign-Off Checklist

### Before Marking Complete
- [ ] All screens tested in dark mode
- [ ] Text is readable everywhere
- [ ] Icons are visible everywhere
- [ ] Colors are appropriate
- [ ] No visual glitches
- [ ] Performance is good
- [ ] Accessibility is maintained
- [ ] User experience is positive

### Documentation
- [ ] Screenshots taken
- [ ] Issues documented
- [ ] Fixes verified
- [ ] Testing notes complete

## Test Results Template

```
Date: ___________
Tester: ___________
Device: ___________
Android Version: ___________

Screens Tested:
- [ ] Login/Register
- [ ] Dashboard
- [ ] Tasks
- [ ] Task Details
- [ ] Groups
- [ ] Group Details
- [ ] Chat List
- [ ] Chat Room
- [ ] Calendar
- [ ] Profile

Issues Found:
1. ___________
2. ___________
3. ___________

Overall Assessment:
- Visual Quality: ___/10
- Readability: ___/10
- Usability: ___/10
- Performance: ___/10

Recommendation:
[ ] Approve
[ ] Needs fixes
[ ] Major issues

Notes:
___________
```

## Quick Reference

### Expected Colors
- Background: #1C1C1E
- Cards: #2C2C2E
- Text Primary: #FFFFFF
- Text Secondary: #8E8E93
- Primary Blue: #0A84FF
- Error Red: #FF453A
- Warning Orange: #FF9F0A
- Success Green: #32D74B

### Key Files
- `values-night/colors.xml`
- `values-night/themes.xml`
- `ThemeUtils.kt`

### Testing Tools
- Android Studio Layout Inspector
- Accessibility Scanner
- Device Settings > Display
- Screenshot comparison

## Support

If you find issues:
1. Document the issue
2. Take screenshots
3. Note device and Android version
4. Check if it's a theme issue or general bug
5. Report with reproduction steps
