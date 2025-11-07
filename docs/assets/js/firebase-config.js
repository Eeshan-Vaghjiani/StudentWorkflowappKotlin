/**
 * Firebase Configuration Module
 * 
 * This module initializes Firebase services for the Account Deletion page.
 * It imports the Firebase SDK from CDN and exports auth and db instances.
 * 
 * Requirements: 3.1, 3.2
 */

// Import Firebase SDK v10.x from CDN
import { initializeApp } from 'https://www.gstatic.com/firebasejs/10.7.1/firebase-app.js';
import { getAuth } from 'https://www.gstatic.com/firebasejs/10.7.1/firebase-auth.js';
import { getFirestore } from 'https://www.gstatic.com/firebasejs/10.7.1/firebase-firestore.js';

// Firebase project configuration
const firebaseConfig = {
  apiKey: "AIzaSyB6QouijeF9AqxwEY7JYfFsyvRdAOre6-I",
  authDomain: "android-logreg.firebaseapp.com",
  projectId: "android-logreg",
  storageBucket: "android-logreg.firebasestorage.app",
  messagingSenderId: "52796977485",
  appId: "1:52796977485:android:702289c23df108cdbd0704"
};

// Initialize Firebase with error handling
let app;
let auth;
let db;

try {
  app = initializeApp(firebaseConfig);
  auth = getAuth(app);
  db = getFirestore(app);
  
  console.log('Firebase initialized successfully');
} catch (error) {
  console.error('Firebase initialization failed:', error);
  
  // Display user-friendly error message
  const errorMessage = 'Unable to connect to Firebase services. Please refresh the page or try again later.';
  
  // If there's a status message element on the page, show the error
  if (typeof document !== 'undefined') {
    document.addEventListener('DOMContentLoaded', () => {
      const statusElement = document.getElementById('statusMessage');
      if (statusElement) {
        statusElement.textContent = errorMessage;
        statusElement.className = 'message error';
        statusElement.classList.remove('hidden');
      }
    });
  }
  
  // Re-throw to prevent further execution
  throw new Error('Firebase initialization failed: ' + error.message);
}

// Export auth and db instances for use in other modules
export { auth, db };
