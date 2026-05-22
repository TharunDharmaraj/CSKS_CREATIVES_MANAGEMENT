package com.example.csks_creatives.domain.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object SecurityUtils {
    // Note: In a real production app, the key and IV should be stored securely (e.g., Android Keystore)
    // and not hardcoded in the source code.
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private val key = SecretKeySpec("CSKS_SECRET_KEY_123".take(16).toByteArray(), "AES")
    private val iv = IvParameterSpec("CSKS_IV_678901234".take(16).toByteArray())

    /**
     * Encrypts a string using AES.
     */
    fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val encrypted = cipher.doFinal(value.toByteArray())
        return Base64.encodeToString(encrypted, Base64.DEFAULT).trim()
    }

    /**
     * Decrypts a string using AES.
     */
    fun decrypt(value: String): String {
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key, iv)
            val decodedBytes = Base64.decode(value, Base64.DEFAULT)
            val decrypted = cipher.doFinal(decodedBytes)
            String(decrypted)
        } catch (e: Exception) {
            // Return original if decryption fails (e.g., if it's already plain text or hashed)
            value
        }
    }
}
