# Task 23: Link Detection Testing Guide

## Quick Test Scenarios

### Test 1: Basic HTTPS URL
**Message:** `Check out https://www.google.com`

**Expected:**
- URL appears in blue color
- URL is underlined
- Tapping opens Google in browser

---

### Test 2: HTTP URL
**Message:** `Visit http://example.com`

**Expected:**
- URL is detected and clickable
- Opens in browser with http scheme

---

### Test 3: WWW URL (No Scheme)
**Message:** `Go to www.github.com`

**Expected:**
- URL is detected and clickable
- Opens with https:// prefix automatically

---

### Test 4: Multiple URLs
**Message:** `Check https://google.com and www.github.com for more info`

**Expected:**
- Both URLs are blue and underlined
- Each URL is independently clickable
- Both open correctly in browser

---

### Test 5: URL with Path and Query
**Message:** `https://example.com/path/to/page?id=123&name=test#section`

**Expected:**
- Entire URL is detected as one link
- Clicking opens the full URL with all parameters

---

### Test 6: Mixed Content
**Message:** `Our website is https://example.com and you can email us too`

**Expected:**
- Only the URL is highlighted
- Regular text remains normal
- URL is clickable

---

### Test 7: URL at Start
**Message:** `https://example.com is our website`

**Expected:**
- URL at beginning is detected
- Clickable and opens correctly

---

### Test 8: URL at End
**Message:** `Visit our site at https://example.com`

**Expected:**
- URL at end is detected
- Clickable and opens correctly

---

### Test 9: Sent Message with URL
**Action:** Send a message with a URL from your account

**Expected:**
- URL appears blue and underlined in sent message (right side)
- Clicking opens browser

---

### Test 10: Received Message with URL
**Action:** Receive a message with a URL from another user

**Expected:**
- URL appears blue and underlined in received message (left side)
- Clicking opens browser

---

### Test 11: Image Message with URL Caption
**Action:** Send an image with caption containing a URL

**Expected:**
- URL in caption is clickable
- Image displays normally

---

### Test 12: Document Message with URL Text
**Action:** Send a document with text containing a URL

**Expected:**
- URL in text is clickable
- Document displays normally

---

## Edge Cases to Test

### Edge Case 1: Very Long URL
**Message:** `https://example.com/very/long/path/with/many/segments/and/parameters?param1=value1&param2=value2&param3=value3`

**Expected:**
- Entire URL is detected
- May wrap to multiple lines
- Still clickable

---

### Edge Case 2: URL with Special Characters
**Message:** `https://example.com/path?query=hello+world&name=John%20Doe`

**Expected:**
- URL with encoded characters is detected
- Opens correctly in browser

---

### Edge Case 3: No Browser Available
**Action:** Test on device with no browser (unlikely but possible)

**Expected:**
- App doesn't crash
- Error is handled gracefully

---

### Edge Case 4: Invalid URL
**Message:** `https://invalid..url..com`

**Expected:**
- May be detected as URL
- Browser handles invalid URL appropriately

---

## Visual Verification Checklist

- [ ] URLs are displayed in blue color (#2196F3)
- [ ] URLs are underlined
- [ ] URLs are visually distinct from regular text
- [ ] Clicking a URL shows no visible highlight color
- [ ] URLs in sent messages (right side) work correctly
- [ ] URLs in received messages (left side) work correctly
- [ ] Multiple URLs in one message are all clickable
- [ ] Text around URLs is not affected

---

## Functional Verification Checklist

- [ ] Tapping URL opens default browser
- [ ] Browser opens to correct URL
- [ ] URLs without scheme get https:// prefix
- [ ] Multiple URLs in one message all work
- [ ] URLs work in both sent and received messages
- [ ] URLs work in messages with images
- [ ] URLs work in messages with documents
- [ ] App doesn't crash on invalid URLs
- [ ] URLs with paths and queries work correctly
- [ ] URLs with fragments (#section) work correctly

---

## Performance Verification

- [ ] Link detection doesn't cause lag when loading messages
- [ ] Scrolling through messages with URLs is smooth
- [ ] Opening URLs is responsive (no delay)
- [ ] Multiple URLs in one message don't cause performance issues

---

## Regression Testing

After implementing this feature, verify that:
- [ ] Regular text messages still display correctly
- [ ] Image messages still work
- [ ] Document messages still work
- [ ] Message timestamps still display
- [ ] Read receipts still work
- [ ] Sender profile pictures still display
- [ ] Message grouping still works
- [ ] Scroll to bottom still works

---

## Test Data Examples

Copy these messages to test quickly:

```
https://www.google.com
http://example.com
www.github.com
Check out https://stackoverflow.com for help
Visit https://firebase.google.com and www.android.com
https://example.com/path/to/page?id=123&name=test#section
Our site: https://example.com
https://example.com is our website
Multiple links: https://google.com, www.github.com, http://example.com
```

---

## Success Criteria

✅ All basic test scenarios pass
✅ All edge cases are handled gracefully
✅ Visual appearance matches design
✅ No performance degradation
✅ No regression in existing features
✅ URLs open correctly in browser
✅ Works on both sent and received messages
