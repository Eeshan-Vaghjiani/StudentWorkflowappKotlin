const {
    assertFails,
    assertSucceeds,
    initializeTestEnvironment,
} = require('@firebase/rules-unit-testing');
const fs = require('fs');
const path = require('path');

let testEnv;

// Test data
const USER1_ID = 'user1';
const USER2_ID = 'user2';

beforeAll(async () => {
    // Initialize test environment with Firestore rules
    testEnv = await initializeTestEnvironment({
        projectId: 'test-project',
        firestore: {
            rules: fs.readFileSync(path.resolve(__dirname, '../firestore.rules'), 'utf8'),
            host: 'localhost',
            port: 8080
        }
    });
});

afterAll(async () => {
    await testEnv.cleanup();
});

beforeEach(async () => {
    await testEnv.clearFirestore();
});

describe('Users Collection - Create Permissions', () => {
    test('user can create their own profile with uid field', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                uid: USER1_ID,
                email: 'user1@example.com',
                displayName: 'User One',
                photoUrl: '',
                online: true,
                createdAt: new Date()
            })
        );
    });

    test('user can create their own profile with userId field (backward compatibility)', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER1_ID,
                email: 'user1@example.com',
                displayName: 'User One',
                photoUrl: '',
                online: true,
                createdAt: new Date()
            })
        );
    });

    test('user can create their own profile with both uid and userId fields', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                uid: USER1_ID,
                userId: USER1_ID,
                email: 'user1@example.com',
                displayName: 'User One',
                firstName: 'User',
                lastName: 'One',
                photoUrl: '',
                profileImageUrl: '',
                online: true,
                isOnline: true,
                lastSeen: new Date(),
                lastActive: new Date(),
                createdAt: new Date(),
                fcmToken: '',
                aiUsageCount: 0,
                aiPromptsUsed: 0,
                aiPromptsLimit: 10
            })
        );
    });

    test('user cannot create profile for another user', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('users').doc(USER2_ID).set({
                uid: USER2_ID,
                email: 'user2@example.com',
                displayName: 'User Two',
                online: true
            })
        );
    });

    test('user cannot create profile with mismatched uid', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                uid: USER2_ID, // Mismatched uid
                email: 'user1@example.com',
                displayName: 'User One',
                online: true
            })
        );
    });

    test('user cannot create profile with mismatched userId', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER2_ID, // Mismatched userId
                email: 'user1@example.com',
                displayName: 'User One',
                online: true
            })
        );
    });

    test('user cannot create profile without email', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                uid: USER1_ID,
                displayName: 'User One',
                online: true
            })
        );
    });

    test('user cannot create profile with empty displayName', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                uid: USER1_ID,
                email: 'user1@example.com',
                displayName: '', // Empty displayName
                online: true
            })
        );
    });

    test('user cannot create profile with displayName exceeding 100 characters', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const longName = 'a'.repeat(101);

        await assertFails(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                uid: USER1_ID,
                email: 'user1@example.com',
                displayName: longName,
                online: true
            })
        );
    });

    test('unauthenticated user cannot create profile', async () => {
        const unauthContext = testEnv.unauthenticatedContext();

        await assertFails(
            unauthContext.firestore().collection('users').doc(USER1_ID).set({
                uid: USER1_ID,
                email: 'user1@example.com',
                displayName: 'User One',
                online: true
            })
        );
    });
});

describe('Users Collection - Update Permissions', () => {
    test('user can update their own profile with uid field', async () => {
        // Setup: Create user profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                uid: USER1_ID,
                email: 'user1@example.com',
                displayName: 'User One',
                online: false
            });
        });

        // Test: user1 can update their profile
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).update({
                uid: USER1_ID,
                online: true,
                lastActive: new Date()
            })
        );
    });

    test('user can update their own profile with userId field (backward compatibility)', async () => {
        // Setup: Create user profile with userId
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER1_ID,
                email: 'user1@example.com',
                displayName: 'User One',
                online: false
            });
        });

        // Test: user1 can update their profile
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).update({
                userId: USER1_ID,
                online: true,
                lastActive: new Date()
            })
        );
    });

    test('user cannot update another user profile', async () => {
        // Setup: Create user2 profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER2_ID).set({
                uid: USER2_ID,
                email: 'user2@example.com',
                displayName: 'User Two',
                online: false
            });
        });

        // Test: user1 cannot update user2's profile
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore().collection('users').doc(USER2_ID).update({
                online: true
            })
        );
    });

    test('user cannot change their uid during update', async () => {
        // Setup: Create user profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                uid: USER1_ID,
                email: 'user1@example.com',
                displayName: 'User One',
                online: false
            });
        });

        // Test: user1 cannot change their uid
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore().collection('users').doc(USER1_ID).update({
                uid: USER2_ID, // Trying to change uid
                online: true
            })
        );
    });

    test('user cannot change their email during update', async () => {
        // Setup: Create user profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                uid: USER1_ID,
                email: 'user1@example.com',
                displayName: 'User One',
                online: false
            });
        });

        // Test: user1 cannot change their email
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore().collection('users').doc(USER1_ID).update({
                uid: USER1_ID,
                email: 'newemail@example.com', // Trying to change email
                online: true
            })
        );
    });
});

describe('Users Collection - Read Permissions', () => {
    test('authenticated user can read any user profile', async () => {
        // Setup: Create user2 profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER2_ID).set({
                uid: USER2_ID,
                email: 'user2@example.com',
                displayName: 'User Two',
                online: true
            });
        });

        // Test: user1 can read user2's profile
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER2_ID).get()
        );
    });

    test('authenticated user can query users collection', async () => {
        // Setup: Create multiple users
        await testEnv.withSecurityRulesDisabled(async (context) => {
            const db = context.firestore();
            await db.collection('users').doc(USER1_ID).set({
                uid: USER1_ID,
                email: 'user1@example.com',
                displayName: 'User One',
                online: true
            });

            await db.collection('users').doc(USER2_ID).set({
                uid: USER2_ID,
                email: 'user2@example.com',
                displayName: 'User Two',
                online: false
            });
        });

        // Test: user1 can query users
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const db = user1Context.firestore();
        await assertSucceeds(
            db.collection('users').get()
        );
    });

    test('unauthenticated user cannot read user profiles', async () => {
        // Setup: Create user profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                uid: USER1_ID,
                email: 'user1@example.com',
                displayName: 'User One',
                online: true
            });
        });

        // Test: unauthenticated user cannot read
        const unauthContext = testEnv.unauthenticatedContext();
        await assertFails(
            unauthContext.firestore().collection('users').doc(USER1_ID).get()
        );
    });
});

describe('Users Collection - Delete Permissions', () => {
    test('user can delete their own profile', async () => {
        // Setup: Create user profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                uid: USER1_ID,
                email: 'user1@example.com',
                displayName: 'User One',
                online: true
            });
        });

        // Test: user1 can delete their profile
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).delete()
        );
    });

    test('user cannot delete another user profile', async () => {
        // Setup: Create user2 profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER2_ID).set({
                uid: USER2_ID,
                email: 'user2@example.com',
                displayName: 'User Two',
                online: true
            });
        });

        // Test: user1 cannot delete user2's profile
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore().collection('users').doc(USER2_ID).delete()
        );
    });
});
