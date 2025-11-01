// Firebase Admin SDK script to fix existing user profiles
// Run with: node fix-user-profiles.js

const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function fixUserProfiles() {
    console.log('Starting user profile migration...');

    try {
        const usersSnapshot = await db.collection('users').get();
        console.log(`Found ${usersSnapshot.size} user documents`);

        let fixed = 0;
        let skipped = 0;

        const batch = db.batch();

        usersSnapshot.forEach((doc) => {
            const data = doc.data();
            const userId = doc.id;

            // Check if userId field exists in document
            if (!data.userId) {
                console.log(`Fixing user ${userId} - adding userId field`);
                batch.update(doc.ref, { userId: userId });
                fixed++;
            } else if (data.userId !== userId) {
                console.log(`Fixing user ${userId} - userId mismatch (${data.userId} -> ${userId})`);
                batch.update(doc.ref, { userId: userId });
                fixed++;
            } else {
                skipped++;
            }
        });

        if (fixed > 0) {
            await batch.commit();
            console.log(`✅ Fixed ${fixed} user profiles`);
        }

        console.log(`✅ Skipped ${skipped} profiles (already correct)`);
        console.log('Migration complete!');

    } catch (error) {
        console.error('❌ Error during migration:', error);
        process.exit(1);
    }

    process.exit(0);
}

fixUserProfiles();
