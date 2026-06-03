package com.github.huymaster.kmessage.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object KeyStoreVault {
    private const val PROVIDER = "AndroidKeyStore"
    private const val MASTER_KEY_ALIAS = "kmessage_master_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_SIZE_BYTES = 12
    private const val TAG_SIZE_BITS = 128
    private val TAG = KeyStoreVault::class.java.simpleName
    private val keyStore: KeyStore by lazy { KeyStore.getInstance(PROVIDER).apply { load(null) } }

    fun getMasterKey(): SecretKey {
        val entry = keyStore.getEntry(MASTER_KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return entry?.secretKey ?: generateMasterKey()
    }

    fun encrypt(data: ByteArray): ByteArray {
        val masterKey = getMasterKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, masterKey)
        return cipher.doFinal(data)
    }

    fun decrypt(data: ByteArray): ByteArray {
        val masterKey = getMasterKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, masterKey)
        return cipher.doFinal(data)
    }

    private fun generateMasterKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val spec = createSpec(true)
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            } catch (_: StrongBoxUnavailableException) {
                val spec = createSpec(false)
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }
        } else {
            keyGenerator.init(createSpec(false))
            keyGenerator.generateKey()
        }
    }

    private fun createSpec(strongbox: Boolean): KeyGenParameterSpec {
        var strong = false
        val builder = KeyGenParameterSpec.Builder(
            MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        builder.setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        builder.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        builder.setKeySize(256)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            runCatching { builder.setIsStrongBoxBacked(strongbox) }
                .onSuccess { strong = true }

        if (!strong)
            Log.w(TAG, "Strongbox is not available.")
        return builder.build()
    }
}