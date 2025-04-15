package com.pvlmamaev.decentralizedstorageapp

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object FileEncryptor {
    private const val AES_KEY_SIZE = 256 // bits
    private const val GCM_IV_LENGTH = 12 // bytes
    private const val GCM_TAG_LENGTH = 128 // bits

    // Генерация случайного ключа AES
    fun generateKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(AES_KEY_SIZE)
        return keyGen.generateKey()
    }

    // Шифрование: вход — Uri файла, выход — временный файл с зашифрованным содержимым
    fun encryptFile(context: Context, inputUri: Uri, key: SecretKey): File {
        val inputStream = context.contentResolver.openInputStream(inputUri)
            ?: throw IllegalArgumentException("Не удалось открыть файл")

        val inputBytes = inputStream.readBytes()
        inputStream.close()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)

        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        val encryptedBytes = cipher.doFinal(inputBytes)

        // Сохраняем IV + зашифрованные данные в один файл
        val encryptedFile = File.createTempFile("encrypted_", ".bin", context.cacheDir)
        FileOutputStream(encryptedFile).use { fos ->
            fos.write(iv)
            fos.write(encryptedBytes)
        }

        return encryptedFile
    }
}
