package id.jagakeluarga.salesfunnel.backup

import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** Portable AES-GCM envelope for manual backups. The user's app PIN is never stored in the backup. */
object BackupCrypto {
    private val magic = "SFA-BACKUP-1".toByteArray(Charsets.US_ASCII)
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 12
    private const val KEY_BITS = 256
    private const val ITERATIONS = 120_000

    fun isEncrypted(data: ByteArray): Boolean = data.size > magic.size && data.copyOfRange(0, magic.size).contentEquals(magic)

    fun encrypt(plain: ByteArray, pin: String): ByteArray {
        require(pin.length >= 4 && pin.all(Char::isDigit)) { "PIN backup harus terdiri dari minimal 4 digit" }
        val salt = ByteArray(SALT_SIZE).also(SecureRandom()::nextBytes)
        val iv = ByteArray(IV_SIZE).also(SecureRandom()::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(pin, salt), GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(plain)
        return ByteArrayOutputStream().apply {
            write(magic)
            write(salt)
            write(iv)
            write(encrypted)
        }.toByteArray()
    }

    fun decrypt(data: ByteArray, pin: String): ByteArray {
        require(isEncrypted(data)) { "Format backup tidak dikenali" }
        val offset = magic.size
        val salt = data.copyOfRange(offset, offset + SALT_SIZE)
        val ivStart = offset + SALT_SIZE
        val iv = data.copyOfRange(ivStart, ivStart + IV_SIZE)
        val encrypted = data.copyOfRange(ivStart + IV_SIZE, data.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, deriveKey(pin, salt), GCMParameterSpec(128, iv))
        return try {
            cipher.doFinal(encrypted)
        } catch (_: Exception) {
            error("PIN backup salah atau file backup rusak")
        }
    }

    private fun deriveKey(pin: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_BITS)
        return try {
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            SecretKeySpec(bytes, "AES")
        } finally {
            spec.clearPassword()
        }
    }
}
