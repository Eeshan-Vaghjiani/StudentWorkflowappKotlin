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
const GROUP2_ID = 'group2';
const TASK1_ID = 'task1';
const CHAT1_ID = 'chat1';

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

describe('Groups Collection - Read Permissions', () => {
    test('user can read groups they are a member of', async () => {
        // Setup: Create a group with user1 as member
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Test Group',
                description: 'A test group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isActive: true,
                createdAt: new Date(),
                updatedAt: new Date()
            });
        });

        // Test: user1 can read the group
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).get()
        );
    });

    test('user can query groups using array-contains filter', async () => {
        // Setup: Create multiple groups
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Group 1',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isActive: true
            });

            await context.firestore().collection('groups').doc(GROUP2_ID).set({
                name: 'Group 2',
                owner: USER2_ID,
                memberIds: [USER2_ID, USER3_ID],
                isActive: true
            });
        });

        // Test: user1 can query their groups
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore()
                .collection('groups')
                .where('memberIds', 'array-contains', USER1_ID)
                .get()
        );
    });

    test('user cannot read groups they are not a member of', async () => {
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

    test('unauthenticated user cannot read groups', async () => {
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
});

describe('Groups Collection - Public Group Discovery', () => {
    test('any authenticated user can read public groups', async () => {
        // Setup: Create a public group
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Public Study Group',
                description: 'Open to all',
                owner: USER1_ID,
                memberIds: [USER1_ID],
                isPublic: true,
                isActive: true
            });
        });

        // Test: user3 (non-member) can read the public group
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertSucceeds(
            user3Context.firestore().collection('groups').doc(GROUP1_ID).get()
        );
    });

    test('non-member cannot read private groups', async () => {
        // Setup: Create a private group
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Private Group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isPublic: false,
                isActive: true
            });
        });

        // Test: user3 cannot read the private group
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore().collection('groups').doc(GROUP1_ID).get()
        );
    });

    test('user can query public groups with isPublic filter', async () => {
        // Setup: Create public and private groups
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Public Group',
                owner: USER1_ID,
                memberIds: [USER1_ID],
                isPublic: true,
                isActive: true
            });

            await context.firestore().collection('groups').doc(GROUP2_ID).set({
                name: 'Private Group',
                owner: USER2_ID,
                memberIds: [USER2_ID],
                isPublic: false,
                isActive: true
            });
        });

        // Test: user3 can query public groups
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        const result = await assertSucceeds(
            user3Context.firestore()
                .collection('groups')
                .where('isPublic', '==', true)
                .get()
        );

        expect(result.docs.length).toBe(1);
        expect(result.docs[0].data().name).toBe('Public Group');
    });

    test('member can read private group even when isPublic is false', async () => {
        // Setup: Create a private group with user2 as member
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Private Group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isPublic: false,
                isActive: true
            });
        });

        // Test: user2 can read the private group (they are a member)
        const user2Context = testEnv.authenticatedContext(USER2_ID);
        await assertSucceeds(
            user2Context.firestore().collection('groups').doc(GROUP1_ID).get()
        );
    });
});

describe('Groups Collection - Member Limit Validation', () => {
    test('user can create group with 1 member (minimum)', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Solo Group',
                owner: USER1_ID,
                memberIds: [USER1_ID],
                isActive: true
            })
        );
    });

    test('user can create group with 100 members (maximum)', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const memberIds = Array.from({ length: 100 }, (_, i) => `user${i + 1}`);

        await assertSucceeds(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Large Group',
                owner: USER1_ID,
                memberIds: memberIds,
                isActive: true
            })
        );
    });

    test('user cannot create group with 0 members', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Empty Group',
                owner: USER1_ID,
                memberIds: [],
                isActive: true
            })
        );
    });

    test('user cannot create group with 101 members (exceeds maximum)', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const memberIds = Array.from({ length: 101 }, (_, i) => `user${i + 1}`);

        await assertFails(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Too Large Group',
                owner: USER1_ID,
                memberIds: memberIds,
                isActive: true
            })
        );
    });

    test('user cannot update group to exceed 100 members', async () => {
        // Setup: Create a group with 50 members
        await testEnv.withSecurityRulesDisabled(async (context) => {
            const memberIds = Array.from({ length: 50 }, (_, i) => `user${i + 1}`);
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Medium Group',
                owner: USER1_ID,
                memberIds: memberIds,
                isActive: true
            });
        });

        // Test: Cannot update to 101 members
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const newMemberIds = Array.from({ length: 101 }, (_, i) => `user${i + 1}`);

        await assertFails(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).update({
                memberIds: newMemberIds
            })
        );
    });

    test('owner must remain in memberIds when updating', async () => {
        // Setup: Create a group
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Test Group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isActive: true
            });
        });

        // Test: Cannot remove owner from memberIds
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).update({
                memberIds: [USER2_ID, USER3_ID] // USER1_ID (owner) not in list
            })
        );
    });
});

describe('Groups Collection - Create Permissions', () => {
    test('user can create group with themselves as member and owner', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'New Group',
                description: 'A new group',
                owner: USER1_ID,
                memberIds: [USER1_ID],
                isActive: true,
                createdAt: new Date(),
                updatedAt: new Date()
            })
        );
    });

    test('user can create group with themselves and others as members', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Multi-member Group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID, USER3_ID],
                isActive: true
            })
        );
    });

    test('user cannot create group without themselves as member', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Invalid Group',
                owner: USER1_ID,
                memberIds: [USER2_ID, USER3_ID], // user1 not in memberIds
                isActive: true
            })
        );
    });

    test('user cannot create group with different owner', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Invalid Group',
                owner: USER2_ID, // Different owner
                memberIds: [USER1_ID, USER2_ID],
                isActive: true
            })
        );
    });
});

describe('Tasks Collection - Group Membership Validation', () => {
    test('group member can read group tasks', async () => {
        // Setup: Create group and group task
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Test Group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isActive: true
            });

            await context.firestore().collection('tasks').doc(TASK1_ID).set({
                title: 'Group Task',
                userId: USER1_ID,
                groupId: GROUP1_ID,
                assignedTo: [USER1_ID],
                status: 'pending'
            });
        });

        // Test: user2 (group member) can read the group task
        const user2Context = testEnv.authenticatedContext(USER2_ID);
        await assertSucceeds(
            user2Context.firestore().collection('tasks').doc(TASK1_ID).get()
        );
    });

    test('non-member cannot read group tasks', async () => {
        // Setup: Create group and group task
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Test Group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isActive: true
            });

            await context.firestore().collection('tasks').doc(TASK1_ID).set({
                title: 'Group Task',
                userId: USER1_ID,
                groupId: GROUP1_ID,
                assignedTo: [USER1_ID],
                status: 'pending'
            });
        });

        // Test: user3 (non-member) cannot read the group task
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore().collection('tasks').doc(TASK1_ID).get()
        );
    });

    test('user can create group task if they are a group member', async () => {
        // Setup: Create group
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Test Group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isActive: true
            });
        });

        // Test: user1 can create a group task
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('tasks').doc(TASK1_ID).set({
                title: 'New Group Task',
                userId: USER1_ID,
                groupId: GROUP1_ID,
                assignedTo: [USER1_ID],
                status: 'pending'
            })
        );
    });

    test('non-member cannot create group task', async () => {
        // Setup: Create group without user3
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Test Group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isActive: true
            });
        });

        // Test: user3 cannot create a group task
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore().collection('tasks').doc(TASK1_ID).set({
                title: 'Unauthorized Group Task',
                userId: USER3_ID,
                groupId: GROUP1_ID,
                assignedTo: [USER3_ID],
                status: 'pending'
            })
        );
    });
});

describe('Tasks Collection - Assignment Limit Validation', () => {
    test('user can create task with 1 assignee (minimum)', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('tasks').doc(TASK1_ID).set({
                title: 'Solo Task',
                userId: USER1_ID,
                assignedTo: [USER1_ID],
                status: 'pending'
            })
        );
    });

    test('user can create task with 50 assignees (maximum)', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const assignedTo = Array.from({ length: 50 }, (_, i) => `user${i + 1}`);

        await assertSucceeds(
            user1Context.firestore().collection('tasks').doc(TASK1_ID).set({
                title: 'Large Assignment Task',
                userId: USER1_ID,
                assignedTo: assignedTo,
                status: 'pending'
            })
        );
    });

    test('user cannot create task with 0 assignees', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('tasks').doc(TASK1_ID).set({
                title: 'No Assignee Task',
                userId: USER1_ID,
                assignedTo: [],
                status: 'pending'
            })
        );
    });

    test('user cannot create task with 51 assignees (exceeds maximum)', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const assignedTo = Array.from({ length: 51 }, (_, i) => `user${i + 1}`);

        await assertFails(
            user1Context.firestore().collection('tasks').doc(TASK1_ID).set({
                title: 'Too Many Assignees Task',
                userId: USER1_ID,
                assignedTo: assignedTo,
                status: 'pending'
            })
        );
    });

    test('user cannot update task to exceed 50 assignees', async () => {
        // Setup: Create a task with 25 assignees
        await testEnv.withSecurityRulesDisabled(async (context) => {
            const assignedTo = Array.from({ length: 25 }, (_, i) => `user${i + 1}`);
            await context.firestore().collection('tasks').doc(TASK1_ID).set({
                title: 'Medium Task',
                userId: USER1_ID,
                assignedTo: assignedTo,
                status: 'pending'
            });
        });

        // Test: Cannot update to 51 assignees
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const newAssignedTo = Array.from({ length: 51 }, (_, i) => `user${i + 1}`);

        await assertFails(
            user1Context.firestore().collection('tasks').doc(TASK1_ID).update({
                assignedTo: newAssignedTo
            })
        );
    });

    test('user cannot update task to have 0 assignees', async () => {
        // Setup: Create a task
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('tasks').doc(TASK1_ID).set({
                title: 'Test Task',
                userId: USER1_ID,
                assignedTo: [USER1_ID, USER2_ID],
                status: 'pending'
            });
        });

        // Test: Cannot remove all assignees
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore().collection('tasks').doc(TASK1_ID).update({
                assignedTo: []
            })
        );
    });
});

describe('Tasks Collection - Read Permissions', () => {
    test('user can read tasks they created', async () => {
        // Setup: Create a task
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('tasks').doc(TASK1_ID).set({
                title: 'Test Task',
                description: 'A test task',
                userId: USER1_ID,
                assignedTo: [],
                status: 'pending',
                createdAt: new Date()
            });
        });

        // Test: user1 can read their task
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('tasks').doc(TASK1_ID).get()
        );
    });

    test('user can read tasks they are assigned to', async () => {
        // Setup: Create a task assigned to user2
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('tasks').doc(TASK1_ID).set({
                title: 'Assigned Task',
                userId: USER1_ID,
                assignedTo: [USER2_ID, USER3_ID],
                status: 'pending'
            });
        });

        // Test: user2 can read the task
        const user2Context = testEnv.authenticatedContext(USER2_ID);
        await assertSucceeds(
            user2Context.firestore().collection('tasks').doc(TASK1_ID).get()
        );
    });

    test('user can query tasks using userId filter', async () => {
        // Setup: Create tasks
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('tasks').doc('task1').set({
                title: 'Task 1',
                userId: USER1_ID,
                assignedTo: [],
                status: 'pending'
            });

            await context.firestore().collection('tasks').doc('task2').set({
                title: 'Task 2',
                userId: USER2_ID,
                assignedTo: [],
                status: 'pending'
            });
        });

        // Test: user1 can query their tasks
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore()
                .collection('tasks')
                .where('userId', '==', USER1_ID)
                .get()
        );
    });

    test('user can query tasks using assignedTo filter', async () => {
        // Setup: Create tasks
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('tasks').doc('task1').set({
                title: 'Assigned Task',
                userId: USER1_ID,
                assignedTo: [USER2_ID],
                status: 'pending'
            });
        });

        // Test: user2 can query assigned tasks
        const user2Context = testEnv.authenticatedContext(USER2_ID);
        await assertSucceeds(
            user2Context.firestore()
                .collection('tasks')
                .where('assignedTo', 'array-contains', USER2_ID)
                .get()
        );
    });

    test('user cannot read tasks they did not create or are not assigned to', async () => {
        // Setup: Create a task for user1
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('tasks').doc(TASK1_ID).set({
                title: 'Private Task',
                userId: USER1_ID,
                assignedTo: [],
                status: 'pending'
            });
        });

        // Test: user3 cannot read the task
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore().collection('tasks').doc(TASK1_ID).get()
        );
    });
});

describe('Chats Collection - Read Permissions', () => {
    test('user can read chats they are a participant in', async () => {
        // Setup: Create a chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: 'Hello',
                lastMessageTime: new Date(),
                createdAt: new Date()
            });
        });

        // Test: user1 can read the chat
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('chats').doc(CHAT1_ID).get()
        );
    });

    test('user can query chats using participants filter', async () => {
        // Setup: Create chats
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc('chat1').set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: 'Hello'
            });

            await context.firestore().collection('chats').doc('chat2').set({
                type: 'DIRECT',
                participants: [USER2_ID, USER3_ID],
                lastMessage: 'Hi'
            });
        });

        // Test: user1 can query their chats
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore()
                .collection('chats')
                .where('participants', 'array-contains', USER1_ID)
                .get()
        );
    });

    test('user cannot read chats they are not a participant in', async () => {
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
});

describe('Chats Collection - Create Permissions with Participant Limits', () => {
    test('user can create DIRECT chat with exactly 2 participants', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: '',
                createdAt: new Date()
            })
        );
    });

    test('user cannot create DIRECT chat with 1 participant', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID],
                lastMessage: '',
                createdAt: new Date()
            })
        );
    });

    test('user cannot create DIRECT chat with 3 participants', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID, USER3_ID],
                lastMessage: '',
                createdAt: new Date()
            })
        );
    });

    test('user can create GROUP chat with 2 participants', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'GROUP',
                participants: [USER1_ID, USER2_ID],
                lastMessage: '',
                createdAt: new Date()
            })
        );
    });

    test('user can create GROUP chat with 3 participants', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'GROUP',
                participants: [USER1_ID, USER2_ID, USER3_ID],
                lastMessage: '',
                createdAt: new Date()
            })
        );
    });

    test('user cannot create GROUP chat with 1 participant', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'GROUP',
                participants: [USER1_ID],
                lastMessage: '',
                createdAt: new Date()
            })
        );
    });

    test('user cannot create chat without themselves as participant', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER2_ID, USER3_ID],
                lastMessage: '',
                createdAt: new Date()
            })
        );
    });
});

describe('Chats Collection - Update Permissions', () => {
    test('participant can update chat metadata', async () => {
        // Setup: Create a chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: 'Hello',
                lastMessageTime: new Date()
            });
        });

        // Test: user1 can update lastMessage
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('chats').doc(CHAT1_ID).update({
                lastMessage: 'Updated message',
                lastMessageTime: new Date()
            })
        );
    });

    test('participant cannot modify participants array', async () => {
        // Setup: Create a chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'GROUP',
                participants: [USER1_ID, USER2_ID],
                lastMessage: 'Hello'
            });
        });

        // Test: user1 cannot add user3 to participants
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore().collection('chats').doc(CHAT1_ID).update({
                participants: [USER1_ID, USER2_ID, USER3_ID]
            })
        );
    });

    test('participant cannot remove themselves from participants', async () => {
        // Setup: Create a chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'GROUP',
                participants: [USER1_ID, USER2_ID, USER3_ID],
                lastMessage: 'Hello'
            });
        });

        // Test: user1 cannot remove themselves
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore().collection('chats').doc(CHAT1_ID).update({
                participants: [USER2_ID, USER3_ID]
            })
        );
    });

    test('non-participant cannot update chat', async () => {
        // Setup: Create a chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: 'Hello'
            });
        });

        // Test: user3 cannot update the chat
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore().collection('chats').doc(CHAT1_ID).update({
                lastMessage: 'Hacked'
            })
        );
    });
});

describe('Messages Subcollection - Access Control', () => {
    test('chat participant can read messages', async () => {
        // Setup: Create chat and message
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: 'Hello'
            });

            await context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').set({
                    senderId: USER1_ID,
                    text: 'Hello there',
                    timestamp: Date.now()
                });
        });

        // Test: user2 can read the message
        const user2Context = testEnv.authenticatedContext(USER2_ID);
        await assertSucceeds(
            user2Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').get()
        );
    });

    test('non-participant cannot read messages', async () => {
        // Setup: Create chat and message
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: 'Hello'
            });

            await context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').set({
                    senderId: USER1_ID,
                    text: 'Private message',
                    timestamp: Date.now()
                });
        });

        // Test: user3 cannot read the message
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').get()
        );
    });

    test('participant can create message with valid text', async () => {
        // Setup: Create chat
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
                .collection('messages').doc('msg1').set({
                    senderId: USER1_ID,
                    text: 'Hello!',
                    timestamp: Date.now()
                })
        );
    });

    test('participant can create message with valid imageUrl', async () => {
        // Setup: Create chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });
        });

        // Test: user1 can create a message with Firebase Storage URL
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').set({
                    senderId: USER1_ID,
                    imageUrl: 'https://firebasestorage.googleapis.com/v0/b/bucket/o/image.jpg',
                    timestamp: Date.now()
                })
        );
    });

    test('non-participant cannot create message', async () => {
        // Setup: Create chat
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
                .collection('messages').doc('msg1').set({
                    senderId: USER3_ID,
                    text: 'Unauthorized message',
                    timestamp: Date.now()
                })
        );
    });
});

describe('Messages Subcollection - Content Validation', () => {
    test('cannot create message without text, imageUrl, or documentUrl', async () => {
        // Setup: Create chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });
        });

        // Test: Cannot create empty message
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').set({
                    senderId: USER1_ID,
                    timestamp: Date.now()
                })
        );
    });

    test('cannot create message with text exceeding 10000 characters', async () => {
        // Setup: Create chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });
        });

        // Test: Cannot create message with text > 10000 chars
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const longText = 'a'.repeat(10001);

        await assertFails(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').set({
                    senderId: USER1_ID,
                    text: longText,
                    timestamp: Date.now()
                })
        );
    });

    test('can create message with text at 10000 characters (boundary)', async () => {
        // Setup: Create chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });
        });

        // Test: Can create message with exactly 10000 chars
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const maxText = 'a'.repeat(10000);

        await assertSucceeds(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').set({
                    senderId: USER1_ID,
                    text: maxText,
                    timestamp: Date.now()
                })
        );
    });

    test('cannot create message with external imageUrl', async () => {
        // Setup: Create chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });
        });

        // Test: Cannot use external URL
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').set({
                    senderId: USER1_ID,
                    imageUrl: 'https://example.com/image.jpg',
                    timestamp: Date.now()
                })
        );
    });

    test('cannot create message with external documentUrl', async () => {
        // Setup: Create chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });
        });

        // Test: Cannot use external URL
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').set({
                    senderId: USER1_ID,
                    documentUrl: 'https://malicious-site.com/doc.pdf',
                    timestamp: Date.now()
                })
        );
    });

    test('cannot create message with mismatched senderId', async () => {
        // Setup: Create chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });
        });

        // Test: user1 cannot create message with user2's senderId
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').set({
                    senderId: USER2_ID,
                    text: 'Spoofed message',
                    timestamp: Date.now()
                })
        );
    });

    test('sender can update their own message', async () => {
        // Setup: Create chat and message
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });

            await context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').set({
                    senderId: USER1_ID,
                    text: 'Original message',
                    timestamp: Date.now()
                });
        });

        // Test: user1 can update their message
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').update({
                    text: 'Edited message'
                })
        );
    });

    test('sender can delete their own message', async () => {
        // Setup: Create chat and message
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });

            await context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').set({
                    senderId: USER1_ID,
                    text: 'Message to delete',
                    timestamp: Date.now()
                });
        });

        // Test: user1 can delete their message
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').delete()
        );
    });

    test('other participant cannot delete message they did not send', async () => {
        // Setup: Create chat and message
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });

            await context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').set({
                    senderId: USER1_ID,
                    text: 'User1 message',
                    timestamp: Date.now()
                });
        });

        // Test: user2 cannot delete user1's message
        const user2Context = testEnv.authenticatedContext(USER2_ID);
        await assertFails(
            user2Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').delete()
        );
    });
});

describe('Typing Status Subcollection - Access Control', () => {
    test('participant can read typing status', async () => {
        // Setup: Create chat and typing status
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });

            await context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('typing_status').doc(USER1_ID).set({
                    isTyping: true,
                    timestamp: Date.now()
                });
        });

        // Test: user2 can read user1's typing status
        const user2Context = testEnv.authenticatedContext(USER2_ID);
        await assertSucceeds(
            user2Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('typing_status').doc(USER1_ID).get()
        );
    });

    test('non-participant cannot read typing status', async () => {
        // Setup: Create chat and typing status
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });

            await context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('typing_status').doc(USER1_ID).set({
                    isTyping: true,
                    timestamp: Date.now()
                });
        });

        // Test: user3 cannot read typing status
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('typing_status').doc(USER1_ID).get()
        );
    });

    test('participant can write their own typing status', async () => {
        // Setup: Create chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });
        });

        // Test: user1 can write their typing status
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('typing_status').doc(USER1_ID).set({
                    isTyping: true,
                    timestamp: Date.now()
                })
        );
    });

    test('participant cannot write another users typing status', async () => {
        // Setup: Create chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });
        });

        // Test: user1 cannot write user2's typing status
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('typing_status').doc(USER2_ID).set({
                    isTyping: true,
                    timestamp: Date.now()
                })
        );
    });

    test('non-participant cannot write typing status', async () => {
        // Setup: Create chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });
        });

        // Test: user3 cannot write typing status
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('typing_status').doc(USER3_ID).set({
                    isTyping: true,
                    timestamp: Date.now()
                })
        );
    });
});

describe('Permission Denied Scenarios', () => {
    test('querying groups without filter returns empty results for non-members', async () => {
        // Setup: Create groups
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Group 1',
                owner: USER1_ID,
                memberIds: [USER1_ID],
                isActive: true
            });
        });

        // Test: user2 queries with their filter - should succeed but return empty
        const user2Context = testEnv.authenticatedContext(USER2_ID);
        const result = await assertSucceeds(
            user2Context.firestore()
                .collection('groups')
                .where('memberIds', 'array-contains', USER2_ID)
                .get()
        );

        expect(result.docs.length).toBe(0);
    });

    test('querying tasks without filter returns empty results for non-owners', async () => {
        // Setup: Create tasks
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('tasks').doc('task1').set({
                title: 'Task 1',
                userId: USER1_ID,
                assignedTo: [],
                status: 'pending'
            });
        });

        // Test: user2 queries with their filter - should succeed but return empty
        const user2Context = testEnv.authenticatedContext(USER2_ID);
        const result = await assertSucceeds(
            user2Context.firestore()
                .collection('tasks')
                .where('userId', '==', USER2_ID)
                .get()
        );

        expect(result.docs.length).toBe(0);
    });

    test('querying chats without filter returns empty results for non-participants', async () => {
        // Setup: Create chats
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc('chat1').set({
                participants: [USER1_ID, USER2_ID],
                lastMessage: 'Hello'
            });
        });

        // Test: user3 queries with their filter - should succeed but return empty
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        const result = await assertSucceeds(
            user3Context.firestore()
                .collection('chats')
                .where('participants', 'array-contains', USER3_ID)
                .get()
        );

        expect(result.docs.length).toBe(0);
    });
});

describe('Group Activities - Read Permissions', () => {
    test('user can read activities from groups they are members of (denormalized memberIds)', async () => {
        // Setup: Create activity with memberIds denormalized
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('group_activities').doc('activity1').set({
                groupId: GROUP1_ID,
                userId: USER1_ID,
                type: 'task_created',
                description: 'Created a task',
                memberIds: [USER1_ID, USER2_ID],
                createdAt: new Date()
            });
        });

        // Test: user1 can read the activity
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('group_activities').doc('activity1').get()
        );
    });

    test('user can read activities using isGroupMember fallback (no memberIds)', async () => {
        // Setup: Create group and activity without denormalized memberIds
        await testEnv.withSecurityRulesDisabled(async (context) => {
            // Create the group first
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Test Group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isActive: true
            });

            // Create activity without memberIds (tests fallback to isGroupMember)
            await context.firestore().collection('group_activities').doc('activity1').set({
                groupId: GROUP1_ID,
                userId: USER1_ID,
                type: 'task_created',
                description: 'Created a task',
                createdAt: new Date()
            });
        });

        // Test: user1 can read the activity via isGroupMember fallback
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('group_activities').doc('activity1').get()
        );
    });

    test('user cannot read activities from groups they are not members of (denormalized)', async () => {
        // Setup: Create activity
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('group_activities').doc('activity1').set({
                groupId: GROUP1_ID,
                userId: USER1_ID,
                type: 'task_created',
                description: 'Created a task',
                memberIds: [USER1_ID, USER2_ID],
                createdAt: new Date()
            });
        });

        // Test: user3 cannot read the activity
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore().collection('group_activities').doc('activity1').get()
        );
    });

    test('user cannot read activities from groups they are not members of (fallback)', async () => {
        // Setup: Create group and activity
        await testEnv.withSecurityRulesDisabled(async (context) => {
            // Create the group
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Test Group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isActive: true
            });

            // Create activity without memberIds
            await context.firestore().collection('group_activities').doc('activity1').set({
                groupId: GROUP1_ID,
                userId: USER1_ID,
                type: 'task_created',
                description: 'Created a task',
                createdAt: new Date()
            });
        });

        // Test: user3 cannot read the activity (not a group member)
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore().collection('group_activities').doc('activity1').get()
        );
    });
});

describe('Group Activities - Create Permissions', () => {
    test('group member can create activity with memberIds', async () => {
        // Setup: Create group
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Test Group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isActive: true
            });
        });

        // Test: user1 can create activity
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('group_activities').doc('activity1').set({
                groupId: GROUP1_ID,
                userId: USER1_ID,
                type: 'task_created',
                description: 'Created a task',
                memberIds: [USER1_ID, USER2_ID],
                createdAt: new Date()
            })
        );
    });

    test('non-member cannot create activity', async () => {
        // Setup: Create group without user3
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Test Group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isActive: true
            });
        });

        // Test: user3 cannot create activity
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore().collection('group_activities').doc('activity1').set({
                groupId: GROUP1_ID,
                userId: USER3_ID,
                type: 'task_created',
                description: 'Created a task',
                memberIds: [USER1_ID, USER2_ID],
                createdAt: new Date()
            })
        );
    });

    test('creator must be in memberIds array', async () => {
        // Setup: Create group
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Test Group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isActive: true
            });
        });

        // Test: user1 cannot create activity without themselves in memberIds
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore().collection('group_activities').doc('activity1').set({
                groupId: GROUP1_ID,
                userId: USER1_ID,
                type: 'task_created',
                description: 'Created a task',
                memberIds: [USER2_ID], // user1 not in memberIds
                createdAt: new Date()
            })
        );
    });
});

describe('Notifications Collection - Enhanced Rules', () => {
    test('user can read their own notifications', async () => {
        // Setup: Create a notification for user1
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('notifications').doc('notif1').set({
                userId: USER1_ID,
                type: 'task_assigned',
                message: 'You have been assigned a task',
                read: false,
                createdAt: new Date()
            });
        });

        // Test: user1 can read their notification
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('notifications').doc('notif1').get()
        );
    });

    test('user cannot read other users notifications', async () => {
        // Setup: Create a notification for user1
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('notifications').doc('notif1').set({
                userId: USER1_ID,
                type: 'task_assigned',
                message: 'You have been assigned a task',
                read: false,
                createdAt: new Date()
            });
        });

        // Test: user2 cannot read user1's notification
        const user2Context = testEnv.authenticatedContext(USER2_ID);
        await assertFails(
            user2Context.firestore().collection('notifications').doc('notif1').get()
        );
    });

    test('user cannot create notifications (client-side blocked)', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        // Test: user1 cannot create a notification
        await assertFails(
            user1Context.firestore().collection('notifications').doc('notif1').set({
                userId: USER1_ID,
                type: 'task_assigned',
                message: 'Trying to create notification',
                read: false,
                createdAt: new Date()
            })
        );
    });

    test('user can update only the read field of their notification', async () => {
        // Setup: Create a notification for user1
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('notifications').doc('notif1').set({
                userId: USER1_ID,
                type: 'task_assigned',
                message: 'You have been assigned a task',
                read: false,
                createdAt: new Date()
            });
        });

        // Test: user1 can mark notification as read
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('notifications').doc('notif1').update({
                read: true
            })
        );
    });

    test('user cannot update other fields of their notification', async () => {
        // Setup: Create a notification for user1
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('notifications').doc('notif1').set({
                userId: USER1_ID,
                type: 'task_assigned',
                message: 'You have been assigned a task',
                read: false,
                createdAt: new Date()
            });
        });

        // Test: user1 cannot modify the message field
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore().collection('notifications').doc('notif1').update({
                message: 'Modified message'
            })
        );
    });

    test('user cannot update multiple fields including read', async () => {
        // Setup: Create a notification for user1
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('notifications').doc('notif1').set({
                userId: USER1_ID,
                type: 'task_assigned',
                message: 'You have been assigned a task',
                read: false,
                createdAt: new Date()
            });
        });

        // Test: user1 cannot update read and message together
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore().collection('notifications').doc('notif1').update({
                read: true,
                message: 'Modified message'
            })
        );
    });

    test('user can delete their own notifications', async () => {
        // Setup: Create a notification for user1
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('notifications').doc('notif1').set({
                userId: USER1_ID,
                type: 'task_assigned',
                message: 'You have been assigned a task',
                read: false,
                createdAt: new Date()
            });
        });

        // Test: user1 can delete their notification
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('notifications').doc('notif1').delete()
        );
    });

    test('user cannot delete other users notifications', async () => {
        // Setup: Create a notification for user1
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('notifications').doc('notif1').set({
                userId: USER1_ID,
                type: 'task_assigned',
                message: 'You have been assigned a task',
                read: false,
                createdAt: new Date()
            });
        });

        // Test: user2 cannot delete user1's notification
        const user2Context = testEnv.authenticatedContext(USER2_ID);
        await assertFails(
            user2Context.firestore().collection('notifications').doc('notif1').delete()
        );
    });
});

describe('Authentication Rules - Basic Access Control', () => {
    test('unauthenticated user cannot read users collection', async () => {
        // Setup: Create a user
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                name: 'User One',
                email: 'user1@test.com'
            });
        });

        // Test: unauthenticated user cannot read
        const unauthContext = testEnv.unauthenticatedContext();
        await assertFails(
            unauthContext.firestore().collection('users').doc(USER1_ID).get()
        );
    });

    test('authenticated user can read their own user document', async () => {
        // Setup: Create a user
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                name: 'User One',
                email: 'user1@test.com'
            });
        });

        // Test: user1 can read their own document
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).get()
        );
    });

    test('authenticated user can read other users public data', async () => {
        // Setup: Create a user
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER2_ID).set({
                name: 'User Two',
                email: 'user2@test.com'
            });
        });

        // Test: user1 can read user2's document (public profile data)
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER2_ID).get()
        );
    });

    test('authenticated user cannot create document for another user', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        // Test: user1 cannot create a document for user2
        await assertFails(
            user1Context.firestore().collection('users').doc(USER2_ID).set({
                name: 'Fake User Two',
                email: 'fake@test.com'
            })
        );
    });

    test('authenticated user cannot update another users document', async () => {
        // Setup: Create a user
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER2_ID).set({
                name: 'User Two',
                email: 'user2@test.com'
            });
        });

        // Test: user1 cannot update user2's document
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.firestore().collection('users').doc(USER2_ID).update({
                name: 'Hacked Name'
            })
        );
    });
});

describe('Users Collection - Profile Picture URL Validation', () => {
    test('user can create profile with valid Firebase Storage URL', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                name: 'User One',
                email: 'user1@test.com',
                profileImageUrl: 'https://firebasestorage.googleapis.com/v0/b/bucket/o/image.jpg'
            })
        );
    });

    test('user can create profile with empty profileImageUrl', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                name: 'User One',
                email: 'user1@test.com',
                profileImageUrl: ''
            })
        );
    });

    test('user can create profile without profileImageUrl field', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                name: 'User One',
                email: 'user1@test.com'
            })
        );
    });

    test('user cannot create profile with external URL', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                name: 'User One',
                email: 'user1@test.com',
                profileImageUrl: 'https://example.com/image.jpg'
            })
        );
    });

    test('user can update profile with valid Firebase Storage URL', async () => {
        // Setup: Create user profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                name: 'User One',
                email: 'user1@test.com',
                profileImageUrl: ''
            });
        });

        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).update({
                profileImageUrl: 'https://firebasestorage.googleapis.com/v0/b/bucket/o/newimage.jpg'
            })
        );
    });

    test('user cannot update profile with external URL', async () => {
        // Setup: Create user profile
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('users').doc(USER1_ID).set({
                name: 'User One',
                email: 'user1@test.com',
                profileImageUrl: ''
            });
        });

        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertFails(
            user1Context.firestore().collection('users').doc(USER1_ID).update({
                profileImageUrl: 'https://malicious-site.com/tracker.jpg'
            })
        );
    });
});
