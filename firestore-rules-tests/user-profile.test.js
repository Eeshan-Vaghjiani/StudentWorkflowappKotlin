const {
    assertFails,
    assertSucceeds,
    initializeTestEnvironment,
    RulesTestEnvironment
} = require('@firebase/rules-unit-testing');
const fs = require('fs');
const path = require('path');

let testEnv;

// Test data
const USER1_ID = 'user1';
const USER2_ID = 'user2';
const USER3_ID = 'user3';

beforeAll(async () => {
    // Initialize test environment with Firestore rules
    testEnv = await initializeTestEnvironment({
        projectId: 'test-project-user-profiles',
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

describe('User Profile Security Rules - Create Permissions', () => {
    test('user can create their own profile', async () => {
        // Requirement 3.1: Allow users to create their own profile
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER1_ID,
                displayName: 'Test User 1',
                email: 'user1@example.com',
                photoUrl: 'https://example.com/photo1.jpg',
                online: false,
                createdAt: new Date(),
                lastActive: new Date()
            })
        );
    });

    test('user can create profile with uid field (backward compatibility)', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                uid: USER1_ID,
                displayName: 'Test User 1',
                email: 'user1@example.com',
                photoUrl: 'https://example.com/photo1.jpg',
                online: false,
                createdAt: new Date(),
                lastActive: new Date()
            })
        );
    });

    test('user cannot create profile for another user', async () => {
        // Requirement 3.2: Deny creation of profiles for other users
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('users').doc(USER2_ID).set({
                userId: USER2_ID,
                displayName: 'Test User 2',
                email: 'user2@example.com',
                photoUrl: 'https://example.com/photo2.jpg',
                online: false,
                createdAt: new Date(),
                lastActive: new Date()
            })
        );
    });

    test('user cannot create profile with mismatched userId', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER2_ID, // Mismatched userId
                displayName: 'Test User',
                email: 'user@example.com',
                online: false
            })
        );
    });

    test('user cannot create profile without displayName', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER1_ID,
                email: 'user1@example.com',
                // Missing displayName
                online: false
            })
        );
    });

    test('user cannot create profile without email', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER1_ID,
                displayName: 'Test User',
                // Missing email
                online: false
            })
        );
    });

    test('user cannot create profile with displayName exceeding 100 characters', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const longName = 'a'.repeat(101);

        await assertFails(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER1_ID,
                displayName: longName,
                email: 'user1@example.com',
                online: false
            })
        );
    });

    test('user can create profile with optional fields', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER1_ID,
                displayName: 'Test User 1',
                email: 'user1@example.com',
                photoUrl: 'https://example.com/photo.jpg',
                fcmToken: 'fcm_token_123',
                online: true,
                createdAt: new Date(),
                lastActive: new Date()
            })
        );
    });

    test('unauthenticated user cannot create profile', async () => {
        const unauthContext = testEnv.unauthenticatedContext();

        await assertFails(
            unauthContext.firestore().collection('users').doc(USER1_ID).set({
                userId: USER1_ID,
                displayName: 'Test User',
                email: 'user@example.com',
                online: false
            })
        );
    });
});

describe('User Profile Security Rules - Read Permissions', () => {
    test('authenticated user can read any user profile', async () => {
        // Requirement 3.3, 3.4: Allow all authenticated users to read profiles
        // Setup: Create user2's profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER2_ID).set({
                userId: USER2_ID,
                displayName: 'Test User 2',
                email: 'user2@example.com',
                photoUrl: 'https://example.com/photo2.jpg',
                online: false
            });
        });

        // Test: user1 can read user2's profile
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER2_ID).get()
        );
    });

    test('authenticated user can read their own profile', async () => {
        // Setup: Create user1's profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER1_ID,
                displayName: 'Test User 1',
                email: 'user1@example.com',
                online: false
            });
        });

        // Test: user1 can read their own profile
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).get()
        );
    });

    test('authenticated user can query all user profiles', async () => {
        // Setup: Create multiple profiles
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER1_ID,
                displayName: 'User 1',
                email: 'user1@example.com',
                online: false
            });

            await context.firestore().collection('users').doc(USER2_ID).set({
                userId: USER2_ID,
                displayName: 'User 2',
                email: 'user2@example.com',
                online: false
            });
        });

        // Test: user3 can query all profiles
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertSucceeds(
            user3Context.firestore().collection('users').get()
        );
    });

    test('unauthenticated user cannot read user profiles', async () => {
        // Setup: Create a profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER1_ID,
                displayName: 'Test User',
                email: 'user@example.com',
                online: false
            });
        });

        // Test: unauthenticated user cannot read
        const unauthContext = testEnv.unauthenticatedContext();
        await assertFails(
            unauthContext.firestore().collection('users').doc(USER1_ID).get()
        );
    });
});

describe('User Profile Security Rules - Update Permissions', () => {
    test('user can update their own profile', async () => {
        // Setup: Create user1's profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER1_ID,
                displayName: 'Original Name',
                email: 'user1@example.com',
                online: false
            });
        });

        // Test: user1 can update their profile
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).update({
                displayName: 'Updated Name',
                lastActive: new Date()
            })
        );
    });

    test('user cannot update another user profile', async () => {
        // Requirement 3.5: Prevent users from updating other users' profiles
        // Setup: Create user2's profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER2_ID).set({
                userId: USER2_ID,
                displayName: 'User 2',
                email: 'user2@example.com',
                online: false
            });
        });

        // Test: user1 cannot update user2's profile
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore().collection('users').doc(USER2_ID).update({
                displayName: 'Hacked Name'
            })
        );
    });

    test('user cannot change their userId when updating', async () => {
        // Setup: Create user1's profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER1_ID,
                displayName: 'User 1',
                email: 'user1@example.com',
                online: false
            });
        });

        // Test: user1 cannot change their userId
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore().collection('users').doc(USER1_ID).update({
                userId: USER2_ID // Attempting to change userId
            })
        );
    });

    test('user cannot change their email when updating', async () => {
        // Setup: Create user1's profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER1_ID,
                displayName: 'User 1',
                email: 'user1@example.com',
                online: false
            });
        });

        // Test: user1 cannot change their email
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore().collection('users').doc(USER1_ID).update({
                email: 'newemail@example.com'
            })
        );
    });

    test('user can update optional fields', async () => {
        // Setup: Create user1's profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER1_ID,
                displayName: 'User 1',
                email: 'user1@example.com',
                online: false
            });
        });

        // Test: user1 can update optional fields
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).update({
                photoUrl: 'https://example.com/newphoto.jpg',
                fcmToken: 'new_fcm_token',
                online: true,
                lastActive: new Date()
            })
        );
    });

    test('unauthenticated user cannot update profile', async () => {
        // Setup: Create a profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER1_ID,
                displayName: 'User 1',
                email: 'user1@example.com',
                online: false
            });
        });

        // Test: unauthenticated user cannot update
        const unauthContext = testEnv.unauthenticatedContext();
        await assertFails(
            unauthContext.firestore().collection('users').doc(USER1_ID).update({
                displayName: 'Hacked'
            })
        );
    });
});

describe('User Profile Security Rules - Delete Permissions', () => {
    test('user cannot delete their own profile', async () => {
        // Requirement 3.5: Prevent profile deletion to maintain data integrity
        // Setup: Create user1's profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER1_ID,
                displayName: 'User 1',
                email: 'user1@example.com',
                online: false
            });
        });

        // Test: user1 cannot delete their own profile
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore().collection('users').doc(USER1_ID).delete()
        );
    });

    test('user cannot delete another user profile', async () => {
        // Setup: Create user2's profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER2_ID).set({
                userId: USER2_ID,
                displayName: 'User 2',
                email: 'user2@example.com',
                online: false
            });
        });

        // Test: user1 cannot delete user2's profile
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore().collection('users').doc(USER2_ID).delete()
        );
    });

    test('unauthenticated user cannot delete profile', async () => {
        // Setup: Create a profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                userId: USER1_ID,
                displayName: 'User 1',
                email: 'user1@example.com',
                online: false
            });
        });

        // Test: unauthenticated user cannot delete
        const unauthContext = testEnv.unauthenticatedContext();
        await assertFails(
            unauthContext.firestore().collection('users').doc(USER1_ID).delete()
        );
    });
});
