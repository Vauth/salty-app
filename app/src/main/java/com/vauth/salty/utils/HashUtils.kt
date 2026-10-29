package com.vauth.salty.utils

import android.util.Base64
import com.vauth.salty.BuildConfig
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object HashUtils {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_ALGORITHM = "AES"
    private const val SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATION_COUNT = 600000 
    private const val KEY_LENGTH = 256
    private const val KDF_SALT_LENGTH = 16
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val GCM_TAG_LENGTH_BYTES = GCM_TAG_LENGTH_BITS / 8
    private const val CURRENT_ENVELOPE_PREFIX = "salty:"
    private const val CURRENT_ENVELOPE_VERSION = "1"
    private const val ENVELOPE_DELIMITER = ':'

    fun encode(message: String, salt: String): String {
        try {
            val kdfSalt = ByteArray(KDF_SALT_LENGTH)
            val iv = ByteArray(GCM_IV_LENGTH)
            SecureRandom().apply {
                nextBytes(kdfSalt)
                nextBytes(iv)
            }
            
            val key = deriveCurrentKey(salt, kdfSalt)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            
            val encryptedBytes = cipher.doFinal(message.toByteArray(StandardCharsets.UTF_8))
            return listOf(
                CURRENT_ENVELOPE_PREFIX.dropLast(1),
                CURRENT_ENVELOPE_VERSION,
                Base64.encodeToString(kdfSalt, Base64.NO_WRAP),
                Base64.encodeToString(iv, Base64.NO_WRAP),
                Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            ).joinToString(ENVELOPE_DELIMITER.toString())
        } catch (e: Exception) {
            throw EncodingException("Failed to encode message: ${e.message}", e)
        }
    }

    fun decode(encodedMessage: String, salt: String): String {
        try {
            return if (encodedMessage.startsWith(CURRENT_ENVELOPE_PREFIX)) {
                decodeCurrentEnvelope(encodedMessage, salt)
            } else {
                decodeLegacyEnvelope(encodedMessage, salt)
            }
        } catch (e: Exception) {
            throw DecodingException("Failed to decode message: ${e.message}", e)
        }
    }
    
    fun generateHash(message: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(message.toByteArray(StandardCharsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    private fun decodeCurrentEnvelope(encodedMessage: String, userSalt: String): String {
        val envelope = parseCurrentEnvelope(encodedMessage)
        val key = deriveCurrentKey(userSalt, envelope.kdfSalt)
        return decrypt(envelope.encryptedBytes, key, envelope.iv)
    }

    private fun decodeLegacyEnvelope(encodedMessage: String, userSalt: String): String {
        val combined = Base64.decode(encodedMessage, Base64.NO_WRAP)
        if (combined.size <= GCM_IV_LENGTH) {
            throw DecodingException("Encoded message is too short")
        }

        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val encryptedBytes = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
        return decrypt(encryptedBytes, deriveLegacyKey(userSalt), iv)
    }

    private fun decrypt(encryptedBytes: ByteArray, key: SecretKeySpec, iv: ByteArray): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }

    private fun parseCurrentEnvelope(encodedMessage: String): CurrentEnvelope {
        val parts = encodedMessage.split(ENVELOPE_DELIMITER)
        if (parts.size != 5 ||
            parts[0] != CURRENT_ENVELOPE_PREFIX.dropLast(1) ||
            parts[1] != CURRENT_ENVELOPE_VERSION ||
            parts[2].isEmpty() ||
            parts[3].isEmpty() ||
            parts[4].isEmpty()
        ) {
            throw DecodingException("Malformed or unsupported current encoded message")
        }

        val kdfSalt = Base64.decode(parts[2], Base64.NO_WRAP)
        val iv = Base64.decode(parts[3], Base64.NO_WRAP)
        val encryptedBytes = Base64.decode(parts[4], Base64.NO_WRAP)
        if (kdfSalt.size != KDF_SALT_LENGTH ||
            iv.size != GCM_IV_LENGTH ||
            encryptedBytes.size < GCM_TAG_LENGTH_BYTES
        ) {
            throw DecodingException("Malformed current encoded message")
        }

        return CurrentEnvelope(kdfSalt, iv, encryptedBytes)
    }

    private fun deriveCurrentKey(userSalt: String, kdfSalt: ByteArray): SecretKeySpec {
        val password = (userSalt + '\u0000' + BuildConfig.APP_SALT).toCharArray()
        return deriveKey(password, kdfSalt)
    }

    private fun deriveLegacyKey(userSalt: String): SecretKeySpec {
        val appSalt = BuildConfig.APP_SALT.toByteArray(StandardCharsets.UTF_8)
        val legacyKdfSalt = appSalt + userSalt.toByteArray(StandardCharsets.UTF_8)
        return deriveKey(userSalt.toCharArray(), legacyKdfSalt)
    }
        
    private fun deriveKey(password: CharArray, kdfSalt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, kdfSalt, ITERATION_COUNT, KEY_LENGTH)
        return try {
            val factory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITHM)
            val keyBytes = factory.generateSecret(spec).encoded
            try {
                SecretKeySpec(keyBytes, KEY_ALGORITHM)
            } finally {
                keyBytes.fill(0)
            }
        } finally {
            spec.clearPassword()
            password.fill('\u0000')
        }
    }
    
    fun isValidEncodedMessage(encodedMessage: String): Boolean {
        return try {
            if (encodedMessage.startsWith(CURRENT_ENVELOPE_PREFIX)) {
                parseCurrentEnvelope(encodedMessage)
                true
            } else {
                Base64.decode(encodedMessage, Base64.NO_WRAP).size > GCM_IV_LENGTH
            }
        } catch (e: Exception) {
            false
        }
    }

    private data class CurrentEnvelope(
        val kdfSalt: ByteArray,
        val iv: ByteArray,
        val encryptedBytes: ByteArray
    )
}

class EncodingException(message: String, cause: Throwable? = null) : Exception(message, cause)
class DecodingException(message: String, cause: Throwable? = null) : Exception(message, cause)
