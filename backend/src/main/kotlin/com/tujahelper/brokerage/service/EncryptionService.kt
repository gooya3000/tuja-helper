package com.tujahelper.brokerage.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Service
class EncryptionService(
    @Value("\${app.encryption.key}") private val rawKey: String,
) {
    companion object {
        private const val ALGORITHM = "AES/CBC/PKCS5Padding"
        private const val KEY_ALGORITHM = "AES"
        private const val IV_LENGTH = 16
    }

    // 키는 정확히 32바이트(256비트)가 필요하므로 패딩 또는 자름 처리
    private val keyBytes: ByteArray by lazy {
        rawKey.toByteArray(Charsets.UTF_8).let { bytes ->
            if (bytes.size >= 32) bytes.copyOf(32)
            else bytes.copyOf(32).also { padded -> bytes.copyInto(padded) }
        }
    }

    fun encrypt(plainText: String): String {
        val iv = ByteArray(IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyBytes, KEY_ALGORITHM), IvParameterSpec(iv))
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        // IV를 암호문 앞에 prefix로 붙여 Base64 인코딩
        val combined = iv + encrypted
        return Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(cipherText: String): String {
        val combined = Base64.getDecoder().decode(cipherText)
        val iv = combined.copyOf(IV_LENGTH)
        val encrypted = combined.copyOfRange(IV_LENGTH, combined.size)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(keyBytes, KEY_ALGORITHM), IvParameterSpec(iv))
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }
}
