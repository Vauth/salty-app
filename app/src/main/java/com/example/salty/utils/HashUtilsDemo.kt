package com.example.salty.utils

/**
 * Simple test/demo for HashUtils
 * 
 * ⚠️ FOR DEVELOPMENT AND TESTING PURPOSES ONLY ⚠️
 * This demo should NOT be included in production builds.
 * 
 * This demo demonstrates the core encryption/decryption functionality of Salty.
 * It verifies that:
 * - Messages can be encoded and decoded correctly
 * - Different salts produce different encrypted outputs
 * - Wrong salts fail to decode (security check)
 * - Random IVs ensure unique encrypted outputs even for identical inputs
 * - Validation works correctly
 * 
 * To run this demo during development:
 * 1. In MainActivity.onCreate(), add: HashUtilsDemo.runDemo()
 * 2. Check logcat output for test results
 * 3. Remove the call before building release versions
 * 
 * Expected output: Series of test results showing successful encryption/decryption
 * operations and security validations.
 */
object HashUtilsDemo {
    
    fun runDemo() {
        println("=== Salty Hash Utils Demo ===\n")
        
        // Test 1: Basic encoding and decoding
        println("Test 1: Basic Encoding/Decoding")
        val message = "Hello, this is a secret message!"
        val salt = "MySecretKey2024"
        
        try {
            val encoded = HashUtils.encode(message, salt)
            println("Original: $message")
            println("Salt/Key: $salt")
            println("Encoded: $encoded")
            
            val decoded = HashUtils.decode(encoded, salt)
            println("Decoded: $decoded")
            println("Match: ${message == decoded}")
            println()
        } catch (e: Exception) {
            println("Error: ${e.message}\n")
        }
        
        // Test 2: Different salts produce different outputs
        println("Test 2: Different Salts")
        val salt1 = "Key1"
        val salt2 = "Key2"
        
        try {
            val encoded1 = HashUtils.encode(message, salt1)
            val encoded2 = HashUtils.encode(message, salt2)
            println("Same message with different salts:")
            println("Salt1 result: ${encoded1.take(20)}...")
            println("Salt2 result: ${encoded2.take(20)}...")
            println("Different: ${encoded1 != encoded2}")
            println()
        } catch (e: Exception) {
            println("Error: ${e.message}\n")
        }
        
        // Test 3: Wrong salt fails to decode
        println("Test 3: Wrong Salt")
        try {
            val encoded = HashUtils.encode(message, "CorrectSalt")
            val decoded = HashUtils.decode(encoded, "WrongSalt")
            println("This shouldn't print: $decoded")
        } catch (e: Exception) {
            println("✓ Correctly failed to decode with wrong salt")
            println("Error: ${e.message}")
            println()
        }
        
        // Test 4: Same message encrypted twice gives different results (due to random IV)
        println("Test 4: Random IV")
        try {
            val encoded1 = HashUtils.encode(message, salt)
            val encoded2 = HashUtils.encode(message, salt)
            println("Same message encrypted twice:")
            println("Result1: ${encoded1.take(20)}...")
            println("Result2: ${encoded2.take(20)}...")
            println("Different (due to random IV): ${encoded1 != encoded2}")
            
            // But both should decode correctly
            val decoded1 = HashUtils.decode(encoded1, salt)
            val decoded2 = HashUtils.decode(encoded2, salt)
            println("Both decode correctly: ${decoded1 == message && decoded2 == message}")
            println()
        } catch (e: Exception) {
            println("Error: ${e.message}\n")
        }
        
        // Test 5: Validation
        println("Test 5: Validation")
        val validEncoded = HashUtils.encode("test", "key")
        println("Valid encoded message: ${HashUtils.isValidEncodedMessage(validEncoded)}")
        println("Invalid message: ${HashUtils.isValidEncodedMessage("not-valid-base64")}")
        println("Short message: ${HashUtils.isValidEncodedMessage("abc")}")
        
        println("\n=== Demo Complete ===")
    }
}

/**
 * Usage in MainActivity or any other file:
 * 
 * import com.example.salty.utils.HashUtilsDemo
 * 
 * // In onCreate or any function:
 * HashUtilsDemo.runDemo()
 */
