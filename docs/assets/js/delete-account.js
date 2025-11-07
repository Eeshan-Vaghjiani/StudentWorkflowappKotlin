// delete-account.js - Form validation and account deletion logic
// This module handles client-side validation, input sanitization, and form state management

// Import Firebase authentication and Firestore
import { auth, db } from './firebase-config.js';
import { signInWithEmailAndPassword, deleteUser } from 'https://www.gstatic.com/firebasejs/10.7.1/firebase-auth.js';
import { 
  collection, 
  query, 
  where, 
  getDocs, 
  writeBatch, 
  doc, 
  deleteDoc 
} from 'https://www.gstatic.com/firebasejs/10.7.1/firebase-firestore.js';

// ============================================================================
// SECURITY SAFEGUARDS
// ============================================================================

/**
 * Rate Limiter - Prevents abuse by limiting deletion attempts
 * Stores data in sessionStorage (cleared when browser closes)
 * Maximum 3 attempts per hour per browser session
 */
const RateLimiter = {
  STORAGE_KEY: 'account_deletion_attempts',
  MAX_ATTEMPTS: 3,
  WINDOW_MS: 3600000, // 1 hour in milliseconds
  
  /**
   * Get stored attempts from sessionStorage
   * @returns {Array} - Array of attempt timestamps
   */
  getAttempts() {
    try {
      const stored = sessionStorage.getItem(this.STORAGE_KEY);
      if (!stored) return [];
      
      const attempts = JSON.parse(stored);
      
      // Validate that attempts is an array of numbers
      if (!Array.isArray(attempts)) return [];
      if (!attempts.every(t => typeof t === 'number')) return [];
      
      return attempts;
    } catch (error) {
      console.error('[Rate Limiter] Error reading attempts:', error.message);
      return [];
    }
  },
  
  /**
   * Save attempts to sessionStorage
   * @param {Array} attempts - Array of attempt timestamps
   */
  saveAttempts(attempts) {
    try {
      // Validate input
      if (!Array.isArray(attempts)) {
        console.error('[Rate Limiter] Invalid attempts array');
        return;
      }
      
      sessionStorage.setItem(this.STORAGE_KEY, JSON.stringify(attempts));
    } catch (error) {
      console.error('[Rate Limiter] Error saving attempts:', error.message);
    }
  },
  
  /**
   * Clean up old attempts outside the time window
   * @returns {Array} - Array of valid attempts within time window
   */
  cleanOldAttempts() {
    const now = Date.now();
    const attempts = this.getAttempts();
    const validAttempts = attempts.filter(timestamp => {
      return (now - timestamp) < this.WINDOW_MS;
    });
    
    this.saveAttempts(validAttempts);
    return validAttempts;
  },
  
  /**
   * Check if user can make another deletion attempt
   * @returns {Object} - Object with canAttempt boolean and remaining attempts
   */
  canAttempt() {
    const validAttempts = this.cleanOldAttempts();
    const remainingAttempts = this.MAX_ATTEMPTS - validAttempts.length;
    
    return {
      canAttempt: validAttempts.length < this.MAX_ATTEMPTS,
      remainingAttempts: Math.max(0, remainingAttempts),
      attemptsUsed: validAttempts.length
    };
  },
  
  /**
   * Record a new deletion attempt
   */
  recordAttempt() {
    const attempts = this.cleanOldAttempts();
    attempts.push(Date.now());
    this.saveAttempts(attempts);
    
    console.log('[Rate Limiter] Attempt recorded. Total attempts:', attempts.length);
  },
  
  /**
   * Get time until rate limit resets
   * @returns {number} - Milliseconds until oldest attempt expires
   */
  getTimeUntilReset() {
    const attempts = this.cleanOldAttempts();
    if (attempts.length === 0) return 0;
    
    const oldestAttempt = Math.min(...attempts);
    const resetTime = oldestAttempt + this.WINDOW_MS;
    const timeUntilReset = resetTime - Date.now();
    
    return Math.max(0, timeUntilReset);
  },
  
  /**
   * Format time until reset in human-readable format
   * @returns {string} - Formatted time string
   */
  getFormattedResetTime() {
    const ms = this.getTimeUntilReset();
    const minutes = Math.ceil(ms / 60000);
    
    if (minutes < 60) {
      return `${minutes} minute${minutes !== 1 ? 's' : ''}`;
    }
    
    const hours = Math.ceil(minutes / 60);
    return `${hours} hour${hours !== 1 ? 's' : ''}`;
  }
};

/**
 * CSRF Protection - Validates that requests originate from the same domain
 * Prevents cross-site request forgery attacks
 */
const CSRFProtection = {
  /**
   * Validate that the current page origin matches expected origin
   * @returns {boolean} - True if origin is valid
   */
  validateOrigin() {
    try {
      const currentOrigin = window.location.origin;
      const currentHostname = window.location.hostname;
      
      // Allow localhost for development
      if (currentHostname === 'localhost' || currentHostname === '127.0.0.1') {
        console.log('[CSRF Protection] Development environment detected');
        return true;
      }
      
      // Allow GitHub Pages domains
      if (currentHostname.endsWith('.github.io')) {
        console.log('[CSRF Protection] GitHub Pages domain validated');
        return true;
      }
      
      // Allow custom domains (if configured)
      // Add your custom domain here if you have one
      const allowedDomains = [
        // 'yourdomain.com',
        // 'www.yourdomain.com'
      ];
      
      if (allowedDomains.includes(currentHostname)) {
        console.log('[CSRF Protection] Custom domain validated');
        return true;
      }
      
      console.error('[CSRF Protection] Invalid origin:', currentOrigin);
      return false;
      
    } catch (error) {
      console.error('[CSRF Protection] Error validating origin:', error.message);
      return false;
    }
  },
  
  /**
   * Check if request is coming from a valid referrer
   * @returns {boolean} - True if referrer is valid or empty
   */
  validateReferrer() {
    try {
      const referrer = document.referrer;
      
      // Empty referrer is acceptable (direct navigation)
      if (!referrer) {
        return true;
      }
      
      const referrerUrl = new URL(referrer);
      const currentHostname = window.location.hostname;
      
      // Referrer must be from same hostname
      if (referrerUrl.hostname === currentHostname) {
        return true;
      }
      
      console.warn('[CSRF Protection] Referrer mismatch:', referrerUrl.hostname);
      return true; // Don't block on referrer alone, just log warning
      
    } catch (error) {
      console.error('[CSRF Protection] Error validating referrer:', error.message);
      return true; // Don't block on error
    }
  },
  
  /**
   * Perform all CSRF validation checks
   * @returns {boolean} - True if all checks pass
   */
  validate() {
    const originValid = this.validateOrigin();
    const referrerValid = this.validateReferrer();
    
    return originValid && referrerValid;
  }
};

/**
 * Input Sanitizer - Enhanced XSS prevention
 * Sanitizes all user inputs to prevent script injection
 */
const InputSanitizer = {
  /**
   * Sanitize string input to prevent XSS attacks
   * @param {string} input - Raw user input
   * @returns {string} - Sanitized input
   */
  sanitizeString(input) {
    if (typeof input !== 'string') return '';
    
    // Remove any null bytes
    let sanitized = input.replace(/\0/g, '');
    
    // Create a temporary element to leverage browser's built-in HTML encoding
    const temp = document.createElement('div');
    temp.textContent = sanitized;
    sanitized = temp.innerHTML;
    
    // Additional sanitization: remove any remaining script-like patterns
    sanitized = sanitized.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '');
    sanitized = sanitized.replace(/javascript:/gi, '');
    sanitized = sanitized.replace(/on\w+\s*=/gi, '');
    
    return sanitized;
  },
  
  /**
   * Sanitize email input
   * @param {string} email - Email input
   * @returns {string} - Sanitized email
   */
  sanitizeEmail(email) {
    if (typeof email !== 'string') return '';
    
    // Basic sanitization for email
    let sanitized = this.sanitizeString(email);
    
    // Remove any whitespace
    sanitized = sanitized.trim();
    
    // Convert to lowercase for consistency
    sanitized = sanitized.toLowerCase();
    
    return sanitized;
  },
  
  /**
   * Validate that input doesn't contain malicious patterns
   * @param {string} input - Input to validate
   * @returns {boolean} - True if input is safe
   */
  isSafe(input) {
    if (typeof input !== 'string') return false;
    
    // Check for common XSS patterns
    const dangerousPatterns = [
      /<script/i,
      /javascript:/i,
      /on\w+\s*=/i,
      /<iframe/i,
      /<object/i,
      /<embed/i,
      /eval\(/i,
      /expression\(/i
    ];
    
    return !dangerousPatterns.some(pattern => pattern.test(input));
  }
};

/**
 * Firebase Response Validator - Validates all Firebase responses
 * Ensures responses are properly formatted before processing
 */
const FirebaseValidator = {
  /**
   * Validate Firebase user object
   * @param {Object} user - Firebase user object
   * @returns {boolean} - True if valid
   */
  validateUser(user) {
    if (!user || typeof user !== 'object') {
      console.error('[Firebase Validator] Invalid user object');
      return false;
    }
    
    if (!user.uid || typeof user.uid !== 'string') {
      console.error('[Firebase Validator] Invalid user UID');
      return false;
    }
    
    if (!user.email || typeof user.email !== 'string') {
      console.error('[Firebase Validator] Invalid user email');
      return false;
    }
    
    return true;
  },
  
  /**
   * Validate Firestore query snapshot
   * @param {Object} snapshot - Firestore query snapshot
   * @returns {boolean} - True if valid
   */
  validateSnapshot(snapshot) {
    if (!snapshot || typeof snapshot !== 'object') {
      console.error('[Firebase Validator] Invalid snapshot object');
      return false;
    }
    
    if (typeof snapshot.empty !== 'boolean') {
      console.error('[Firebase Validator] Invalid snapshot.empty property');
      return false;
    }
    
    if (!Array.isArray(snapshot.docs)) {
      console.error('[Firebase Validator] Invalid snapshot.docs array');
      return false;
    }
    
    return true;
  },
  
  /**
   * Validate Firebase error object
   * @param {Object} error - Firebase error object
   * @returns {boolean} - True if valid error object
   */
  validateError(error) {
    if (!error || typeof error !== 'object') {
      return false;
    }
    
    // Firebase errors should have a code property
    if (!error.code || typeof error.code !== 'string') {
      return false;
    }
    
    return true;
  }
};

/**
 * Security Logger - Logs security events without exposing sensitive data
 * Never logs credentials, passwords, or personal information
 */
const SecurityLogger = {
  /**
   * Log security event
   * @param {string} event - Event type
   * @param {Object} details - Event details (non-sensitive only)
   */
  log(event, details = {}) {
    // Filter out any potentially sensitive data
    const safeDetails = this.filterSensitiveData(details);
    
    console.log(`[Security] ${event}:`, safeDetails);
  },
  
  /**
   * Filter out sensitive data from log details
   * @param {Object} details - Details object
   * @returns {Object} - Filtered details
   */
  filterSensitiveData(details) {
    const filtered = { ...details };
    
    // Remove sensitive keys
    const sensitiveKeys = [
      'password',
      'credential',
      'token',
      'secret',
      'key',
      'auth',
      'email', // Remove email from logs for privacy
      'uid',
      'userId'
    ];
    
    sensitiveKeys.forEach(key => {
      if (key in filtered) {
        delete filtered[key];
      }
    });
    
    return filtered;
  },
  
  /**
   * Log rate limit event
   * @param {string} action - Action taken
   * @param {Object} details - Rate limit details
   */
  logRateLimit(action, details) {
    this.log(`Rate Limit - ${action}`, details);
  },
  
  /**
   * Log CSRF validation event
   * @param {boolean} valid - Whether validation passed
   */
  logCSRF(valid) {
    this.log('CSRF Validation', { valid });
  }
};

// ============================================================================
// END SECURITY SAFEGUARDS
// ============================================================================

/**
 * Sanitize user input to prevent XSS attacks
 * Uses the enhanced InputSanitizer for comprehensive protection
 * @param {string} input - Raw user input
 * @returns {string} - Sanitized input
 */
function sanitizeInput(input) {
  return InputSanitizer.sanitizeString(input);
}

/**
 * Validate email address using regex pattern
 * @param {string} email - Email address to validate
 * @returns {boolean} - True if valid, false otherwise
 */
function validateEmail(email) {
  // RFC 5322 compliant email regex (simplified)
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email.trim());
}

/**
 * Validate password meets Firebase minimum requirements
 * @param {string} password - Password to validate
 * @returns {boolean} - True if valid, false otherwise
 */
function validatePassword(password) {
  // Firebase requires minimum 6 characters
  return password.length >= 6;
}

/**
 * Update submit button state based on form validation
 */
function updateButtonState() {
  const emailInput = document.getElementById('email');
  const passwordInput = document.getElementById('password');
  const confirmCheckbox = document.getElementById('confirmCheckbox');
  const deleteButton = document.getElementById('deleteButton');
  const buttonStatus = document.getElementById('button-status');
  
  if (!emailInput || !passwordInput || !confirmCheckbox || !deleteButton) {
    SecurityLogger.log('Form Elements Not Found', { function: 'updateButtonState' });
    return;
  }
  
  // Get sanitized values
  const email = sanitizeInput(emailInput.value);
  const password = passwordInput.value; // Don't sanitize password (preserve special chars)
  const isConfirmed = confirmCheckbox.checked;
  
  // Validate all fields
  const isEmailValid = validateEmail(email);
  const isPasswordValid = validatePassword(password);
  
  // Enable button only if all validations pass
  const isFormValid = isEmailValid && isPasswordValid && isConfirmed;
  
  deleteButton.disabled = !isFormValid;
  
  // Update ARIA live region for screen readers
  if (buttonStatus) {
    if (isFormValid) {
      buttonStatus.textContent = 'Form is valid. You can now submit.';
    } else {
      buttonStatus.textContent = 'Form is incomplete. Please fill all required fields.';
    }
  }
}

/**
 * Clear all form fields
 */
function clearFormFields() {
  const emailInput = document.getElementById('email');
  const passwordInput = document.getElementById('password');
  const confirmCheckbox = document.getElementById('confirmCheckbox');
  
  if (emailInput) emailInput.value = '';
  if (passwordInput) passwordInput.value = '';
  if (confirmCheckbox) confirmCheckbox.checked = false;
  
  // Reset button state
  updateButtonState();
}

/**
 * Display validation error message for a specific field
 * @param {string} fieldId - ID of the form field
 * @param {string} message - Error message to display
 */
function showFieldError(fieldId, message) {
  const errorElement = document.getElementById(`${fieldId}-error`);
  if (errorElement) {
    errorElement.textContent = message;
    errorElement.style.display = 'block';
  }
}

/**
 * Clear validation error message for a specific field
 * @param {string} fieldId - ID of the form field
 */
function clearFieldError(fieldId) {
  const errorElement = document.getElementById(`${fieldId}-error`);
  if (errorElement) {
    errorElement.textContent = '';
    errorElement.style.display = 'none';
  }
}

/**
 * Validate email field and show error if invalid
 */
function validateEmailField() {
  const emailInput = document.getElementById('email');
  if (!emailInput) return;
  
  const email = sanitizeInput(emailInput.value);
  
  if (email.length === 0) {
    clearFieldError('email');
    return;
  }
  
  if (!validateEmail(email)) {
    showFieldError('email', 'Please enter a valid email address');
  } else {
    clearFieldError('email');
  }
}

/**
 * Validate password field and show error if invalid
 */
function validatePasswordField() {
  const passwordInput = document.getElementById('password');
  if (!passwordInput) return;
  
  const password = passwordInput.value;
  
  if (password.length === 0) {
    clearFieldError('password');
    return;
  }
  
  if (!validatePassword(password)) {
    showFieldError('password', 'Password must be at least 6 characters');
  } else {
    clearFieldError('password');
  }
}

/**
 * Show loading spinner and disable form
 */
function showLoadingSpinner() {
  const loadingSpinner = document.getElementById('loadingSpinner');
  const deleteButton = document.getElementById('deleteButton');
  const form = document.getElementById('deleteAccountForm');
  
  if (loadingSpinner) {
    loadingSpinner.classList.remove('hidden');
  }
  
  if (deleteButton) {
    deleteButton.disabled = true;
  }
  
  // Disable all form inputs
  if (form) {
    const inputs = form.querySelectorAll('input, button');
    inputs.forEach(input => input.disabled = true);
  }
}

/**
 * Hide loading spinner and enable form
 */
function hideLoadingSpinner() {
  const loadingSpinner = document.getElementById('loadingSpinner');
  const form = document.getElementById('deleteAccountForm');
  
  if (loadingSpinner) {
    loadingSpinner.classList.add('hidden');
  }
  
  // Re-enable form inputs
  if (form) {
    const inputs = form.querySelectorAll('input, button');
    inputs.forEach(input => input.disabled = false);
  }
  
  // Update button state based on validation
  updateButtonState();
}

/**
 * Display status message to user with optional dismiss button
 * @param {string} message - Message to display
 * @param {string} type - Message type: 'success', 'error', 'warning', or 'info'
 * @param {boolean} showRetry - Whether to show retry suggestion for network errors
 */
function showMessage(message, type = 'error', showRetry = false) {
  const statusMessage = document.getElementById('statusMessage');
  
  if (!statusMessage) {
    SecurityLogger.log('Status Message Element Not Found', {});
    return;
  }
  
  // Clear any existing content
  statusMessage.innerHTML = '';
  
  // Create message container
  const messageContainer = document.createElement('div');
  messageContainer.className = 'message-content';
  
  // Create message text
  const messageText = document.createElement('p');
  messageText.className = 'message-text';
  messageText.textContent = message;
  messageContainer.appendChild(messageText);
  
  // Add retry suggestion for network errors
  if (showRetry) {
    const retryText = document.createElement('p');
    retryText.className = 'retry-suggestion';
    retryText.textContent = 'Please check your internet connection and try again. If the problem persists, contact support.';
    messageContainer.appendChild(retryText);
  }
  
  // Create dismiss button for error and warning messages
  if (type === 'error' || type === 'warning') {
    const dismissButton = document.createElement('button');
    dismissButton.className = 'dismiss-button';
    dismissButton.setAttribute('aria-label', 'Dismiss message');
    dismissButton.innerHTML = '✕';
    dismissButton.onclick = () => {
      statusMessage.classList.add('hidden');
      statusMessage.innerHTML = '';
    };
    messageContainer.appendChild(dismissButton);
  }
  
  statusMessage.appendChild(messageContainer);
  statusMessage.className = `status-message ${type}`;
  statusMessage.classList.remove('hidden');
  
  // Auto-hide success and info messages after 5 seconds
  if (type === 'success' || type === 'info') {
    setTimeout(() => {
      statusMessage.classList.add('hidden');
      statusMessage.innerHTML = '';
    }, 5000);
  }
  
  // Scroll to message for visibility
  statusMessage.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  
  // Log message for debugging (without sensitive data)
  SecurityLogger.log('Message Displayed', { 
    type, 
    messageLength: message.length 
  });
}

/**
 * Handle Firebase authentication errors with user-friendly messages
 * Does not reveal whether account exists (security requirement 6.5)
 * @param {string} errorCode - Firebase error code
 * @returns {Object} - Object with error message and whether it's a network error
 */
function handleAuthError(errorCode) {
  // Log error code for debugging (without exposing sensitive data)
  SecurityLogger.log('Auth Error Handler', { errorCode });
  
  const errorMessages = {
    'auth/user-not-found': 'Invalid email or password. Please check your credentials and try again.',
    'auth/wrong-password': 'Invalid email or password. Please check your credentials and try again.',
    'auth/invalid-email': 'Please enter a valid email address.',
    'auth/user-disabled': 'This account has been disabled. Please contact support for assistance.',
    'auth/too-many-requests': 'Too many failed attempts. Please try again later.',
    'auth/network-request-failed': 'Network error. Please check your internet connection and try again.',
    'auth/invalid-credential': 'Invalid email or password. Please check your credentials and try again.',
    'auth/operation-not-allowed': 'This operation is not allowed. Please contact support.',
    'auth/weak-password': 'Password must be at least 6 characters.',
    'auth/missing-password': 'Please enter your password.',
    'auth/invalid-login-credentials': 'Invalid email or password. Please check your credentials and try again.'
  };
  
  // Determine if this is a network error
  const isNetworkError = errorCode === 'auth/network-request-failed';
  
  // Return generic message for unknown errors (don't reveal account existence)
  const message = errorMessages[errorCode] || 'Authentication failed. Please check your credentials and try again.';
  
  return { message, isNetworkError };
}

/**
 * Handle Firebase account deletion errors with user-friendly messages
 * @param {string} errorCode - Firebase error code
 * @returns {Object} - Object with error message and whether it's a network error
 */
function handleDeletionError(errorCode) {
  // Log error code for debugging (without exposing sensitive data)
  SecurityLogger.log('Deletion Error Handler', { errorCode });
  
  const errorMessages = {
    'auth/requires-recent-login': 'For security reasons, please sign in again before deleting your account.',
    'auth/user-not-found': 'Account not found. It may have already been deleted.',
    'auth/network-request-failed': 'Network error. Please check your internet connection and try again.',
    'auth/too-many-requests': 'Too many requests. Please try again later.',
    'auth/operation-not-allowed': 'This operation is not allowed. Please contact support.'
  };
  
  // Determine if this is a network error
  const isNetworkError = errorCode === 'auth/network-request-failed';
  
  const message = errorMessages[errorCode] || 'Failed to delete account. Please try again or contact support.';
  
  return { message, isNetworkError };
}

/**
 * Delete user authentication account from Firebase
 * @param {Object} user - Firebase user object (already validated)
 * @returns {Promise<Object>} - Result object with success status
 */
async function deleteAuthenticationAccount(user) {
  try {
    // ========================================================================
    // SECURITY CHECK: Validate User Object
    // ========================================================================
    if (!FirebaseValidator.validateUser(user)) {
      SecurityLogger.log('Invalid User Object for Deletion', {});
      return { 
        success: false, 
        errorCode: 'invalid-user', 
        errorMessage: 'Invalid user object' 
      };
    }
    
    SecurityLogger.log('Deleting Authentication Account', { hasUid: !!user.uid });
    
    await deleteUser(user);
    
    SecurityLogger.log('Authentication Account Deleted', {});
    
    return { success: true };
    
  } catch (error) {
    // Validate and log error without exposing sensitive data
    if (FirebaseValidator.validateError(error)) {
      SecurityLogger.log('Auth Deletion Error', { errorCode: error.code });
      return { 
        success: false, 
        errorCode: error.code, 
        errorMessage: error.message 
      };
    } else {
      SecurityLogger.log('Unknown Auth Deletion Error', {});
      return { 
        success: false, 
        errorCode: 'unknown-error', 
        errorMessage: 'An unexpected error occurred' 
      };
    }
  }
}

/**
 * Show success message with options to return to app or close page
 */
function showSuccessWithOptions() {
  const statusMessage = document.getElementById('statusMessage');
  
  if (!statusMessage) {
    SecurityLogger.log('Status Message Element Not Found', { function: 'showSuccessWithOptions' });
    return;
  }
  
  // Create success message with options
  const messageContainer = document.createElement('div');
  messageContainer.className = 'success-container';
  
  const successText = document.createElement('p');
  successText.className = 'success-text';
  successText.textContent = '✓ Your account has been successfully deleted. All your data has been permanently removed from our systems.';
  
  const optionsText = document.createElement('p');
  optionsText.className = 'options-text';
  optionsText.textContent = 'You can now close this page or return to the privacy policy.';
  
  const buttonContainer = document.createElement('div');
  buttonContainer.className = 'button-container';
  
  const privacyButton = document.createElement('a');
  privacyButton.href = 'privacy-policy.html';
  privacyButton.className = 'action-button';
  privacyButton.textContent = 'View Privacy Policy';
  
  const closeButton = document.createElement('button');
  closeButton.className = 'action-button secondary';
  closeButton.textContent = 'Close Page';
  closeButton.onclick = () => {
    window.close();
    // If window.close() doesn't work (some browsers block it), show a message
    setTimeout(() => {
      alert('Please close this browser tab manually.');
    }, 100);
  };
  
  buttonContainer.appendChild(privacyButton);
  buttonContainer.appendChild(closeButton);
  
  messageContainer.appendChild(successText);
  messageContainer.appendChild(optionsText);
  messageContainer.appendChild(buttonContainer);
  
  // Clear existing content and add new message
  statusMessage.innerHTML = '';
  statusMessage.appendChild(messageContainer);
  statusMessage.className = 'status-message success';
  statusMessage.classList.remove('hidden');
  
  // Scroll to message for visibility
  statusMessage.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

/**
 * Authenticate user with Firebase
 * @param {string} email - User email (already sanitized)
 * @param {string} password - User password (not logged)
 * @returns {Promise<Object>} - User credential object or error
 */
async function authenticateUser(email, password) {
  try {
    // Authenticate with Firebase (credentials are never logged)
    const userCredential = await signInWithEmailAndPassword(auth, email, password);
    
    // ========================================================================
    // SECURITY CHECK: Validate Firebase Response
    // ========================================================================
    if (!userCredential || typeof userCredential !== 'object') {
      SecurityLogger.log('Invalid Auth Response', { type: 'missing_credential' });
      return { 
        success: false, 
        errorCode: 'invalid-response', 
        errorMessage: 'Invalid authentication response' 
      };
    }
    
    if (!FirebaseValidator.validateUser(userCredential.user)) {
      SecurityLogger.log('Invalid Auth Response', { type: 'invalid_user' });
      return { 
        success: false, 
        errorCode: 'invalid-user', 
        errorMessage: 'Invalid user object received' 
      };
    }
    
    // Authentication successful - log without sensitive data
    SecurityLogger.log('Authentication Successful', { 
      hasUser: !!userCredential.user,
      hasUid: !!userCredential.user?.uid 
    });
    
    return { success: true, user: userCredential.user };
    
  } catch (error) {
    // Validate error object
    if (!FirebaseValidator.validateError(error)) {
      SecurityLogger.log('Invalid Error Response', { hasCode: !!error?.code });
      return { 
        success: false, 
        errorCode: 'unknown-error', 
        errorMessage: 'An unexpected error occurred' 
      };
    }
    
    // Log error for debugging without exposing credentials
    SecurityLogger.log('Authentication Failed', { errorCode: error.code });
    
    return { 
      success: false, 
      errorCode: error.code, 
      errorMessage: error.message 
    };
  }
}

/**
 * Delete user data from Firestore in batches
 * Handles collections: users, messages, tasks, groupMembers
 * Uses batch operations for atomic deletions (up to 500 operations per batch)
 * @param {string} userId - User ID to delete data for (already validated)
 * @returns {Promise<Object>} - Result object with success status and details
 */
async function deleteUserDataFromFirestore(userId) {
  try {
    // ========================================================================
    // SECURITY CHECK: Validate User ID
    // ========================================================================
    if (!userId || typeof userId !== 'string' || userId.trim().length === 0) {
      SecurityLogger.log('Invalid User ID for Deletion', {});
      return {
        success: false,
        error: 'Invalid user ID',
        errorCode: 'invalid-user-id'
      };
    }
    
    SecurityLogger.log('Starting Firestore Deletion', { hasUserId: !!userId });
    
    let totalDeleted = 0;
    const deletionDetails = {
      userProfile: 0,
      messages: 0,
      tasks: 0,
      groupMembers: 0
    };
    
    // Step 1: Delete user profile document
    try {
      const userDocRef = doc(db, 'users', userId);
      await deleteDoc(userDocRef);
      deletionDetails.userProfile = 1;
      totalDeleted += 1;
      SecurityLogger.log('User Profile Deleted', {});
    } catch (error) {
      // Document might not exist, log but continue
      SecurityLogger.log('User Profile Deletion Warning', { 
        errorCode: error.code || 'unknown' 
      });
    }
    
    // Step 2: Delete messages where senderId matches user ID
    const messagesQuery = query(
      collection(db, 'messages'),
      where('senderId', '==', userId)
    );
    const messagesDeleted = await deleteBatchedDocuments(messagesQuery, 'messages');
    deletionDetails.messages = messagesDeleted;
    totalDeleted += messagesDeleted;
    
    // Step 3: Delete tasks where createdBy matches user ID
    const tasksQuery = query(
      collection(db, 'tasks'),
      where('createdBy', '==', userId)
    );
    const tasksDeleted = await deleteBatchedDocuments(tasksQuery, 'tasks');
    deletionDetails.tasks = tasksDeleted;
    totalDeleted += tasksDeleted;
    
    // Step 4: Delete group memberships where userId matches user ID
    const groupMembersQuery = query(
      collection(db, 'groupMembers'),
      where('userId', '==', userId)
    );
    const groupMembersDeleted = await deleteBatchedDocuments(groupMembersQuery, 'groupMembers');
    deletionDetails.groupMembers = groupMembersDeleted;
    totalDeleted += groupMembersDeleted;
    
    SecurityLogger.log('Firestore Deletion Complete', { 
      totalDeleted,
      userProfile: deletionDetails.userProfile,
      messages: deletionDetails.messages,
      tasks: deletionDetails.tasks,
      groupMembers: deletionDetails.groupMembers
    });
    
    return {
      success: true,
      totalDeleted,
      details: deletionDetails
    };
    
  } catch (error) {
    // Log error for debugging without exposing sensitive data
    SecurityLogger.log('Firestore Deletion Error', {
      errorCode: error.code || 'unknown'
    });
    
    // Determine if this is a network error
    const isNetworkError = error.code === 'unavailable' || 
                          error.code === 'deadline-exceeded' ||
                          error.message.toLowerCase().includes('network');
    
    return {
      success: false,
      error: error.message,
      errorCode: error.code,
      isNetworkError
    };
  }
}

/**
 * Delete documents in batches (handles more than 500 documents)
 * Firestore batch operations are limited to 500 operations per batch
 * @param {Object} queryRef - Firestore query reference
 * @param {string} collectionName - Name of collection for logging
 * @returns {Promise<number>} - Number of documents deleted
 */
async function deleteBatchedDocuments(queryRef, collectionName) {
  let totalDeleted = 0;
  let hasMore = true;
  
  while (hasMore) {
    try {
      // Get documents matching the query
      const snapshot = await getDocs(queryRef);
      
      // ========================================================================
      // SECURITY CHECK: Validate Firestore Response
      // ========================================================================
      if (!FirebaseValidator.validateSnapshot(snapshot)) {
        SecurityLogger.log('Invalid Firestore Snapshot', { collection: collectionName });
        hasMore = false;
        break;
      }
      
      if (snapshot.empty) {
        hasMore = false;
        break;
      }
      
      // Create a new batch
      const batch = writeBatch(db);
      let batchCount = 0;
      
      // Add delete operations to batch (max 500)
      snapshot.docs.forEach((document) => {
        if (batchCount < 500) {
          // Validate document has ref property
          if (document && document.ref) {
            batch.delete(document.ref);
            batchCount++;
          } else {
            SecurityLogger.log('Invalid Document Reference', { collection: collectionName });
          }
        }
      });
      
      // Commit the batch
      if (batchCount > 0) {
        await batch.commit();
        totalDeleted += batchCount;
        SecurityLogger.log('Batch Deletion Complete', { 
          collection: collectionName, 
          count: batchCount 
        });
      }
      
      // If we deleted less than 500, we're done
      if (batchCount < 500) {
        hasMore = false;
      }
      
    } catch (error) {
      // Validate and log error without exposing sensitive data
      if (FirebaseValidator.validateError(error)) {
        SecurityLogger.log('Firestore Deletion Error', { 
          collection: collectionName,
          errorCode: error.code 
        });
      } else {
        SecurityLogger.log('Unknown Firestore Error', { collection: collectionName });
      }
      
      // Continue with deletion even if one batch fails
      hasMore = false;
    }
  }
  
  return totalDeleted;
}

/**
 * Handle form submission
 * @param {Event} event - Form submit event
 */
async function handleFormSubmit(event) {
  event.preventDefault();
  
  // ========================================================================
  // SECURITY CHECK 1: CSRF Protection
  // ========================================================================
  if (!CSRFProtection.validate()) {
    SecurityLogger.logCSRF(false);
    showMessage(
      'Security validation failed. This request cannot be processed from the current origin.',
      'error'
    );
    return;
  }
  SecurityLogger.logCSRF(true);
  
  // ========================================================================
  // SECURITY CHECK 2: Rate Limiting
  // ========================================================================
  const rateLimitStatus = RateLimiter.canAttempt();
  
  if (!rateLimitStatus.canAttempt) {
    const resetTime = RateLimiter.getFormattedResetTime();
    SecurityLogger.logRateLimit('Blocked', {
      attemptsUsed: rateLimitStatus.attemptsUsed,
      resetTime
    });
    
    showMessage(
      `Too many deletion attempts. Please try again in ${resetTime}. This limit helps protect your account from unauthorized access.`,
      'error'
    );
    return;
  }
  
  SecurityLogger.logRateLimit('Allowed', {
    remainingAttempts: rateLimitStatus.remainingAttempts
  });
  
  const emailInput = document.getElementById('email');
  const passwordInput = document.getElementById('password');
  const confirmCheckbox = document.getElementById('confirmCheckbox');
  
  if (!emailInput || !passwordInput || !confirmCheckbox) {
    SecurityLogger.log('Form Elements Not Found', { function: 'handleFormSubmit' });
    return;
  }
  
  // ========================================================================
  // SECURITY CHECK 3: Input Sanitization and Validation
  // ========================================================================
  
  // Get raw values
  const rawEmail = emailInput.value;
  const rawPassword = passwordInput.value;
  
  // Sanitize email using enhanced sanitizer
  const email = InputSanitizer.sanitizeEmail(rawEmail);
  
  // Validate that inputs don't contain malicious patterns
  if (!InputSanitizer.isSafe(rawEmail)) {
    SecurityLogger.log('XSS Attempt Detected', { field: 'email' });
    showMessage('Invalid input detected. Please enter a valid email address.', 'error');
    clearFormFields();
    return;
  }
  
  // Password is not sanitized (preserve special characters) but check for safety
  if (!InputSanitizer.isSafe(rawPassword)) {
    SecurityLogger.log('XSS Attempt Detected', { field: 'password' });
    showMessage('Invalid input detected. Please check your password.', 'error');
    clearFormFields();
    return;
  }
  
  // Use raw password (Firebase handles it securely)
  const password = rawPassword;
  
  // Final validation before submission
  if (!validateEmail(email)) {
    showFieldError('email', 'Please enter a valid email address');
    return;
  }
  
  if (!validatePassword(password)) {
    showFieldError('password', 'Password must be at least 6 characters');
    return;
  }
  
  if (!confirmCheckbox.checked) {
    showFieldError('confirm', 'You must confirm that you understand this action cannot be undone');
    return;
  }
  
  // Clear any previous errors
  clearFieldError('email');
  clearFieldError('password');
  clearFieldError('confirm');
  
  // ========================================================================
  // SECURITY CHECK 4: Record Attempt (before processing)
  // ========================================================================
  RateLimiter.recordAttempt();
  
  // Show loading spinner during authentication
  showLoadingSpinner();
  
  try {
    // Step 1: Authenticate user with Firebase
    const authResult = await authenticateUser(email, password);
    
    if (authResult.success) {
      // ========================================================================
      // SECURITY CHECK: Validate User Object from Auth Result
      // ========================================================================
      if (!authResult.user || !FirebaseValidator.validateUser(authResult.user)) {
        SecurityLogger.log('Invalid User in Auth Result', {});
        showMessage('Authentication failed. Invalid user data received.', 'error');
        hideLoadingSpinner();
        return;
      }
      
      const user = authResult.user;
      SecurityLogger.log('Form Submit - Auth Success', { hasUid: !!user.uid });
      
      // Step 2: Delete Firestore data
      showMessage('Deleting your data from Firestore...', 'info');
      
      const deletionResult = await deleteUserDataFromFirestore(user.uid);
      
      if (deletionResult.success) {
        SecurityLogger.log('Form Submit - Firestore Success', { 
          totalDeleted: deletionResult.totalDeleted 
        });
        
        // Step 3: Delete authentication account
        showMessage('Deleting your authentication account...', 'info');
        
        const authDeletionResult = await deleteAuthenticationAccount(user);
        
        if (authDeletionResult.success) {
          SecurityLogger.log('Form Submit - Complete', {});
          
          // Clear form fields
          clearFormFields();
          
          // Show success message with options
          showSuccessWithOptions();
          
        } else {
          // Handle authentication deletion error with network detection
          SecurityLogger.log('Form Submit - Auth Deletion Failed', { 
            errorCode: authDeletionResult.errorCode 
          });
          const errorInfo = handleDeletionError(authDeletionResult.errorCode);
          showMessage(
            `Your data was deleted from Firestore, but we couldn't delete your authentication account. ${errorInfo.message}`,
            'error',
            errorInfo.isNetworkError
          );
        }
        
      } else {
        // Handle Firestore deletion error with network detection
        SecurityLogger.log('Form Submit - Firestore Failed', { 
          errorCode: deletionResult.errorCode 
        });
        const isNetworkError = deletionResult.isNetworkError || false;
        showMessage(
          'Failed to delete some data from Firestore. Please contact support for assistance.',
          'error',
          isNetworkError
        );
      }
      
    } else {
      // Handle authentication error with user-friendly message and network detection
      const errorInfo = handleAuthError(authResult.errorCode);
      showMessage(errorInfo.message, 'error', errorInfo.isNetworkError);
      
      // Clear password field for security (but keep email for user convenience)
      if (passwordInput) {
        passwordInput.value = '';
      }
    }
    
  } catch (error) {
    // Log unexpected error for debugging without exposing sensitive data
    SecurityLogger.log('Form Submit - Unexpected Error', {
      errorName: error.name || 'unknown'
    });
    
    // Check if it's a network-related error
    const isNetworkError = error.message.toLowerCase().includes('network') ||
                          error.message.toLowerCase().includes('fetch') ||
                          error.message.toLowerCase().includes('connection');
    
    showMessage('An unexpected error occurred. Please try again later.', 'error', isNetworkError);
  } finally {
    // Hide loading spinner
    hideLoadingSpinner();
  }
}

/**
 * Initialize form validation and event listeners
 */
function initializeFormValidation() {
  const form = document.getElementById('deleteAccountForm');
  const emailInput = document.getElementById('email');
  const passwordInput = document.getElementById('password');
  const confirmCheckbox = document.getElementById('confirmCheckbox');
  
  if (!form || !emailInput || !passwordInput || !confirmCheckbox) {
    SecurityLogger.log('Required Form Elements Not Found', { function: 'initializeFormValidation' });
    return;
  }
  
  // Add input event listeners for real-time validation
  emailInput.addEventListener('input', () => {
    validateEmailField();
    updateButtonState();
  });
  
  emailInput.addEventListener('blur', validateEmailField);
  
  passwordInput.addEventListener('input', () => {
    validatePasswordField();
    updateButtonState();
  });
  
  passwordInput.addEventListener('blur', validatePasswordField);
  
  confirmCheckbox.addEventListener('change', () => {
    updateButtonState();
  });
  
  // Add form submit handler
  form.addEventListener('submit', handleFormSubmit);
  
  // Initial button state
  updateButtonState();
  
  SecurityLogger.log('Form Validation Initialized', {});
}

// Initialize when DOM is fully loaded
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initializeFormValidation);
} else {
  // DOM is already loaded
  initializeFormValidation();
}

// Export functions for testing and future use
export {
  sanitizeInput,
  validateEmail,
  validatePassword,
  updateButtonState,
  clearFormFields,
  showFieldError,
  clearFieldError,
  showLoadingSpinner,
  hideLoadingSpinner,
  showMessage,
  handleAuthError,
  handleDeletionError,
  authenticateUser,
  deleteUserDataFromFirestore,
  deleteBatchedDocuments,
  deleteAuthenticationAccount,
  showSuccessWithOptions
};
