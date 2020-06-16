package com.manimarank.spell4wiki.utils

import android.annotation.SuppressLint
import android.util.Base64
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


object CryptUtils {

    private const val ALGORITHM = "AES"
    private const val KEY = "@-YOUR_KEY_HERE-"  // Key value should be 16 characters

    @SuppressLint("GetInstance")
    fun encrypt(value: String): String {
        return try {
            val key: Key = generateKey()
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encryptedByteValue = cipher.doFinal(value.toByteArray(charset("utf-8")))
            Base64.encodeToString(encryptedByteValue, Base64.DEFAULT)
        }catch (e : Exception){
            e.printStackTrace()
            value
        }
    }

    @SuppressLint("GetInstance")
    fun decrypt(value: String): String {
        return try {
            val key: Key = generateKey()
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key)
            val decryptedValue64: ByteArray = Base64.decode(value, Base64.DEFAULT)
            val decryptedByteValue = cipher.doFinal(decryptedValue64)
            String(decryptedByteValue, charset("utf-8"))
        }catch (e : Exception){
            e.printStackTrace()
            value
        }

    }

    private fun generateKey(): Key {
        return SecretKeySpec(KEY.toByteArray(), ALGORITHM)
    }

}