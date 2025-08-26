
package com.studentworkflow.services

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.studentworkflow.db.Users
import com.studentworkflow.utils.EncryptionUtil
import de.taimos.totp.TOTP
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.time.Instant
import java.util.Base64

class TwoFactorAuthenticationService {

    fun generateSecretKey(): String {
        return TOTP.generateBase32Secret()
    }

    fun generateEncryptedSecretKey(): String {
        val secret = TOTP.generateBase32Secret()
        return EncryptionUtil.encrypt(secret)
    }

    fun getCurrentOtp(secret: String): String {
        return TOTP.getOTP(secret)
    }

    fun verify(secret: String, code: String): Boolean {
        return TOTP.verify(secret, code)
    }

    fun verify(userId: Int, code: String): Boolean {
        val secret = getDecryptedSecret(userId)
        return if (secret != null) {
            TOTP.verify(secret, code)
        } else {
            false
        }
    }

    fun qrCodeUrl(companyName: String, companyEmail: String, secret: String): String {
        val encodedCompanyName = URLEncoder.encode(companyName, "UTF-8")
        val encodedCompanyEmail = URLEncoder.encode(companyEmail, "UTF-8")
        return "otpauth://totp/$encodedCompanyName:$encodedCompanyEmail?secret=$secret&issuer=$encodedCompanyName"
    }

    fun qrCodeUrl(userId: Int, secret: String): String {
        return transaction {
            val user = Users.select { Users.id eq userId }.singleOrNull()
            if (user != null) {
                val userEmail = user[Users.email]
                qrCodeUrl("StudentWorkflowApp", userEmail, secret)
            } else {
                qrCodeUrl("StudentWorkflowApp", "user@example.com", secret)
            }
        }
    }

    fun qrCodeSvg(companyName: String, companyEmail: String, secret: String): String {
        val qrCodeUrl = qrCodeUrl(companyName, companyEmail, secret)
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(qrCodeUrl, BarcodeFormat.QR_CODE, 200, 200)
        val outputStream = ByteArrayOutputStream()
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream)
        val base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray())
        return "<img src=\"data:image/png;base64,$base64Image\" alt=\"QR Code\"/>"
    }

    fun qrCodeSvg(userId: Int, secret: String): String {
        return transaction {
            val user = Users.select { Users.id eq userId }.singleOrNull()
            if (user != null) {
                val userEmail = user[Users.email]
                qrCodeSvg("StudentWorkflowApp", userEmail, secret)
            } else {
                qrCodeSvg("StudentWorkflowApp", "user@example.com", secret)
            }
        }
    }

    fun enable(userId: Int, secret: String): Boolean {
        return transaction {
            val encryptedSecret = EncryptionUtil.encrypt(secret)
            Users.update({ Users.id eq userId }) {
                it[twoFactorSecret] = encryptedSecret
                it[twoFactorConfirmedAt] = Instant.now()
            } > 0
        }
    }

    fun disable(userId: Int): Boolean {
        return transaction {
            Users.update({ Users.id eq userId }) {
                it[twoFactorSecret] = null
                it[twoFactorRecoveryCodes] = null
                it[twoFactorConfirmedAt] = null
            } > 0
        }
    }

    fun getDecryptedSecret(userId: Int): String? {
        return transaction {
            val user = Users.select { Users.id eq userId }.singleOrNull()
            user?.get(Users.twoFactorSecret)?.let { EncryptionUtil.decrypt(it) }
        }
    }
}
