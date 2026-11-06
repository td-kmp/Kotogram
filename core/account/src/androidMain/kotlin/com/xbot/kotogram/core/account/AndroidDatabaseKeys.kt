package com.xbot.kotogram.core.account

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import co.touchlab.kermit.Logger
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class AndroidDatabaseKeys(private val context: Context) : DatabaseKeys {
    private val mutex = Mutex()

    override suspend fun getOrCreate(id: AccountId): ByteArray? = withContext(Dispatchers.IO) {
        mutex.withLock {
            val file = keyFile(id)
            try {
                if (file.exists()) decrypt(id, file.readBytes()) else create(id, file)
            } catch (e: GeneralSecurityException) {
                Logger.e(e) { "Can't read the database key of account ${id.value}" }
                null
            } catch (e: IOException) {
                Logger.e(e) { "Can't read the database key of account ${id.value}" }
                null
            }
        }
    }

    override suspend fun delete(id: AccountId) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                keyFile(id).delete()
                keyStore().deleteEntry(alias(id))
            }
        }
    }

    // Named by the data centers too: test DC ids repeat production ones, and deleting one mustn't take the other's key.
    private fun alias(id: AccountId) = "$accountsDirectoryName.database_key.${id.value}"

    private fun keyFile(id: AccountId) =
        File(context.noBackupFilesDir, "$accountsDirectoryName-database-key-${id.value}")

    private fun keyStore() = KeyStore.getInstance(KEY_STORE).apply { load(null) }

    private fun create(id: AccountId, file: File): ByteArray =
        ByteArray(KEY_SIZE_BYTES).also { key ->
            SecureRandom().nextBytes(key)
            file.writeBytes(encrypt(id, key))
        }

    private fun secretKey(id: AccountId): SecretKey {
        (keyStore().getKey(alias(id), null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_STORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                alias(id),
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE_BITS)
                .build()
        )
        return generator.generateKey()
    }

    private fun encrypt(id: AccountId, plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey(id))
        return cipher.iv + cipher.doFinal(plain)
    }

    private fun decrypt(id: AccountId, data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey(id),
            GCMParameterSpec(TAG_BITS, data, 0, IV_SIZE)
        )
        return cipher.doFinal(data, IV_SIZE, data.size - IV_SIZE)
    }

    private companion object {
        const val KEY_STORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_SIZE_BYTES = 32
        const val KEY_SIZE_BITS = 256
        const val TAG_BITS = 128
        const val IV_SIZE = 12
    }
}
