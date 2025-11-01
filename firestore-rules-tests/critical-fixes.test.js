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
const GROUP1_ID = 'group1';
const CHAT1_ID = 'chat1';
const MESSAGE1_ID = 'message1';
const NOTIFICATION1_ID = 'notification1';

beforeAll(async () => {
    // Initialize test environment with Firestore rules
    testEnv = await initializeTestEnvironment({
        projectId: 'test-project-critical-fixes',
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

describe('Critical Fix - Requirement 1.1: Groups Collection Write with ownerId Validation', () => {
    test('authenticated user can create group where ownerId matches their UID', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Test Group',
                description: 'A test group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isActive: true
            })
        );
    });

    test('authenticated user cannot create group with different ownerId', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Invalid Group',
                description: 'Should fail',
                owner: USER2_ID, // Different from authenticated user
                memberIds: [USER1_ID, USER2_ID],
                isActive: true
            })
        );
    });

    test('authenticated user must include themselves in memberIds when creating group', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Invalid Group',
                description: 'Should fail',
                owner: USER1_ID,
                memberIds: [USER2_ID, USER3_ID], // USER1_ID not included
                isActive: true
            })
        );
    });
});

describe('Critical Fix - Requirement 1.2: Groups Collection Read with memberIds Check', () => {
    test('authenticated user can read groups where memberIds contains their UID', async () => {
        // Setup: Create a group with user1 as member
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Test Group',
                description: 'A test group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isActive: true
            });
        });

        // Test: user1 can read the group
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).get()
        );
    });

    test('authenticated user can query groups using array-contains on memberIds', async () => {
        // Setup: Create multiple groups
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Group 1',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isActive: true
            });

            await context.firestore().collection('groups').doc('group2').set({
                name: 'Group 2',
                owner: USER2_ID,
                memberIds: [USER2_ID, USER3_ID],
                isActive: true
            });
        });

        // Test: user1 can query their groups
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const result = await assertSucceeds(
            user1Context.firestore()
                .collection('groups')
                .where('memberIds', 'array-contains', USER1_ID)
                .get()
        );

        expect(result.docs.length).toBe(1);
        expect(result.docs[0].data().name).toBe('Group 1');
    });

    test('authenticated user cannot read groups where memberIds does not contain their UID', async () => {
        // Setup: Create a group without user3
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Private Group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isActive: true
            });
        });

        // Test: user3 cannot read the group
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore().collection('groups').doc(GROUP1_ID).get()
        );
    });
});

describe('Critical Fix - Requirement 1.3 & 1.4: Chats Collection Participant Access', () => {
    test('authenticated user can read chats where participants contains their UID', async () => {
        // Setup: Create a chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: 'Hello',
                lastMessageTime: new Date()
            });
        });

        // Test: user1 can read the chat
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('chats').doc(CHAT1_ID).get()
        );
    });

    test('authenticated user can write to chats where participants contains their UID', async () => {
        // Setup: Create a chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: 'Hello',
                lastMessageTime: new Date()
            });
        });

        // Test: user1 can update the chat
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('chats').doc(CHAT1_ID).update({
                lastMessage: 'Updated message'
            })
        );
    });

    test('authenticated user cannot read chats where participants does not contain their UID', async () => {
        // Setup: Create a chat without user3
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: 'Private chat'
            });
        });

        // Test: user3 cannot read the chat
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore().collection('chats').doc(CHAT1_ID).get()
        );
    });

    test('authenticated user can query chats using array-contains on participants', async () => {
        // Setup: Create multiple chats
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: 'Chat 1'
            });

            await context.firestore().collection('chats').doc('chat2').set({
                type: 'DIRECT',
                participants: [USER2_ID, USER3_ID],
                lastMessage: 'Chat 2'
            });
        });

        // Test: user1 can query their chats
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const result = await assertSucceeds(
            user1Context.firestore()
                .collection('chats')
                .where('participants', 'array-contains', USER1_ID)
                .get()
        );

        expect(result.docs.length).toBe(1);
        expect(result.docs[0].data().lastMessage).toBe('Chat 1');
    });
});

describe('Critical Fix - Requirement 1.3: Messages Subcollection Participant Access', () => {
    test('chat participant can create messages in the subcollection', async () => {
        // Setup: Create a chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });
        });

        // Test: user1 can create a message
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc(MESSAGE1_ID)
                .set({
                    senderId: USER1_ID,
                    chatId: CHAT1_ID,
                    text: 'Hello',
                    timestamp: new Date(),
                    status: 'SENT'
                })
        );
    });

    test('chat participant can read messages in the subcollection', async () => {
        // Setup: Create a chat and message
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: 'Hello'
            });

            await context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc(MESSAGE1_ID)
                .set({
                    senderId: USER1_ID,
                    chatId: CHAT1_ID,
                    text: 'Hello',
                    timestamp: new Date(),
                    status: 'SENT'
                });
        });

        // Test: user2 can read the message
        const user2Context = testEnv.authenticatedContext(USER2_ID);
        await assertSucceeds(
            user2Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc(MESSAGE1_ID)
                .get()
        );
    });

    test('non-participant cannot create messages in the subcollection', async () => {
        // Setup: Create a chat without user3
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });
        });

        // Test: user3 cannot create a message
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc(MESSAGE1_ID)
                .set({
                    senderId: USER3_ID,
                    chatId: CHAT1_ID,
                    text: 'Unauthorized',
                    timestamp: new Date(),
                    status: 'SENT'
                })
        );
    });

    test('non-participant cannot read messages in the subcollection', async () => {
        // Setup: Create a chat and message without user3
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: 'Private'
            });

            await context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc(MESSAGE1_ID)
                .set({
                    senderId: USER1_ID,
                    chatId: CHAT1_ID,
                    text: 'Private message',
                    timestamp: new Date(),
                    status: 'SENT'
                });
        });

        // Test: user3 cannot read the message
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc(MESSAGE1_ID)
                .get()
        );
    });

    test('participant must set senderId to their own UID when creating message', async () => {
        // Setup: Create a chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });
        });

        // Test: user1 cannot create message with different senderId
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc(MESSAGE1_ID)
                .set({
                    senderId: USER2_ID, // Different from authenticated user
                    chatId: CHAT1_ID,
                    text: 'Spoofed message',
                    timestamp: new Date(),
                    status: 'SENT'
                })
        );
    });
});

describe('Critical Fix - Requirement 1.5: Notifications Collection User Access', () => {
    test('authenticated user can read their own notifications', async () => {
        // Setup: Create a notification for user1
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('notifications').doc(NOTIFICATION1_ID).set({
                userId: USER1_ID,
                type: 'message',
                message: 'You have a new message',
                timestamp: new Date(),
                read: false
            });
        });

        // Test: user1 can read their notification
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('notifications').doc(NOTIFICATION1_ID).get()
        );
    });

    test('authenticated user cannot read other users notifications', async () => {
        // Setup: Create a notification for user1
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('notifications').doc(NOTIFICATION1_ID).set({
                userId: USER1_ID,
                type: 'message',
                message: 'Private notification',
                timestamp: new Date(),
                read: false
            });
        });

        // Test: user2 cannot read user1's notification
        const user2Context = testEnv.authenticatedContext(USER2_ID);
        await assertFails(
            user2Context.firestore().collection('notifications').doc(NOTIFICATION1_ID).get()
        );
    });

    test('authenticated user can query their own notifications using userId filter', async () => {
        // Setup: Create notifications for different users
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('notifications').doc('notif1').set({
                userId: USER1_ID,
                type: 'message',
                message: 'Notification 1',
                timestamp: new Date(),
                read: false
            });

            await context.firestore().collection('notifications').doc('notif2').set({
                userId: USER2_ID,
                type: 'message',
                message: 'Notification 2',
                timestamp: new Date(),
                read: false
            });
        });

        // Test: user1 can query their notifications
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const result = await assertSucceeds(
            user1Context.firestore()
                .collection('notifications')
                .where('userId', '==', USER1_ID)
                .get()
        );

        expect(result.docs.length).toBe(1);
        expect(result.docs[0].data().message).toBe('Notification 1');
    });

    test('authenticated user can update their own notifications', async () => {
        // Setup: Create a notification for user1
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('notifications').doc(NOTIFICATION1_ID).set({
                userId: USER1_ID,
                type: 'message',
                message: 'Unread notification',
                timestamp: new Date(),
                read: false
            });
        });

        // Test: user1 can mark notification as read
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('notifications').doc(NOTIFICATION1_ID).update({
                read: true
            })
        );
    });

    test('authenticated user can create notifications', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('notifications').doc(NOTIFICATION1_ID).set({
                userId: USER2_ID,
                type: 'message',
                message: 'New notification',
                timestamp: new Date(),
                read: false
            })
        );
    });
});

describe('Critical Fix - Requirement 1.7: Access Denial for Non-Owned Data', () => {
    test('unauthenticated user cannot access any groups', async () => {
        // Setup: Create a group
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Test Group',
                owner: USER1_ID,
                memberIds: [USER1_ID],
                isActive: true
            });
        });

        // Test: unauthenticated user cannot read
        const unauthContext = testEnv.unauthenticatedContext();
        await assertFails(
            unauthContext.firestore().collection('groups').doc(GROUP1_ID).get()
        );
    });

    test('unauthenticated user cannot access any chats', async () => {
        // Setup: Create a chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: 'Hello'
            });
        });

        // Test: unauthenticated user cannot read
        const unauthContext = testEnv.unauthenticatedContext();
        await assertFails(
            unauthContext.firestore().collection('chats').doc(CHAT1_ID).get()
        );
    });

    test('unauthenticated user cannot access any notifications', async () => {
        // Setup: Create a notification
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('notifications').doc(NOTIFICATION1_ID).set({
                userId: USER1_ID,
                type: 'message',
                message: 'Test notification',
                timestamp: new Date(),
                read: false
            });
        });

        // Test: unauthenticated user cannot read
        const unauthContext = testEnv.unauthenticatedContext();
        await assertFails(
            unauthContext.firestore().collection('notifications').doc(NOTIFICATION1_ID).get()
        );
    });
});

describe('Critical Fix - Requirement 1.6: No PERMISSION_DENIED Errors for Valid Operations', () => {
    test('complete workflow: create group → create chat → send message → create notification', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        // Step 1: Create a group
        await assertSucceeds(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Workflow Test Group',
                description: 'Testing complete workflow',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isActive: true
            })
        );

        // Step 2: Create a chat
        await assertSucceeds(
            user1Context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: '',
                lastMessageTime: new Date()
            })
        );

        // Step 3: Send a message
        await assertSucceeds(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc(MESSAGE1_ID)
                .set({
                    senderId: USER1_ID,
                    chatId: CHAT1_ID,
                    text: 'Hello from workflow test',
                    timestamp: new Date(),
                    status: 'SENT'
                })
        );

        // Step 4: Create a notification
        await assertSucceeds(
            user1Context.firestore().collection('notifications').doc(NOTIFICATION1_ID).set({
                userId: USER2_ID,
                type: 'message',
                message: 'You have a new message',
                timestamp: new Date(),
                read: false
            })
        );

        // Verify: user2 can access all created resources
        const user2Context = testEnv.authenticatedContext(USER2_ID);

        await assertSucceeds(
            user2Context.firestore().collection('groups').doc(GROUP1_ID).get()
        );

        await assertSucceeds(
            user2Context.firestore().collection('chats').doc(CHAT1_ID).get()
        );

        await assertSucceeds(
            user2Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc(MESSAGE1_ID)
                .get()
        );

        await assertSucceeds(
            user2Context.firestore().collection('notifications').doc(NOTIFICATION1_ID).get()
        );
    });
});
