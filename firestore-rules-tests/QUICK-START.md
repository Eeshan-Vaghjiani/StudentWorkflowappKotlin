# Quick Start - Running Firebase Rules Tests

## TL;DR

```bash
# Terminal 1 - Start emulators (keep running)
firebase emulators:start

# Terminal 2 - Run tests
cd firestore-rules-tests
npm test
```

## What Gets Tested

✅ **117 test cases** covering:
- Authentication & access control
- Public group discovery
- Member & participant limits
- File upload validation
- Message content validation
- All 12 security requirements

## Test Results

When emulators are running, you should see:
```
Test Suites: 2 passed, 2 total
Tests:       117 passed, 117 total
```

## Troubleshooting

**Error: "FetchError: request to http://localhost:8080/... failed"**
→ Start the emulators first: `firebase emulators:start`

**Tests failing?**
→ Check that `firestore.rules` and `storage.rules` are up to date

## More Info

See `README-TESTING.md` for detailed documentation.
