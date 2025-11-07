# Play Store Compliance Pages

## Overview

This directory contains two HTML pages required for Google Play Store compliance:

1. **Privacy Policy Page** (`privacy-policy.html`) - Discloses data collection, usage, and protection practices
2. **Account Deletion Page** (`delete-account.html`) - Allows users to delete their account and associated data

These pages are hosted on GitHub Pages and fulfill Google Play Store requirements for:
- Data transparency and privacy disclosure (Privacy Policy)
- User data deletion rights under GDPR and COPPA (Account Deletion)

## Live URLs

Use these URLs in your Play Store Console:

- **Privacy Policy**: https://eeshan-vaghjiani.github.io/StudentWorkflowappKotlin/privacy-policy.html
- **Account Deletion**: https://eeshan-vaghjiani.github.io/StudentWorkflowappKotlin/delete-account.html

## Play Store Requirements Fulfilled

### Privacy Policy Page Requirements
- ✅ **App Name Disclosure**: Displays "Team Collaboration App" prominently
- ✅ **Data Collection**: Lists all data types collected (email, profile pictures, messages, tasks, groups, FCM tokens)
- ✅ **Data Usage**: Explains how each data type is used
- ✅ **Third-Party Services**: Discloses Firebase usage for data storage
- ✅ **Contact Information**: Provides email for privacy inquiries
- ✅ **Last Updated Date**: Shows policy version date
- ✅ **GDPR/COPPA Compliance**: Includes user rights and children's privacy sections
- ✅ **Mobile Responsive**: Works on all device sizes (320px - 1920px)
- ✅ **Public HTTPS URL**: Hosted on GitHub Pages with SSL

### Account Deletion Page Requirements
- ✅ **App Name Disclosure**: Displays "Team Collaboration App" prominently
- ✅ **Authentication Required**: Users must sign in with email/password
- ✅ **Data Deletion List**: Shows what data will be deleted
- ✅ **Retention Policy**: Specifies what data is retained (anonymized)
- ✅ **Confirmation Required**: Checkbox for "I understand this cannot be undone"
- ✅ **Irreversibility Warning**: Prominent warning about permanent deletion
- ✅ **Success/Error Feedback**: Clear messages for all outcomes
- ✅ **Mobile Responsive**: Works on all device sizes
- ✅ **Public HTTPS URL**: Hosted on GitHub Pages with SSL
- ✅ **Firebase Integration**: Deletes data from Firestore and Authentication

## Directory Structure

```
docs/
├── README.md                           # This file
├── GITHUB_PAGES_SETUP.md              # Deployment instructions
├── RESPONSIVE_TESTING_GUIDE.md        # Comprehensive responsive testing guide
├── RESPONSIVE_TESTING_QUICK_START.md  # Quick start guide for testing
├── RESPONSIVE_TEST_RESULTS.md         # Test results tracking document
├── index.html                          # Landing page (redirects to privacy policy)
├── privacy-policy.html                 # Privacy policy page
├── delete-account.html                 # Account deletion page
├── test-responsive.html                # Interactive responsive testing tool
├── test-error-handling.html            # Error handling test page
├── test-security.html                  # Security testing suite
├── test-account-deletion.html          # Account deletion testing page
├── validate-accessibility.html         # Accessibility validation tool
├── SECURITY_TEST_GUIDE.md             # Comprehensive security testing guide
├── SECURITY_TEST_RESULTS.md           # Security test results tracking
└── assets/
    ├── css/
    │   ├── common.css          # Shared styles
    │   ├── privacy.css         # Privacy policy specific styles
    │   └── delete.css          # Account deletion specific styles
    ├── js/
    │   ├── firebase-config.js  # Firebase initialization
    │   └── delete-account.js   # Account deletion logic
    └── images/
        ├── logo.png            # App logo
        └── favicon.ico         # Favicon
```

## Updating the Privacy Policy

### When to Update

Update the privacy policy whenever you:
- Add new data collection (e.g., location data, contacts)
- Change how existing data is used
- Add new third-party services
- Change data retention policies
- Add new features that affect privacy

### How to Update

1. **Edit the HTML file**:
   ```bash
   # Open the file
   docs/privacy-policy.html
   ```

2. **Update relevant sections**:
   - **Data Collection** (section id="data-collection"): Add new data types
   - **Data Usage** (section id="data-usage"): Explain new uses
   - **Data Storage** (section id="data-storage"): Update storage details
   - **Third Parties** (section id="data-sharing"): Add new services

3. **Update the last modified date**:
   ```html
   <footer>
     <p>Last Updated: YYYY-MM-DD</p>
   </footer>
   ```

4. **Commit and push changes**:
   ```bash
   git add docs/privacy-policy.html
   git commit -m "Update privacy policy: [describe changes]"
   git push origin main
   ```

5. **Verify deployment**:
   - Wait 2-5 minutes for GitHub Pages to rebuild
   - Visit the live URL and verify changes
   - Check the Actions tab: https://github.com/Eeshan-Vaghjiani/StudentWorkflowappKotlin/actions

6. **Notify users** (if required by law):
   - Send in-app notification about policy changes
   - Email users if you have their consent
   - Update "What's New" in Play Store listing

### Example Updates

#### Adding Location Data Collection
```html
<!-- In data-collection section -->
<li>
  <strong>Location Data:</strong> We collect your approximate location 
  to show nearby team members and suggest local meeting times.
</li>
```

#### Adding New Third-Party Service
```html
<!-- In data-sharing section -->
<li>
  <strong>Analytics Service (e.g., Google Analytics):</strong> We use 
  analytics to understand how users interact with the app. This data 
  is anonymized and aggregated.
</li>
```

## Testing Account Deletion

### IMPORTANT: Use Test Accounts Only

**Never test account deletion with production user accounts!**

### Creating a Test Account

1. **In your Android app**:
   - Open the app
   - Sign up with a test email (e.g., `test-delete-001@example.com`)
   - Create some test data:
     - Send a few messages
     - Create a task
     - Join a group

2. **Verify test data in Firebase Console**:
   - Go to: https://console.firebase.google.com/project/android-logreg/firestore
   - Check that test user appears in `users` collection
   - Check that test messages appear in `messages` collection
   - Check that test tasks appear in `tasks` collection

### Testing the Deletion Flow

1. **Navigate to deletion page**:
   ```
   https://eeshan-vaghjiani.github.io/StudentWorkflowappKotlin/delete-account.html
   ```

2. **Enter test account credentials**:
   - Email: `test-delete-001@example.com`
   - Password: [test account password]
   - Check the confirmation checkbox

3. **Submit the form**:
   - Click "Delete My Account"
   - Observe loading spinner
   - Wait for success message

4. **Verify deletion in Firebase Console**:
   - **Authentication**: User should be removed from Authentication users list
   - **Firestore**: 
     - User document should be deleted from `users` collection
     - Messages from this user should be deleted from `messages` collection
     - Tasks created by this user should be deleted from `tasks` collection
     - Group memberships should be deleted from `groupMembers` collection

5. **Verify account cannot be used**:
   - Try to sign in with deleted credentials in the app
   - Should receive "user not found" error

### Testing Error Scenarios

1. **Wrong Password**:
   - Enter correct email but wrong password
   - Should see: "Invalid email or password"

2. **Non-existent Email**:
   - Enter email that doesn't exist
   - Should see: "Invalid email or password" (same message for security)

3. **Empty Fields**:
   - Leave email or password empty
   - Submit button should remain disabled

4. **No Confirmation**:
   - Fill email and password but don't check confirmation
   - Submit button should remain disabled

5. **Rate Limiting**:
   - Submit deletion request 4 times rapidly
   - Should see rate limit error on 4th attempt

### Testing on Different Devices

- **Desktop**: Chrome, Firefox, Safari, Edge
- **Mobile**: Android Chrome, iOS Safari
- **Tablet**: iPad Safari, Android Chrome
- **Screen sizes**: 320px, 768px, 1024px, 1920px

## Security Testing

### Security Testing Suite

We provide a comprehensive security testing suite to verify all security requirements for Play Store compliance:

**Tool Location**: `docs/test-security.html`

**Features**:
- Automated security tests for all requirements
- HTTPS enforcement verification
- Credential logging prevention checks
- Storage security validation
- XSS prevention testing
- Firebase config exposure verification
- CSRF protection testing
- Error message security validation
- Rate limiting functionality tests

**Quick Start**:
1. Open `docs/test-security.html` in your browser
2. Click "Run All Tests" to execute all security tests
3. Review results for each test section
4. Address any failures or warnings
5. Document results in `SECURITY_TEST_RESULTS.md`

**Security Requirements Tested**:
- ✅ **6.1**: HTTPS Enforcement
- ✅ **6.2**: Credential Logging Prevention
- ✅ **6.3**: Storage Security
- ✅ **6.4**: CSRF Protection
- ✅ **6.5**: Error Message Security
- ✅ **6.7**: XSS Prevention
- ✅ **6.8**: Firebase Config Exposure
- ✅ **3.9**: Rate Limiting

### Security Testing Documentation

**Comprehensive Guide**: `SECURITY_TEST_GUIDE.md`
- Detailed test procedures for all security requirements
- Manual verification instructions
- Expected results and pass/fail criteria
- Troubleshooting guide
- Compliance verification checklist

**Test Results**: `SECURITY_TEST_RESULTS.md`
- Track security testing progress
- Document test outcomes
- Record issues and resolutions
- Sign-off template for compliance

### Key Security Features

**Input Sanitization**:
- ✅ XSS prevention through HTML encoding
- ✅ Script tag removal
- ✅ JavaScript protocol blocking
- ✅ Event handler attribute removal

**Rate Limiting**:
- ✅ Maximum 3 attempts per hour
- ✅ Stored in sessionStorage (cleared on browser close)
- ✅ Automatic cleanup of old attempts
- ✅ User-friendly error messages

**CSRF Protection**:
- ✅ Origin validation
- ✅ Referrer checking
- ✅ Same-origin policy enforcement
- ✅ Allowed domains: localhost, GitHub Pages

**Secure Logging**:
- ✅ No passwords logged
- ✅ No email addresses logged
- ✅ No authentication tokens logged
- ✅ Only generic security events logged

**Error Message Security**:
- ✅ Ambiguous authentication errors
- ✅ Prevents account enumeration
- ✅ Same message for all auth failures

### Security Testing Checklist

Use the automated tool or manual testing to verify:

**HTTPS Enforcement**:
- [ ] Page loads with https:// protocol
- [ ] Lock icon visible in address bar
- [ ] Valid SSL certificate
- [ ] GitHub Pages automatic HTTPS enabled

**Credential Logging**:
- [ ] No passwords in console logs
- [ ] No email addresses in console logs
- [ ] No tokens in console logs
- [ ] Only generic security events logged

**Storage Security**:
- [ ] No sensitive data in localStorage
- [ ] Only rate limit data in sessionStorage
- [ ] Rate limit data contains only timestamps
- [ ] No credentials stored anywhere

**XSS Prevention**:
- [ ] Script tags are sanitized
- [ ] Event handlers are removed
- [ ] JavaScript protocols are blocked
- [ ] No XSS payloads execute

**Firebase Config**:
- [ ] Firebase config is accessible (expected)
- [ ] API key restrictions configured
- [ ] Security rules protect data access
- [ ] Only authorized domains allowed

**CSRF Protection**:
- [ ] Origin validation is active
- [ ] Referrer validation is active
- [ ] Cross-origin requests are blocked
- [ ] Same-origin requests allowed

**Error Messages**:
- [ ] Authentication errors are ambiguous
- [ ] Cannot determine account existence
- [ ] All auth errors use same message
- [ ] Prevents account enumeration

**Rate Limiting**:
- [ ] Rate limit enforces 3 attempts per hour
- [ ] Rate limit blocks after max attempts
- [ ] Rate limit resets after time window
- [ ] Rate limit data stored correctly

## Responsive Design Testing

### Interactive Testing Tool

We provide an interactive testing tool to verify responsive design across different screen sizes:

**Tool Location**: `docs/test-responsive.html`

**Features**:
- Live viewport resizing with device presets
- Switch between Privacy Policy and Account Deletion pages
- Test portrait and landscape orientations
- Interactive checklist with progress tracking
- Automatic progress saving

**Quick Start**:
1. Open `docs/test-responsive.html` in your browser
2. Select a device preset (Mobile 320px, Tablet 768px, Desktop 1024px, etc.)
3. Switch between pages using the dropdown
4. Test both portrait and landscape orientations
5. Complete the checklist as you verify each test case

**Device Presets Available**:
- 📱 Mobile (320px) - iPhone SE, small Android
- 📱 iPhone (375px) - iPhone 12/13
- 📱 Large Phone (414px) - iPhone 12 Pro Max
- 📱 Tablet (768px) - iPad portrait
- 💻 Desktop (1024px) - iPad landscape, small desktop
- 🖥️ Large Desktop (1440px) - Standard desktop

### Testing Documentation

**Comprehensive Guide**: `RESPONSIVE_TESTING_GUIDE.md`
- Detailed test scenarios for all screen sizes
- Touch device testing procedures
- Browser compatibility testing
- Actual device testing instructions
- Issue reporting templates

**Quick Start Guide**: `RESPONSIVE_TESTING_QUICK_START.md`
- Get started in 5 minutes
- Quick testing workflow
- Success criteria
- Pro tips

**Test Results**: `RESPONSIVE_TEST_RESULTS.md`
- Track testing progress
- Document issues found
- Browser compatibility matrix
- Sign-off template

### Key Responsive Requirements

**Mobile (320px - 767px)**:
- ✅ No horizontal scrolling
- ✅ Minimum 16px font size
- ✅ Touch targets minimum 44x44px
- ✅ Form fields minimum 48px height
- ✅ Full-width buttons on mobile

**Tablet (768px - 1023px)**:
- ✅ Content centered with appropriate margins
- ✅ Increased font sizes and spacing
- ✅ Comfortable form layout
- ✅ Logo size increased to 48px

**Desktop (1024px+)**:
- ✅ Max-width applied (800px for content)
- ✅ Content centered
- ✅ Hover states on interactive elements
- ✅ Optimal typography for reading
- ✅ Form max-width (400px for buttons)

### Testing Checklist

Use the interactive tool or manual testing to verify:

**Privacy Policy Page**:
- [ ] Readable on mobile (320px) without horizontal scrolling
- [ ] Sections adapt properly on tablet (768px)
- [ ] Content centered on desktop (1024px+)
- [ ] Works in portrait and landscape orientations
- [ ] Images and layout adapt to screen size

**Account Deletion Page**:
- [ ] Form usable on mobile (320px) with touch-friendly controls
- [ ] Warning section prominent on all sizes
- [ ] Form comfortable on tablet (768px)
- [ ] Form centered on desktop (1024px+)
- [ ] Works in portrait and landscape orientations
- [ ] Touch targets meet 44x44px minimum

**General**:
- [ ] Typography readable (16px minimum)
- [ ] No overlapping elements
- [ ] Consistent spacing at all breakpoints
- [ ] Links and buttons easily clickable/tappable

## Troubleshooting Guide

### Issue: Pages Return 404 Error

**Symptoms**: Clicking the URLs shows "404 - Page Not Found"

**Causes**:
- GitHub Pages not enabled
- Wrong branch or folder selected
- Deployment still in progress
- Files not in correct location

**Solutions**:
1. Check GitHub Pages settings:
   - Go to: Settings → Pages
   - Verify source is "main" branch, "/docs" folder
   
2. Check deployment status:
   - Go to: Actions tab
   - Look for "pages build and deployment" workflow
   - Wait for green checkmark (may take 5-10 minutes)

3. Verify file locations:
   ```bash
   # Files should be in docs/ directory
   ls docs/
   # Should show: privacy-policy.html, delete-account.html, assets/
   ```

4. Force rebuild:
   ```bash
   # Make a small change and push
   git commit --allow-empty -m "Trigger GitHub Pages rebuild"
   git push origin main
   ```

### Issue: Account Deletion Fails with "Permission Denied"

**Symptoms**: Deletion fails with Firebase error about permissions

**Causes**:
- Firestore security rules not updated
- Rules not deployed to Firebase

**Solutions**:
1. Check Firestore rules in Firebase Console:
   - Go to: Firestore Database → Rules
   - Verify rules allow users to delete their own data

2. Deploy updated rules:
   ```bash
   firebase deploy --only firestore:rules
   ```

3. Required rules (add to `firestore.rules`):
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /users/{userId} {
         allow delete: if request.auth != null && request.auth.uid == userId;
       }
       
       match /messages/{messageId} {
         allow delete: if request.auth != null && 
                          resource.data.senderId == request.auth.uid;
       }
       
       match /tasks/{taskId} {
         allow delete: if request.auth != null && 
                          resource.data.createdBy == request.auth.uid;
       }
       
       match /groupMembers/{memberId} {
         allow delete: if request.auth != null && 
                          resource.data.userId == request.auth.uid;
       }
     }
   }
   ```

### Issue: Firebase Configuration Error

**Symptoms**: Console shows "Firebase initialization failed" or similar error

**Causes**:
- Incorrect Firebase config in `firebase-config.js`
- Firebase project settings changed
- API key restrictions blocking web access

**Solutions**:
1. Verify Firebase config:
   - Go to: Firebase Console → Project Settings → General
   - Scroll to "Your apps" section
   - Click on web app (</> icon)
   - Copy the config object

2. Update `docs/assets/js/firebase-config.js`:
   ```javascript
   const firebaseConfig = {
     apiKey: "YOUR_API_KEY",
     authDomain: "YOUR_PROJECT.firebaseapp.com",
     projectId: "YOUR_PROJECT_ID",
     storageBucket: "YOUR_PROJECT.firebasestorage.app",
     messagingSenderId: "YOUR_SENDER_ID",
     appId: "YOUR_APP_ID"
   };
   ```

3. Check API key restrictions:
   - Go to: Google Cloud Console → APIs & Services → Credentials
   - Find your API key
   - Ensure "HTTP referrers" includes your GitHub Pages domain

### Issue: Styling Not Applied

**Symptoms**: Pages load but look unstyled or broken

**Causes**:
- CSS files not loading
- Incorrect file paths
- Browser caching old version

**Solutions**:
1. Check browser console for 404 errors on CSS files

2. Verify CSS paths are relative:
   ```html
   <!-- Correct -->
   <link rel="stylesheet" href="assets/css/common.css">
   
   <!-- Incorrect -->
   <link rel="stylesheet" href="/assets/css/common.css">
   ```

3. Clear browser cache:
   - Chrome: Ctrl+Shift+Delete (Windows) or Cmd+Shift+Delete (Mac)
   - Or use incognito/private browsing mode

4. Force reload:
   - Chrome: Ctrl+F5 (Windows) or Cmd+Shift+R (Mac)

### Issue: JavaScript Not Working

**Symptoms**: Form doesn't validate, deletion doesn't work, no error messages

**Causes**:
- JavaScript files not loading
- Firebase SDK version mismatch
- Browser blocking scripts

**Solutions**:
1. Check browser console for errors:
   - Press F12 to open DevTools
   - Go to Console tab
   - Look for red error messages

2. Verify JavaScript files load:
   - Go to Network tab in DevTools
   - Reload page
   - Check that .js files return 200 status

3. Check Firebase SDK version:
   ```javascript
   // In firebase-config.js, ensure version is 10.x or later
   import { initializeApp } from 
     'https://www.gstatic.com/firebasejs/10.7.1/firebase-app.js';
   ```

4. Disable browser extensions:
   - Ad blockers may block Firebase
   - Try in incognito mode

### Issue: Rate Limiting Not Working

**Symptoms**: Can submit deletion requests unlimited times

**Causes**:
- SessionStorage cleared between attempts
- Testing in different browsers/tabs

**Solutions**:
- Rate limiting is per-browser session
- Use same browser tab for testing
- Clear sessionStorage to reset: `sessionStorage.clear()`

### Issue: Mobile Layout Broken

**Symptoms**: Page doesn't fit on mobile screen, text too small, buttons too small

**Causes**:
- Missing viewport meta tag
- CSS not responsive
- Touch targets too small

**Solutions**:
1. Verify viewport meta tag:
   ```html
   <meta name="viewport" content="width=device-width, initial-scale=1.0">
   ```

2. Test responsive design:
   - Chrome DevTools → Toggle device toolbar (Ctrl+Shift+M)
   - Test at 320px, 375px, 768px, 1024px

3. Check minimum font size (16px) and touch targets (44x44px)

## Rollback Procedure

If issues are discovered after deployment, follow this procedure to rollback:

### Quick Rollback (Revert to Previous Version)

1. **Identify the last working commit**:
   ```bash
   git log --oneline docs/
   # Find the commit hash before the problematic changes
   ```

2. **Revert to that commit**:
   ```bash
   # Option A: Revert specific files
   git checkout <commit-hash> -- docs/privacy-policy.html
   git checkout <commit-hash> -- docs/delete-account.html
   git checkout <commit-hash> -- docs/assets/
   
   # Option B: Revert entire docs directory
   git checkout <commit-hash> -- docs/
   ```

3. **Commit and push**:
   ```bash
   git add docs/
   git commit -m "Rollback: Revert to working version from <commit-hash>"
   git push origin main
   ```

4. **Verify rollback**:
   - Wait 2-5 minutes for GitHub Pages to rebuild
   - Test both URLs
   - Check Actions tab for successful deployment

### Emergency Rollback (Disable Pages Temporarily)

If you need to immediately take down the pages:

1. **Disable GitHub Pages**:
   - Go to: Settings → Pages
   - Under "Source", select "None"
   - Click "Save"

2. **Pages will return 404 within minutes**

3. **Fix issues locally**:
   - Make necessary corrections
   - Test thoroughly locally

4. **Re-enable GitHub Pages**:
   - Go to: Settings → Pages
   - Set source back to "main" branch, "/docs" folder
   - Click "Save"

### Rollback Firebase Security Rules

If you need to rollback Firestore rules:

1. **View rule history in Firebase Console**:
   - Go to: Firestore Database → Rules
   - Click "History" tab
   - Find previous working version

2. **Restore previous rules**:
   - Click on previous version
   - Click "Restore"
   - Confirm restoration

3. **Or deploy from local backup**:
   ```bash
   # If you have firestore.rules.backup file
   cp firestore.rules.backup firestore.rules
   firebase deploy --only firestore:rules
   ```

### Post-Rollback Actions

1. **Document the issue**:
   - What went wrong
   - What was the impact
   - How it was discovered

2. **Notify stakeholders**:
   - If Play Store submission was affected
   - If users were impacted

3. **Fix and test**:
   - Fix the issue in a separate branch
   - Test thoroughly before merging
   - Consider staging environment for future changes

4. **Update documentation**:
   - Add the issue to troubleshooting guide
   - Update testing procedures if needed

## Maintenance Checklist

### Monthly Tasks
- [ ] Test account deletion with test account
- [ ] Verify both pages load correctly
- [ ] Check Firebase quota usage
- [ ] Review error logs in Firebase Console
- [ ] Test on latest browser versions

### Quarterly Tasks
- [ ] Review and update privacy policy if features changed
- [ ] Update Firebase SDK to latest version
- [ ] Run Lighthouse audit on both pages
- [ ] Review Firestore security rules
- [ ] Check for broken links

### Annual Tasks
- [ ] Comprehensive privacy policy review
- [ ] Legal compliance review (GDPR, COPPA, etc.)
- [ ] Accessibility audit (WCAG 2.1 AA)
- [ ] Performance optimization review
- [ ] Update copyright year in footer

## Support and Contact

### For Technical Issues
- **Repository Issues**: https://github.com/Eeshan-Vaghjiani/StudentWorkflowappKotlin/issues
- **Firebase Support**: https://firebase.google.com/support

### For Privacy Inquiries
- **Email**: [Add your privacy contact email]
- **Response Time**: Within 48 hours

### For Play Store Submission
- **Google Play Console**: https://play.google.com/console
- **Policy Help**: https://support.google.com/googleplay/android-developer/answer/9888076

## Additional Resources

- **GitHub Pages Documentation**: https://docs.github.com/en/pages
- **Firebase Documentation**: https://firebase.google.com/docs
- **GDPR Compliance**: https://gdpr.eu/
- **COPPA Compliance**: https://www.ftc.gov/business-guidance/resources/childrens-online-privacy-protection-rule-six-step-compliance-plan-your-business
- **Play Store Data Safety**: https://support.google.com/googleplay/android-developer/answer/10787469
- **WCAG Guidelines**: https://www.w3.org/WAI/WCAG21/quickref/

## Version History

- **v1.0.0** (2024-11-07): Initial deployment
  - Privacy policy page created
  - Account deletion page created
  - Firebase integration implemented
  - GitHub Pages deployment configured

---

**Last Updated**: 2024-11-07
**Maintained By**: Development Team
**License**: Proprietary - Team Collaboration App
