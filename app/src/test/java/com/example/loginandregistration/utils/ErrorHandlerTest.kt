package com.example.loginandregistration.utils

import com.example.loginandregistration.validation.ValidationException
import com.example.loginandregistration.validation.ValidationResult
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import org.junit.Assert.*
import org.junit.Test

/** Unit tests for ErrorHandler Tests error categorization and user-friendly message generation */
class ErrorHandlerTest {

    // ========== Error Categorization Tests ==========

    @Test
    fun `categorizeError with ValidationException should return Validation error`() {
        val validationResult = ValidationResult.failure("Field is required", "Invalid format")
        val exception = ValidationException(validationResult)

        val error = ErrorHandler.categorizeError(exception)

        assertTrue("Should be Validation error", error is ErrorHandler.AppError.Validation)
        val validationError = error as ErrorHandler.AppError.Validation
        assertEquals("Should have 2 errors", 2, validationError.fields.size)
    }

    @Test
    fun `categorizeError with UnknownHostException should return Network error`() {
        val exception = UnknownHostException("Unable to resolve host")

        val error = ErrorHandler.categorizeError(exception)

        assertTrue("Should be Network error", error is ErrorHandler.AppError.Network)
        val networkError = error as ErrorHandler.AppError.Network
        assertTrue(
                "Should mention connection",
                networkError.cause.contains("failed", ignoreCase = true)
        )
    }

    @Test
    fun `categorizeError with SocketTimeoutException should return Network error`() {
        val exception = SocketTimeoutException("Connection timed out")

        val error = ErrorHandler.categorizeError(exception)

        assertTrue("Should be Network error", error is ErrorHandler.AppError.Network)
    }

    @Test
    fun `categorizeError with IOException should return Network error`() {
        val exception = IOException("Network error")

        val error = ErrorHandler.categorizeError(exception)

        assertTrue("Should be Network error", error is ErrorHandler.AppError.Network)
    }

    @Test
    fun `categorizeError with FirebaseNetworkException should return Network error`() {
        val exception = FirebaseNetworkException("Firebase network error")

        val error = ErrorHandler.categorizeError(exception)

        assertTrue("Should be Network error", error is ErrorHandler.AppError.Network)
    }

    @Test
    fun `categorizeError with PERMISSION_DENIED should return Permission error`() {
        val exception =
                FirebaseFirestoreException(
                        "Permission denied",
                        FirebaseFirestoreException.Code.PERMISSION_DENIED
                )

        val error = ErrorHandler.categorizeError(exception)

        assertTrue("Should be Permission error", error is ErrorHandler.AppError.Permission)
        val permissionError = error as ErrorHandler.AppError.Permission
        assertTrue(
                "Should mention access",
                permissionError.operation.contains("access", ignoreCase = true)
        )
    }

    @Test
    fun `categorizeError with UNAVAILABLE should return Network error`() {
        val exception =
                FirebaseFirestoreException(
                        "Service unavailable",
                        FirebaseFirestoreException.Code.UNAVAILABLE
                )

        val error = ErrorHandler.categorizeError(exception)

        assertTrue("Should be Network error", error is ErrorHandler.AppError.Network)
    }

    @Test
    fun `categorizeError with DEADLINE_EXCEEDED should return Network error`() {
        val exception =
                FirebaseFirestoreException(
                        "Deadline exceeded",
                        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED
                )

        val error = ErrorHandler.categorizeError(exception)

        assertTrue("Should be Network error", error is ErrorHandler.AppError.Network)
    }

    @Test
    fun `categorizeError with UNAUTHENTICATED should return Permission error`() {
        val exception =
                FirebaseFirestoreException(
                        "Unauthenticated",
                        FirebaseFirestoreException.Code.UNAUTHENTICATED
                )

        val error = ErrorHandler.categorizeError(exception)

        assertTrue("Should be Permission error", error is ErrorHandler.AppError.Permission)
    }

    @Test
    fun `categorizeError with NOT_FOUND should return NotFound error`() {
        val exception =
                FirebaseFirestoreException(
                        "Document not found",
                        FirebaseFirestoreException.Code.NOT_FOUND
                )

        val error = ErrorHandler.categorizeError(exception)

        assertTrue("Should be NotFound error", error is ErrorHandler.AppError.NotFound)
    }

    @Test
    fun `categorizeError with FirebaseAuthException should categorize by error code`() {
        val exception = FirebaseAuthException("ERROR_INVALID_EMAIL", "Invalid email")

        val error = ErrorHandler.categorizeError(exception)

        assertTrue("Should be Validation error", error is ErrorHandler.AppError.Validation)
    }

    @Test
    fun `categorizeError with StorageException should categorize by error code`() {
        val exception =
                StorageException(StorageException.ERROR_OBJECT_NOT_FOUND, "Object not found", null)

        val error = ErrorHandler.categorizeError(exception)

        assertTrue("Should be NotFound error", error is ErrorHandler.AppError.NotFound)
    }

    @Test
    fun `categorizeError with SecurityException should return Permission error`() {
        val exception = SecurityException("Permission denied")

        val error = ErrorHandler.categorizeError(exception)

        assertTrue("Should be Permission error", error is ErrorHandler.AppError.Permission)
    }

    @Test
    fun `categorizeError with unknown exception should return Unknown error`() {
        val exception = RuntimeException("Something went wrong")

        val error = ErrorHandler.categorizeError(exception)

        assertTrue("Should be Unknown error", error is ErrorHandler.AppError.Unknown)
    }

    // ========== User-Friendly Message Tests ==========

    @Test
    fun `getUserFriendlyError for Permission error should suggest logout`() {
        val error = ErrorHandler.AppError.Permission("access this data")

        val userError = ErrorHandler.getUserFriendlyError(error)

        assertEquals("Should suggest logout", ErrorHandler.ErrorAction.LOGOUT, userError.action)
        assertTrue(
                "Should mention permission",
                userError.message.contains("permission", ignoreCase = true)
        )
        assertTrue("Should suggest logout", userError.message.contains("log", ignoreCase = true))
    }

    @Test
    fun `getUserFriendlyError for Network error should suggest retry`() {
        val error = ErrorHandler.AppError.Network("Connection failed")

        val userError = ErrorHandler.getUserFriendlyError(error)

        assertEquals("Should suggest retry", ErrorHandler.ErrorAction.RETRY, userError.action)
        assertTrue(
                "Should mention network",
                userError.message.contains("network", ignoreCase = true) ||
                        userError.message.contains("connection", ignoreCase = true)
        )
    }

    @Test
    fun `getUserFriendlyError for Validation error should list fields`() {
        val error = ErrorHandler.AppError.Validation(listOf("email", "password"))

        val userError = ErrorHandler.getUserFriendlyError(error)

        assertEquals("Should dismiss", ErrorHandler.ErrorAction.DISMISS, userError.action)
        assertTrue("Should mention email", userError.message.contains("email"))
        assertTrue("Should mention password", userError.message.contains("password"))
    }

    @Test
    fun `getUserFriendlyError for NotFound error should be informative`() {
        val error = ErrorHandler.AppError.NotFound("user account")

        val userError = ErrorHandler.getUserFriendlyError(error)

        assertEquals("Should dismiss", ErrorHandler.ErrorAction.DISMISS, userError.action)
        assertTrue("Should mention resource", userError.message.contains("user account"))
        assertTrue(
                "Should mention not found",
                userError.message.contains("not found", ignoreCase = true)
        )
    }

    @Test
    fun `getUserFriendlyError for Unknown error should suggest retry`() {
        val error = ErrorHandler.AppError.Unknown("Unexpected error")

        val userError = ErrorHandler.getUserFriendlyError(error)

        assertEquals("Should suggest retry", ErrorHandler.ErrorAction.RETRY, userError.action)
        assertTrue("Should be user-friendly", userError.message.length > 0)
    }

    // ========== Message Quality Tests ==========

    @Test
    fun `user-friendly messages should not contain technical jargon`() {
        val errors =
                listOf(
                        ErrorHandler.AppError.Permission("access data"),
                        ErrorHandler.AppError.Network("Connection failed"),
                        ErrorHandler.AppError.Validation(listOf("field")),
                        ErrorHandler.AppError.NotFound("resource"),
                        ErrorHandler.AppError.Unknown("Error occurred")
                )

        errors.forEach { error ->
            val userError = ErrorHandler.getUserFriendlyError(error)

            assertFalse("Should not contain 'Exception'", userError.message.contains("Exception"))
            assertFalse("Should not contain 'null'", userError.message.contains("null"))
            assertFalse("Should not contain stack trace", userError.message.contains("at com."))
            assertTrue("Should be descriptive", userError.message.length > 10)
        }
    }

    @Test
    fun `user-friendly messages should provide actionable guidance`() {
        val permissionError = ErrorHandler.AppError.Permission("access data")
        val networkError = ErrorHandler.AppError.Network("Connection failed")

        val permissionMessage = ErrorHandler.getUserFriendlyError(permissionError)
        val networkMessage = ErrorHandler.getUserFriendlyError(networkError)

        assertTrue(
                "Permission error should suggest action",
                permissionMessage.message.contains("log", ignoreCase = true) ||
                        permissionMessage.message.contains("sign", ignoreCase = true)
        )

        assertTrue(
                "Network error should suggest action",
                networkMessage.message.contains("check", ignoreCase = true) ||
                        networkMessage.message.contains("connection", ignoreCase = true)
        )
    }

    @Test
    fun `error messages should have appropriate titles`() {
        val errors =
                listOf(
                        ErrorHandler.AppError.Permission("access data"),
                        ErrorHandler.AppError.Network("Connection failed"),
                        ErrorHandler.AppError.Validation(listOf("field")),
                        ErrorHandler.AppError.NotFound("resource")
                )

        errors.forEach { error ->
            val userError = ErrorHandler.getUserFriendlyError(error)

            assertNotNull("Should have title", userError.title)
            assertTrue("Title should not be empty", userError.title.isNotEmpty())
            assertFalse("Title should not be too long", userError.title.length > 50)
        }
    }

    // ========== Error Recovery Tests ==========

    @Test
    fun `permission errors should suggest logout action`() {
        val exception =
                FirebaseFirestoreException(
                        "Permission denied",
                        FirebaseFirestoreException.Code.PERMISSION_DENIED
                )

        val error = ErrorHandler.categorizeError(exception)
        val userError = ErrorHandler.getUserFriendlyError(error)

        assertEquals("Should suggest logout", ErrorHandler.ErrorAction.LOGOUT, userError.action)
    }

    @Test
    fun `network errors should suggest retry action`() {
        val exception = UnknownHostException("Unable to resolve host")

        val error = ErrorHandler.categorizeError(exception)
        val userError = ErrorHandler.getUserFriendlyError(error)

        assertEquals("Should suggest retry", ErrorHandler.ErrorAction.RETRY, userError.action)
    }

    @Test
    fun `validation errors should suggest dismiss action`() {
        val validationResult = ValidationResult.failure("Invalid input")
        val exception = ValidationException(validationResult)

        val error = ErrorHandler.categorizeError(exception)
        val userError = ErrorHandler.getUserFriendlyError(error)

        assertEquals("Should suggest dismiss", ErrorHandler.ErrorAction.DISMISS, userError.action)
    }

    // ========== Edge Cases ==========

    @Test
    fun `categorizeError should handle null messages gracefully`() {
        val exception = RuntimeException(null as String?)

        val error = ErrorHandler.categorizeError(exception)

        assertTrue("Should be Unknown error", error is ErrorHandler.AppError.Unknown)
        val unknownError = error as ErrorHandler.AppError.Unknown
        assertTrue("Should have default message", unknownError.message.isNotEmpty())
    }

    @Test
    fun `categorizeError should preserve exception for logging`() {
        val originalException = IOException("Network error")

        val error = ErrorHandler.categorizeError(originalException)

        assertTrue("Should be Network error", error is ErrorHandler.AppError.Network)
        val networkError = error as ErrorHandler.AppError.Network
        assertNotNull("Should preserve exception", networkError.exception)
        assertEquals("Should be same exception", originalException, networkError.exception)
    }

    @Test
    fun `getUserFriendlyError should preserve exception for logging`() {
        val originalException = IOException("Network error")
        val error = ErrorHandler.AppError.Network("Connection failed", originalException)

        val userError = ErrorHandler.getUserFriendlyError(error)

        assertNotNull("Should preserve exception", userError.exception)
        assertEquals("Should be same exception", originalException, userError.exception)
    }
}
