package com.vauth.salty.utils

import android.util.Base64
import com.vauth.salty.BuildConfig
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


object HashUtils {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val LEGACY_ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_ALGORITHM = "AES"
    private const val SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATION_COUNT = 100000
    private const val KEY_LENGTH = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH_BITS = 128

    
    fun encode(message: String, salt: String): String {
        try {
            val key = deriveKey(salt)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(message.toByteArray(StandardCharsets.UTF_8))
            val combined = iv + encryptedBytes
            return Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw EncodingException("Failed to encode message: ${e.message}", e)
        }
    }

    fun decode(encodedMessage: String, salt: String): String {
        try {
            val combined = Base64.decode(encodedMessage, Base64.NO_WRAP)
            
            if (combined.size > GCM_IV_LENGTH) {
                try {
                    val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
                    val encryptedBytes = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
                    val key = deriveKey(salt)
                    val cipher = Cipher.getInstance(ALGORITHM)
                    cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
                    val decryptedBytes = cipher.doFinal(encryptedBytes)
                    return String(decryptedBytes, StandardCharsets.UTF_8)
                } catch (_: Exception) {
                }
            }

            if (combined.size <= 16) {
                throw DecodingException("Encoded message is too short")
            }

            val iv = combined.copyOfRange(0, 16)
            val encryptedBytes = combined.copyOfRange(16, combined.size)
            val key = deriveKey(salt)
            val cipher = Cipher.getInstance(LEGACY_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            throw DecodingException("Failed to decode message: ${e.message}", e)
        }
    }
    
    fun generateHash(message: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(message.toByteArray(StandardCharsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    private fun deriveKey(userSalt: String): SecretKeySpec {
        val appSalt = BuildConfig.APP_SALT.toByteArray(StandardCharsets.UTF_8)
        val combinedSalt = appSalt + userSalt.toByteArray(StandardCharsets.UTF_8)
        
        val spec = PBEKeySpec(
            userSalt.toCharArray(),
            combinedSalt,
            ITERATION_COUNT,
            KEY_LENGTH
        )
        val factory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITHM)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, KEY_ALGORITHM)
    }
    
    fun isValidEncodedMessage(encodedMessage: String): Boolean {
        return try {
            val decoded = Base64.decode(encodedMessage, Base64.NO_WRAP)
            decoded.size > GCM_IV_LENGTH
        } catch (e: Exception) {
            false
        }
    }
}

class EncodingException(message: String, cause: Throwable? = null) : Exception(message, cause)
class DecodingException(message: String, cause: Throwable? = null) : Exception(message, cause)
