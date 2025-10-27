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
const CHAT1_ID = 'chat1';

beforeAll(async () => {
    // Initialize test environment with Storage rules
    testEnv = await initializeTestEnvironment({
        projectId: 'test-project',
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
    await testEnv.clearStorage();
});

describe('Storage Rules - Profile Pictures', () => {
    test('authenticated user can upload image to their profile folder', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const imageData = Buffer.from('fake-image-data');

        await assertSucceeds(
            user1Context.storage()
                .ref(`profile_pictures/${USER1_ID}/avatar.jpg`)
                .put(imageData, { contentType: 'image/jpeg' })
        );
    });

    test('authenticated user can read from their profile folder', async () => {
        // Setup: Upload a file
        await testEnv.withSecurityRulesDisabled(async (context) => {
            const imageData = Buffer.from('fake-image-data');
            await context.storage()
                .ref(`profile_pictures/${USER1_ID}/avatar.jpg`)
                .put(imageData, { contentType: 'image/jpeg' });
        });

        // Test: user1 can read their file
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.storage()
                .ref(`profile_pictures/${USER1_ID}/avatar.jpg`)
                .getDownloadURL()
        );
    });

    test('authenticated user cannot upload to another users profile folder', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const imageData = Buffer.from('fake-image-data');

        await assertFails(
            user1Context.storage()
                .ref(`profile_pictures/${USER2_ID}/avatar.jpg`)
                .put(imageData, { contentType: 'image/jpeg' })
        );
    });

    test('unauthenticated user cannot upload profile picture', async () => {
        const unauthContext = testEnv.unauthenticatedContext();
        const imageData = Buffer.from('fake-image-data');

        await assertFails(
            unauthContext.storage()
                .ref(`profile_pictures/${USER1_ID}/avatar.jpg`)
                .put(imageData, { contentType: 'image/jpeg' })
        );
    });

    test('user can upload PNG image to profile folder', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const imageData = Buffer.from('fake-png-data');

        await assertSucceeds(
            user1Context.storage()
                .ref(`profile_pictures/${USER1_ID}/avatar.png`)
                .put(imageData, { contentType: 'image/png' })
        );
    });

    test('user can upload WebP image to profile folder', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const imageData = Buffer.from('fake-webp-data');

        await assertSucceeds(
            user1Context.storage()
                .ref(`profile_pictures/${USER1_ID}/avatar.webp`)
                .put(imageData, { contentType: 'image/webp' })
        );
    });

    test('user cannot upload non-image file to profile folder', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const pdfData = Buffer.from('fake-pdf-data');

        await assertFails(
            user1Context.storage()
                .ref(`profile_pictures/${USER1_ID}/document.pdf`)
                .put(pdfData, { contentType: 'application/pdf' })
        );
    });

    test('user cannot upload file exceeding 5MB to profile folder', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        // Create a buffer larger than 5MB
        const largeImageData = Buffer.alloc(6 * 1024 * 1024);

        await assertFails(
            user1Context.storage()
                .ref(`profile_pictures/${USER1_ID}/large-avatar.jpg`)
                .put(largeImageData, { contentType: 'image/jpeg' })
        );
    });

    test('user can upload file at 5MB limit to profile folder', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        // Create a buffer exactly 5MB
        const maxImageData = Buffer.alloc(5 * 1024 * 1024);

        await assertSucceeds(
            user1Context.storage()
                .ref(`profile_pictures/${USER1_ID}/max-avatar.jpg`)
                .put(maxImageData, { contentType: 'image/jpeg' })
        );
    });
});

describe('Storage Rules - Chat Attachments', () => {
    test('authenticated user can upload image to chat attachments', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const imageData = Buffer.from('fake-image-data');

        await assertSucceeds(
            user1Context.storage()
                .ref(`chat_attachments/${CHAT1_ID}/photo.jpg`)
                .put(imageData, { contentType: 'image/jpeg' })
        );
    });

    test('authenticated user can upload PDF to chat attachments', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const pdfData = Buffer.from('fake-pdf-data');

        await assertSucceeds(
            user1Context.storage()
                .ref(`chat_attachments/${CHAT1_ID}/document.pdf`)
                .put(pdfData, { contentType: 'application/pdf' })
        );
    });

    test('authenticated user can upload Word document to chat attachments', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const docData = Buffer.from('fake-doc-data');

        await assertSucceeds(
            user1Context.storage()
                .ref(`chat_attachments/${CHAT1_ID}/document.docx`)
                .put(docData, { contentType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' })
        );
    });

    test('authenticated user can upload text file to chat attachments', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const textData = Buffer.from('fake-text-data');

        await assertSucceeds(
            user1Context.storage()
                .ref(`chat_attachments/${CHAT1_ID}/notes.txt`)
                .put(textData, { contentType: 'text/plain' })
        );
    });

    test('authenticated user can read chat attachments', async () => {
        // Setup: Upload a file
        await testEnv.withSecurityRulesDisabled(async (context) => {
            const imageData = Buffer.from('fake-image-data');
            await context.storage()
                .ref(`chat_attachments/${CHAT1_ID}/photo.jpg`)
                .put(imageData, { contentType: 'image/jpeg' });
        });

        // Test: user1 can read the file
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertSucceeds(
            user1Context.storage()
                .ref(`chat_attachments/${CHAT1_ID}/photo.jpg`)
                .getDownloadURL()
        );
    });

    test('unauthenticated user cannot upload to chat attachments', async () => {
        const unauthContext = testEnv.unauthenticatedContext();
        const imageData = Buffer.from('fake-image-data');

        await assertFails(
            unauthContext.storage()
                .ref(`chat_attachments/${CHAT1_ID}/photo.jpg`)
                .put(imageData, { contentType: 'image/jpeg' })
        );
    });

    test('user cannot upload file exceeding 10MB to chat attachments', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        // Create a buffer larger than 10MB
        const largeFileData = Buffer.alloc(11 * 1024 * 1024);

        await assertFails(
            user1Context.storage()
                .ref(`chat_attachments/${CHAT1_ID}/large-file.pdf`)
                .put(largeFileData, { contentType: 'application/pdf' })
        );
    });

    test('user can upload file at 10MB limit to chat attachments', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        // Create a buffer exactly 10MB
        const maxFileData = Buffer.alloc(10 * 1024 * 1024);

        await assertSucceeds(
            user1Context.storage()
                .ref(`chat_attachments/${CHAT1_ID}/max-file.pdf`)
                .put(maxFileData, { contentType: 'application/pdf' })
        );
    });

    test('user cannot upload executable file to chat attachments', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const exeData = Buffer.from('fake-exe-data');

        await assertFails(
            user1Context.storage()
                .ref(`chat_attachments/${CHAT1_ID}/malware.exe`)
                .put(exeData, { contentType: 'application/x-msdownload' })
        );
    });

    test('user cannot upload script file to chat attachments', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const scriptData = Buffer.from('fake-script-data');

        await assertFails(
            user1Context.storage()
                .ref(`chat_attachments/${CHAT1_ID}/script.js`)
                .put(scriptData, { contentType: 'application/javascript' })
        );
    });
});

describe('Storage Rules - Invalid Paths', () => {
    test('user cannot upload to root path', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const data = Buffer.from('fake-data');

        await assertFails(
            user1Context.storage()
                .ref('file.txt')
                .put(data, { contentType: 'text/plain' })
        );
    });

    test('user cannot upload to arbitrary path', async () => {
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        const data = Buffer.from('fake-data');

        await assertFails(
            user1Context.storage()
                .ref('random_folder/file.txt')
                .put(data, { contentType: 'text/plain' })
        );
    });

    test('user cannot read from arbitrary path', async () => {
        // Setup: Upload a file to arbitrary path
        await testEnv.withSecurityRulesDisabled(async (context) => {
            const data = Buffer.from('fake-data');
            await context.storage()
                .ref('secret_folder/secret.txt')
                .put(data, { contentType: 'text/plain' });
        });

        // Test: user1 cannot read the file
        const user1Context = testEnv.authenticatedContext(USER1_ID);
        await assertFails(
            user1Context.storage()
                .ref('secret_folder/secret.txt')
                .getDownloadURL()
        );
    });
});
