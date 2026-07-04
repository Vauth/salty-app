package com.vauth.salty.utils

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Utility class for encoding and decoding messages with salt/key
 * Uses AES-256 encryption with PBKDF2 key derivation
 */
object HashUtils {
    
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_ALGORITHM = "AES"
    private const val SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATION_COUNT = 600000
    private const val KEY_LENGTH = 256
    
    /**
     * Encodes a message using the provided salt/key
     * @param message The original message to encode
     * @param salt The salt/key used for encoding
     * @return Base64 encoded string containing IV + encrypted message
     */
    fun encode(message: String, salt: String): String {
        try {
            // Derive key from salt using PBKDF2
            val key = deriveKey(salt)
            
            // Create cipher and encrypt
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            
            // Get IV (initialization vector)
            val iv = cipher.iv
            
            // Encrypt the message
            val encryptedBytes = cipher.doFinal(message.toByteArray(StandardCharsets.UTF_8))
            
            // Combine IV + encrypted data
            val combined = iv + encryptedBytes
            
            // Return Base64 encoded result
            return Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw EncodingException("Failed to encode message: ${e.message}", e)
        }
    }
    
    /**
     * Decodes an encoded message using the provided salt/key
     * @param encodedMessage The Base64 encoded message
     * @param salt The salt/key used for decoding (must match encoding salt)
     * @return The decoded original message
     */
    fun decode(encodedMessage: String, salt: String): String {
        try {
            // Decode from Base64
            val combined = Base64.decode(encodedMessage, Base64.NO_WRAP)
            
            // Extract IV (first 16 bytes)
            val iv = combined.copyOfRange(0, 16)
            val encryptedBytes = combined.copyOfRange(16, combined.size)
            
            // Derive key from salt
            val key = deriveKey(salt)
            
            // Create cipher and decrypt
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
            
            // Decrypt the message
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            
            // Return decoded message
            return String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            throw DecodingException("Failed to decode message: ${e.message}", e)
        }
    }
    
    /**
     * Generates a hash of the message for display/verification purposes
     * @param message The message to hash
     * @return SHA-256 hash as hex string
     */
    fun generateHash(message: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(message.toByteArray(StandardCharsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Derives an AES key from a user's salt/key using PBKDF2
     * Uses a fixed application salt combined with user's salt for better security
     * 
     * Note: The fixed app salt is visible in code but provides defense-in-depth.
     * For enhanced security in production apps, consider:
     * - Storing salt in native code (NDK)
     * - Using BuildConfig to inject salt at build time
     * - Implementing additional key stretching
     */
    private fun deriveKey(userSalt: String): SecretKeySpec {
        // Use a fixed application salt to prevent rainbow table attacks
        val appSalt = "Salty-Fixed-Salt".toByteArray(StandardCharsets.UTF_8)
        
        // Combine user salt with app salt for the PBKDF2 salt parameter
        val combinedSalt = appSalt + userSalt.toByteArray(StandardCharsets.UTF_8)
        
        val spec = PBEKeySpec(
            userSalt.toCharArray(),  // User's salt as password
            combinedSalt,             // Combined salt for PBKDF2
            ITERATION_COUNT,
            KEY_LENGTH
        )
        val factory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITHM)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, KEY_ALGORITHM)
    }
    
    /**
     * Validates if a string is a valid Base64 encoded message
     */
    fun isValidEncodedMessage(encodedMessage: String): Boolean {
        return try {
            val decoded = Base64.decode(encodedMessage, Base64.NO_WRAP)
            decoded.size > 16 // Must have at least IV (16 bytes) + some data
        } catch (e: Exception) {
            false
        }
    }
}

class EncodingException(message: String, cause: Throwable? = null) : Exception(message, cause)
class DecodingException(message: String, cause: Throwable? = null) : Exception(message, cause)
