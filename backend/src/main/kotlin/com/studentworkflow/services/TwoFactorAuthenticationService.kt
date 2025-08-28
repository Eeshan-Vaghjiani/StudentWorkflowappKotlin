package com.studentworkflow.services

class TwoFactorAuthenticationService {
    fun generateSecretKey(): String = "placeholder-secret"
    fun getCurrentOtp(secret: String): String = "123456"
    fun verify(secret: String, code: String): Boolean = code == "123456"
    fun qrCodeSvg(companyName: String, companyEmail: String, secret: String): String = "<img/>"
    fun enable(userId: Int, secret: String): Boolean = true
    fun disable(userId: Int): Boolean = true
    fun getDecryptedSecret(userId: Int): String? = "placeholder-secret"
}
