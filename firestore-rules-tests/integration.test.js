const {
    assertFails,
    assertSucceeds,
    initializeTestEnvironment
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
const TASK2_ID = 'task2';
const CHAT1_ID = 'chat1';
const CHAT2_ID = 'chat2';

beforeAll(async () => {
    // Initialize test environment with Firestore and Storage rules
    testEnv = await initializeTestEnvironment({
        projectId: 'test-project-integration',
        firestore: {
            rules: fs.readFileSync(path.resolve(__dirname, '../firestore.rules'), 'utf8'),
            host: 'localhost',
            port: 8080
        },
        storage: {
            rules: fs.readFileSync(path.resolve(__dirname, '../storage.rules'), 'utf8'),
            host: 'localhost',
            port: 9199
        }
    });
});

afterAll(async () => {
    await testEnv.cleanup();
});

beforeEach(async () => {
    await testEnv.clearFirestore();
    await testEnv.clearStorage();
});

describe('Integration Test 14.1 - Group Workflow', () => {
    test('Complete workflow: Create group → Create group task → Verify member access', async () => {
        // Step 1: User1 creates a group with user2 as member
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Study Group',
                description: 'Math study group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isPublic: false,
                isActive: true,
                createdAt: new Date()
            })
        );

        // Step 2: User1 creates a group task
        await assertSucceeds(
            user1Context.firestore().collection('tasks').doc(TASK1_ID).set({
                title: 'Complete homework',
                description: 'Math homework chapter 5',
                userId: USER1_ID,
                groupId: GROUP1_ID,
                assignedTo: [USER1_ID, USER2_ID],
                status: 'pending',
                priority: 'high',
                createdAt: new Date()
            })
        );

        // Step 3: Verify user2 (member) can access the group
        const user2Context = testEnv.authenticatedContext(USER2_ID);
        const groupDoc = await assertSucceeds(
            user2Context.firestore().collection('groups').doc(GROUP1_ID).get()
        );
        expect(groupDoc.exists).toBe(true);
        expect(groupDoc.data().name).toBe('Study Group');

        // Step 4: Verify user2 (member) can access the group task
        const taskDoc = await assertSucceeds(
            user2Context.firestore().collection('tasks').doc(TASK1_ID).get()
        );
        expect(taskDoc.exists).toBe(true);
        expect(taskDoc.data().title).toBe('Complete homework');

        // Step 5: Verify user3 (non-member) cannot access the private group
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore().collection('groups').doc(GROUP1_ID).get()
        );

        // Step 6: Verify user3 (non-member) cannot access the group task

        await assertFails(
            user3Context.firestore().collection('tasks').doc(TASK1_ID).get()
        );
    });

    test('Complete workflow: Create public group → Verify non-member can discover', async () => {
        // Step 1: User1 creates a public group
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Public Study Group',
                description: 'Open to all students',
                owner: USER1_ID,
                memberIds: [USER1_ID],
                isPublic: true,
                isActive: true,
                subject: 'Mathematics',
                createdAt: new Date()
            })
        );

        // Step 2: User3 (non-member) can discover the public group
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        const groupDoc = await assertSucceeds(
            user3Context.firestore().collection('groups').doc(GROUP1_ID).get()
        );
        expect(groupDoc.exists).toBe(true);
        expect(groupDoc.data().name).toBe('Public Study Group');
        expect(groupDoc.data().isPublic).toBe(true);

        // Step 3: User3 can query for public groups
        const publicGroupsQuery = await assertSucceeds(
            user3Context.firestore()
                .collection('groups')
                .where('isPublic', '==', true)
                .get()
        );
        expect(publicGroupsQuery.docs.length).toBe(1);
        expect(publicGroupsQuery.docs[0].data().name).toBe('Public Study Group');

        // Step 4: Create a private group for comparison
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP2_ID).set({
                name: 'Private Group',
                owner: USER2_ID,
                memberIds: [USER2_ID],
                isPublic: false,
                isActive: true
            });
        });

        // Step 5: Verify user3 cannot access the private group

        await assertFails(
            user3Context.firestore().collection('groups').doc(GROUP2_ID).get()
        );

        // Step 6: Verify public groups query doesn't return private groups
        const publicOnlyQuery = await assertSucceeds(
            user3Context.firestore()
                .collection('groups')
                .where('isPublic', '==', true)
                .get()
        );
        expect(publicOnlyQuery.docs.length).toBe(1);
        expect(publicOnlyQuery.docs[0].id).toBe(GROUP1_ID);
    });

    test('Complete workflow: Add user to group → Verify they can access group data', async () => {
        // Step 1: User1 creates a group without user3
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('groups').doc(GROUP1_ID).set({
                name: 'Exclusive Group',
                owner: USER1_ID,
                memberIds: [USER1_ID, USER2_ID],
                isPublic: false,
                isActive: true
            });

            // Create a group task
            await context.firestore().collection('tasks').doc(TASK1_ID).set({
                title: 'Group Project',
                userId: USER1_ID,
                groupId: GROUP1_ID,
                assignedTo: [USER1_ID, USER2_ID],
                status: 'pending'
            });
        });

        // Step 2: Verify user3 cannot access group or task initially
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore().collection('groups').doc(GROUP1_ID).get()
        );
        await assertFails(
            user3Context.firestore().collection('tasks').doc(TASK1_ID).get()
        );

        // Step 3: User1 adds user3 to the group
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('groups').doc(GROUP1_ID).update({
                memberIds: [USER1_ID, USER2_ID, USER3_ID]
            })
        );

        // Step 4: Verify user3 can now access the group
        const groupDoc = await assertSucceeds(
            user3Context.firestore().collection('groups').doc(GROUP1_ID).get()
        );
        expect(groupDoc.exists).toBe(true);
        expect(groupDoc.data().memberIds).toContain(USER3_ID);

        // Step 5: Verify user3 can now access the group task
        const taskDoc = await assertSucceeds(
            user3Context.firestore().collection('tasks').doc(TASK1_ID).get()
        );
        expect(taskDoc.exists).toBe(true);
        expect(taskDoc.data().groupId).toBe(GROUP1_ID);

        // Step 6: User3 can create a new task in the group
        await assertSucceeds(
            user3Context.firestore().collection('tasks').doc(TASK2_ID).set({
                title: 'User3 Task',
                userId: USER3_ID,
                groupId: GROUP1_ID,
                assignedTo: [USER3_ID],
                status: 'pending'
            })
        );

        // Step 7: Verify other members can see user3's task
        const user2Context = testEnv.authenticatedContext(USER2_ID);
        const user3TaskDoc = await assertSucceeds(
            user2Context.firestore().collection('tasks').doc(TASK2_ID).get()
        );
        expect(user3TaskDoc.exists).toBe(true);
        expect(user3TaskDoc.data().userId).toBe(USER3_ID);
    });
});

describe('Integration Test 14.2 - Chat Workflow', () => {
    test('Complete workflow: Create chat → Send message → Verify participant access', async () => {
        // Step 1: User1 creates a direct chat with user2
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        await assertSucceeds(
            user1Context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: '',
                lastMessageTime: Date.now(),
                createdAt: new Date()
            })
        );

        // Step 2: User1 sends a message in the chat
        await assertSucceeds(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').set({
                    senderId: USER1_ID,
                    text: 'Hello, how are you?',
                    timestamp: Date.now()
                })
        );

        // Step 3: User2 (participant) can read the chat
        const user2Context = testEnv.authenticatedContext(USER2_ID);
        const chatDoc = await assertSucceeds(
            user2Context.firestore().collection('chats').doc(CHAT1_ID).get()
        );
        expect(chatDoc.exists).toBe(true);
        expect(chatDoc.data().participants).toContain(USER2_ID);

        // Step 4: User2 can read the message
        const messageDoc = await assertSucceeds(
            user2Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').get()
        );
        expect(messageDoc.exists).toBe(true);
        expect(messageDoc.data().text).toBe('Hello, how are you?');

        // Step 5: User2 sends a reply
        await assertSucceeds(
            user2Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg2').set({
                    senderId: USER2_ID,
                    text: 'I am doing great, thanks!',
                    timestamp: Date.now()
                })
        );

        // Step 6: User3 (non-participant) cannot access the chat

        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore().collection('chats').doc(CHAT1_ID).get()
        );

        // Step 7: User3 cannot read messages
        await assertFails(
            user3Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').get()
        );

        // Step 8: User3 cannot send messages
        await assertFails(
            user3Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg3').set({
                    senderId: USER3_ID,
                    text: 'Trying to intrude',
                    timestamp: Date.now()
                })
        );
    });

    test('Complete workflow: Test typing indicators work correctly', async () => {
        // Step 1: Create a chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: '',
                createdAt: new Date()
            });
        });

        // Step 2: User1 sets typing status
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('typing_status').doc(USER1_ID).set({
                    isTyping: true,
                    timestamp: Date.now()
                })
        );

        // Step 3: User2 can read user1's typing status
        const user2Context = testEnv.authenticatedContext(USER2_ID);
        const typingDoc = await assertSucceeds(
            user2Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('typing_status').doc(USER1_ID).get()
        );

        expect(typingDoc.exists).toBe(true);
        expect(typingDoc.data().isTyping).toBe(true);

        // Step 4: User1 stops typing
        await assertSucceeds(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('typing_status').doc(USER1_ID).update({
                    isTyping: false,
                    timestamp: Date.now()
                })
        );

        // Step 5: User1 cannot set user2's typing status
        await assertFails(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('typing_status').doc(USER2_ID).set({
                    isTyping: true,
                    timestamp: Date.now()
                })
        );

        // Step 6: User3 (non-participant) cannot read typing status
        const user3Context = testEnv.authenticatedContext(USER3_ID);
        await assertFails(
            user3Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('typing_status').doc(USER1_ID).get()
        );

        // Step 7: User3 cannot set typing status
        await assertFails(
            user3Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('typing_status').doc(USER3_ID).set({
                    isTyping: true,
                    timestamp: Date.now()
                })
        );
    });

    test('Complete workflow: Test message validation prevents invalid content', async () => {
        // Step 1: Create a chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });
        });

        const user1Context = testEnv.authenticatedContext(USER1_ID);


        // Step 2: Cannot send empty message (no text, imageUrl, or documentUrl)
        await assertFails(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').set({
                    senderId: USER1_ID,
                    timestamp: Date.now()
                })
        );

        // Step 3: Cannot send message with text exceeding 10,000 characters
        const longText = 'a'.repeat(10001);
        await assertFails(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg2').set({
                    senderId: USER1_ID,
                    text: longText,
                    timestamp: Date.now()
                })
        );

        // Step 4: Can send message with valid text
        await assertSucceeds(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg3').set({
                    senderId: USER1_ID,
                    text: 'Valid message',
                    timestamp: Date.now()
                })
        );

        // Step 5: Cannot send message with external imageUrl
        await assertFails(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg4').set({
                    senderId: USER1_ID,
                    imageUrl: 'https://example.com/image.jpg',
                    timestamp: Date.now()
                })
        );

        // Step 6: Can send message with valid Firebase Storage imageUrl
        await assertSucceeds(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg5').set({
                    senderId: USER1_ID,
                    imageUrl: 'https://firebasestorage.googleapis.com/v0/b/bucket/o/image.jpg',
                    timestamp: Date.now()
                })
        );


        // Step 7: Cannot send message with external documentUrl
        await assertFails(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg6').set({
                    senderId: USER1_ID,
                    documentUrl: 'https://malicious.com/doc.pdf',
                    timestamp: Date.now()
                })
        );

        // Step 8: Can send message with valid Firebase Storage documentUrl
        await assertSucceeds(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg7').set({
                    senderId: USER1_ID,
                    documentUrl: 'https://firebasestorage.googleapis.com/v0/b/bucket/o/doc.pdf',
                    timestamp: Date.now()
                })
        );

        // Step 9: Cannot spoof senderId
        await assertFails(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg8').set({
                    senderId: USER2_ID,
                    text: 'Spoofed message',
                    timestamp: Date.now()
                })
        );
    });
});

describe('Integration Test 14.3 - File Upload Workflow', () => {
    test('Complete workflow: Upload profile picture → Verify URL validation', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        // Step 1: User1 uploads a valid profile picture
        const imageData = Buffer.from('fake-image-data');
        await assertSucceeds(
            user1Context.storage()
                .ref(`profile_pictures/${USER1_ID}/avatar.jpg`)
                .put(imageData, { contentType: 'image/jpeg' })
        );

        // Step 2: Get the download URL (simulated Firebase Storage URL)
        const profileImageUrl = 'https://firebasestorage.googleapis.com/v0/b/bucket/o/profile_pictures%2Fuser1%2Favatar.jpg';

        // Step 3: User1 creates their profile with the valid URL

        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).set({
                name: 'User One',
                email: 'user1@test.com',
                profileImageUrl: profileImageUrl
            })
        );

        // Step 4: Verify the profile was created successfully
        const userDoc = await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).get()
        );
        expect(userDoc.exists).toBe(true);
        expect(userDoc.data().profileImageUrl).toBe(profileImageUrl);

        // Step 5: User1 tries to update with an external URL (should fail)
        await assertFails(
            user1Context.firestore().collection('users').doc(USER1_ID).update({
                profileImageUrl: 'https://example.com/malicious.jpg'
            })
        );

        // Step 6: User1 can update with another valid Firebase Storage URL
        const newProfileUrl = 'https://firebasestorage.googleapis.com/v0/b/bucket/o/profile_pictures%2Fuser1%2Fnew-avatar.jpg';
        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).update({
                profileImageUrl: newProfileUrl
            })
        );

        // Step 7: User1 can clear their profile picture (empty string)
        await assertSucceeds(
            user1Context.firestore().collection('users').doc(USER1_ID).update({
                profileImageUrl: ''
            })
        );
    });

    test('Complete workflow: Upload chat attachment → Verify type and size validation', async () => {
        // Step 1: Create a chat
        await testEnv.withSecurityRulesDisabled(async (context) => {
            await context.firestore().collection('chats').doc(CHAT1_ID).set({
                type: 'DIRECT',
                participants: [USER1_ID, USER2_ID],
                lastMessage: ''
            });
        });

        const user1Context = testEnv.authenticatedContext(USER1_ID);


        // Step 2: User1 uploads a valid image attachment
        const imageData = Buffer.from('fake-image-data');
        await assertSucceeds(
            user1Context.storage()
                .ref(`chat_attachments/${CHAT1_ID}/photo.jpg`)
                .put(imageData, { contentType: 'image/jpeg' })
        );

        // Step 3: User1 sends a message with the image URL
        const imageUrl = 'https://firebasestorage.googleapis.com/v0/b/bucket/o/chat_attachments%2Fchat1%2Fphoto.jpg';
        await assertSucceeds(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').set({
                    senderId: USER1_ID,
                    imageUrl: imageUrl,
                    timestamp: Date.now()
                })
        );

        // Step 4: User1 uploads a valid PDF document
        const pdfData = Buffer.from('fake-pdf-data');
        await assertSucceeds(
            user1Context.storage()
                .ref(`chat_attachments/${CHAT1_ID}/document.pdf`)
                .put(pdfData, { contentType: 'application/pdf' })
        );

        // Step 5: User1 sends a message with the document URL
        const docUrl = 'https://firebasestorage.googleapis.com/v0/b/bucket/o/chat_attachments%2Fchat1%2Fdocument.pdf';
        await assertSucceeds(
            user1Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg2').set({
                    senderId: USER1_ID,
                    documentUrl: docUrl,
                    timestamp: Date.now()
                })
        );

        // Step 6: User2 can read the messages with attachments
        const user2Context = testEnv.authenticatedContext(USER2_ID);
        const imageMsg = await assertSucceeds(
            user2Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg1').get()
        );
        expect(imageMsg.data().imageUrl).toBe(imageUrl);


        const docMsg = await assertSucceeds(
            user2Context.firestore()
                .collection('chats').doc(CHAT1_ID)
                .collection('messages').doc('msg2').get()
        );
        expect(docMsg.data().documentUrl).toBe(docUrl);
    });

    test('Complete workflow: Test invalid uploads are rejected with clear errors', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);

        // Step 1: Try to upload non-image to profile folder (should fail)
        const pdfData = Buffer.from('fake-pdf-data');
        await assertFails(
            user1Context.storage()
                .ref(`profile_pictures/${USER1_ID}/document.pdf`)
                .put(pdfData, { contentType: 'application/pdf' })
        );

        // Step 2: Try to upload file exceeding 5MB to profile folder (should fail)
        const largeImageData = Buffer.alloc(6 * 1024 * 1024);
        await assertFails(
            user1Context.storage()
                .ref(`profile_pictures/${USER1_ID}/large-avatar.jpg`)
                .put(largeImageData, { contentType: 'image/jpeg' })
        );

        // Step 3: Try to upload file exceeding 10MB to chat attachments (should fail)
        const largeFileData = Buffer.alloc(11 * 1024 * 1024);
        await assertFails(
            user1Context.storage()
                .ref(`chat_attachments/${CHAT1_ID}/large-file.pdf`)
                .put(largeFileData, { contentType: 'application/pdf' })
        );

        // Step 4: Try to upload executable file to chat attachments (should fail)
        const exeData = Buffer.from('fake-exe-data');
        await assertFails(
            user1Context.storage()
                .ref(`chat_attachments/${CHAT1_ID}/malware.exe`)
                .put(exeData, { contentType: 'application/x-msdownload' })
        );

        // Step 5: Try to upload to another user's profile folder (should fail)
        const imageData = Buffer.from('fake-image-data');
        await assertFails(
            user1Context.storage()
                .ref(`profile_pictures/${USER2_ID}/avatar.jpg`)
                .put(imageData, { contentType: 'image/jpeg' })
        );


        // Step 6: Try to upload to invalid path (should fail)
        await assertFails(
            user1Context.storage()
                .ref('random_folder/file.txt')
                .put(imageData, { contentType: 'text/plain' })
        );

        // Step 7: Verify valid uploads still work
        await assertSucceeds(
            user1Context.storage()
                .ref(`profile_pictures/${USER1_ID}/valid-avatar.jpg`)
                .put(imageData, { contentType: 'image/jpeg' })
        );

        await assertSucceeds(
            user1Context.storage()
                .ref(`chat_attachments/${CHAT1_ID}/valid-doc.pdf`)
                .put(pdfData, { contentType: 'application/pdf' })
        );
    });
});
