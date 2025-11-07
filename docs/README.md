# Play Store Compliance Pages

This directory contains the HTML pages required for Google Play Store compliance for the **Team Collaboration App** (package name: `com.example.loginandregistration`).

## Purpose

These pages fulfill Google Play Store requirements for:
1. **Privacy Policy** - Transparent disclosure of data collection and usage practices
2. **Account Deletion** - User-initiated account and data deletion capability (GDPR compliance)

## Pages

### 1. Privacy Policy (`privacy-policy.html`)
- **URL**: `https://[username].github.io/[repo]/privacy-policy.html`
- **Purpose**: Discloses what data the app collects, how it's used, stored, and shared
- **Requirements**: Required by Google Play Store for all apps that collect user data

**Content Includes:**
- Data types collected (email, profile pictures, messages, tasks, groups, FCM tokens)
- How data is used (authentication, app functionality, push notifications)
- Where data is stored (Firebase services)
- Third-party services (Google Firebase)
- User rights (access, correction, deletion, data portability)
- COPPA compliance for children under 13
- Contact information for privacy inquiries

### 2. Account Deletion (`delete-account.html`)
- **URL**: `https://[username].github.io/[repo]/delete-account.html`
- **Purpose**: Allows users to delete their account and associated data
- **Requirements**: Required by Google Play Store and GDPR for user data control

**Functionality:**
- User authentication via Firebase
- Deletion of user profile, messages, tasks, and group memberships
- Deletion of Firebase Authentication account
- Clear warnings about irreversibility
- Confirmation checkbox required
- Success/error feedback

## Directory Structure

```
docs/
├── README.md                   # This file
├── index.html                  # Landing page with redirects
├── privacy-policy.html         # Privacy policy page
├── delete-account.html         # Account deletion page
└── assets/
    ├── css/
    │   ├── common.css         # Shared styles for both pages
    │   ├── privacy.css        # Privacy policy specific styles
    │   └── delete.css         # Account deletion specific styles
    ├── js/
    │   ├── firebase-config.js # Firebase initialization
    │   └── delete-account.js  # Account deletion logic
    └── images/
        ├── logo.webp          # App logo
        └── favicon.ico        # Favicon
```

## Deployment

These pages are hosted on **GitHub Pages** and automatically deployed when changes are pushed to the main branch.

### Setup Instructions

1. **Enable GitHub Pages**:
   - Go to repository Settings → Pages
   - Set source to "Deploy from a branch"
   - Select "main" branch and "/docs" folder
   - Save changes

2. **Verify Deployment**:
   - Wait 2-5 minutes for GitHub Actions to build
   - Visit: `https://[username].github.io/[repo]/privacy-policy.html`
   - Visit: `https://[username].github.io/[repo]/delete-account.html`

3. **Update Play Store Console**:
   - Go to Play Console → App content → Privacy policy
   - Enter the privacy policy URL
   - Go to Play Console → App content → Data safety → Data deletion
   - Enter the account deletion URL

## Maintenance

### Updating the Privacy Policy

When app features change that affect data collection or usage:

1. Open `privacy-policy.html`
2. Update the relevant sections (data collection, usage, etc.)
3. Update the "Last Updated" date at the bottom
4. Commit and push changes
5. Verify deployment on GitHub Pages
6. No action needed in Play Store Console (URL remains the same)

### Updating Account Deletion

If the data model changes (new collections, fields):

1. Open `delete-account.js`
2. Update the `deleteUserData()` function to include new collections
3. Update `delete-account.html` to list new data types being deleted
4. Test thoroughly with a test account
5. Commit and push changes
6. Verify deployment on GitHub Pages

### Firebase Security Rules

The account deletion functionality requires specific Firestore security rules. Ensure these rules are deployed:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow users to delete their own data
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

## Testing

### Privacy Policy Testing
- [ ] Verify all sections are present and accurate
- [ ] Test on mobile, tablet, and desktop devices
- [ ] Validate HTML using W3C validator
- [ ] Check accessibility with WAVE tool
- [ ] Verify all links work correctly

### Account Deletion Testing

**Basic Functionality:**
- [ ] Create a test Firebase account
- [ ] Add sample data (messages, tasks, groups)
- [ ] Test successful deletion flow
- [ ] Verify account is deleted from Firebase Auth
- [ ] Verify data is deleted from Firestore
- [ ] Test error scenarios (wrong password, network error)
- [ ] Test on mobile and desktop

**Security Testing:**
- [ ] Test rate limiting by submitting 4 requests rapidly (4th should be blocked)
- [ ] Verify rate limit resets after 1 hour
- [ ] Test CSRF protection by accessing from different origin (if possible)
- [ ] Test XSS prevention by entering `<script>alert('xss')</script>` in email field
- [ ] Verify no credentials appear in browser console logs
- [ ] Verify no sensitive data in localStorage or sessionStorage (except rate limit)
- [ ] Test with invalid Firebase responses (disconnect network mid-request)
- [ ] Verify error messages don't reveal account existence
- [ ] Check that password field is cleared after failed attempt
- [ ] Verify form is disabled during deletion process

## Troubleshooting

### Pages Not Loading
- Check GitHub Pages is enabled in repository settings
- Verify files are in the `docs/` directory
- Check GitHub Actions tab for deployment errors
- Wait 5 minutes after pushing changes

### Account Deletion Not Working
- Verify Firebase configuration in `firebase-config.js` is correct
- Check Firebase security rules are deployed
- Check browser console for JavaScript errors
- Verify user credentials are correct
- Check Firebase Authentication and Firestore in Firebase Console

### HTTPS Certificate Errors
- GitHub Pages automatically provides HTTPS
- If using custom domain, verify DNS settings
- Check CNAME file is present and correct

## Security Considerations

### Implemented Security Safeguards

The account deletion page includes comprehensive security measures:

1. **Rate Limiting**
   - Maximum 3 deletion attempts per hour per browser session
   - Stored in sessionStorage (cleared when browser closes)
   - Prevents brute force attacks and abuse
   - User-friendly error messages with reset time

2. **CSRF Protection**
   - Validates request origin matches expected domain
   - Supports localhost (development), GitHub Pages, and custom domains
   - Prevents cross-site request forgery attacks

3. **Input Sanitization**
   - All user inputs sanitized to prevent XSS attacks
   - Email addresses validated with regex patterns
   - Malicious patterns detected and blocked
   - Content Security Policy (CSP) headers in HTML

4. **Credential Protection**
   - No credentials logged to console
   - No sensitive data stored in localStorage or sessionStorage (except rate limit timestamps)
   - Passwords never logged or exposed
   - Security logger filters sensitive data from all logs

5. **Firebase Response Validation**
   - All Firebase responses validated before processing
   - User objects, snapshots, and errors checked for integrity
   - Invalid responses handled gracefully with appropriate errors

6. **Additional Security**
   - Firebase API keys are exposed in client-side code (this is acceptable for public Firebase projects)
   - Error messages intentionally don't reveal whether accounts exist
   - All communications use HTTPS
   - Form fields cleared after submission (success or failure)

## Compliance Checklist

Use this checklist when submitting to Play Store:

- [ ] Privacy policy URL is publicly accessible via HTTPS
- [ ] Privacy policy mentions app name "Team Collaboration App"
- [ ] Privacy policy lists all data types collected
- [ ] Privacy policy explains how data is used
- [ ] Privacy policy includes contact email
- [ ] Privacy policy includes last updated date
- [ ] Account deletion URL is publicly accessible via HTTPS
- [ ] Account deletion page shows app name
- [ ] Account deletion page lists what data will be deleted
- [ ] Account deletion page requires authentication
- [ ] Account deletion page requires confirmation
- [ ] Account deletion page specifies data retention periods
- [ ] Both pages are mobile-responsive
- [ ] Both pages meet WCAG AA accessibility standards

## Contact

For questions about these compliance pages, contact:
- **Email**: [your-email@example.com]
- **Repository**: [link to GitHub repository]

## License

These pages are part of the Team Collaboration App project and follow the same license as the main application.

---

**Last Updated**: 2025-11-07
