package com.tormessenger.crypto

import android.util.Base64
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoManager {
    
    companion object {
        private const val RSA_ALGORITHM = "RSA"
        private const val AES_ALGORITHM = "AES"
        private const val RSA_TRANSFORMATION = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING"
        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 2048
        private const val AES_KEY_SIZE = 256
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
        
        init {
            Security.addProvider(BouncyCastleProvider())
        }
    }
    
    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM)
        keyPairGenerator.initialize(KEY_SIZE)
        return keyPairGenerator.generateKeyPair()
    }
    
    fun publicKeyToString(publicKey: PublicKey): String {
        return Base64.encodeToString(publicKey.encoded, Base64.DEFAULT)
    }
    
    fun privateKeyToString(privateKey: PrivateKey): String {
        return Base64.encodeToString(privateKey.encoded, Base64.DEFAULT)
    }
    
    fun stringToPublicKey(publicKeyString: String): PublicKey {
        val keyBytes = Base64.decode(publicKeyString, Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM)
        return keyFactory.generatePublic(keySpec)
    }
    
    fun stringToPrivateKey(privateKeyString: String): PrivateKey {
        val keyBytes = Base64.decode(privateKeyString, Base64.DEFAULT)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM)
        return keyFactory.generatePrivate(keySpec)
    }
    
    fun encryptMessage(message: String, recipientPublicKey: PublicKey): EncryptedMessage {
        // Generate AES key for message encryption
        val aesKey = generateAESKey()
        
        // Encrypt message with AES
        val encryptedMessage = encryptWithAES(message, aesKey)
        
        // Encrypt AES key with recipient's RSA public key
        val encryptedAESKey = encryptWithRSA(aesKey.encoded, recipientPublicKey)
        
        return EncryptedMessage(
            encryptedContent = encryptedMessage.encryptedData,
            encryptedKey = encryptedAESKey,
            iv = encryptedMessage.iv
        )
    }
    
    fun decryptMessage(encryptedMessage: EncryptedMessage, privateKey: PrivateKey): String {
        // Decrypt AES key with private RSA key
        val aesKeyBytes = decryptWithRSA(encryptedMessage.encryptedKey, privateKey)
        val aesKey = SecretKeySpec(aesKeyBytes, AES_ALGORITHM)
        
        // Decrypt message with AES key
        return decryptWithAES(encryptedMessage.encryptedContent, aesKey, encryptedMessage.iv)
    }
    
    private fun generateAESKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM)
        keyGenerator.init(AES_KEY_SIZE)
        return keyGenerator.generateKey()
    }
    
    private fun encryptWithAES(data: String, key: SecretKey): AESEncryptedData {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray())
        
        return AESEncryptedData(
            encryptedData = Base64.encodeToString(encryptedData, Base64.DEFAULT),
            iv = Base64.encodeToString(iv, Base64.DEFAULT)
        )
    }
    
    private fun decryptWithAES(encryptedData: String, key: SecretKey, ivString: String): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val iv = Base64.decode(ivString, Base64.DEFAULT)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
        
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        
        val encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        
        return String(decryptedBytes)
    }
    
    private fun encryptWithRSA(data: ByteArray, publicKey: PublicKey): String {
        val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedData = cipher.doFinal(data)
        return Base64.encodeToString(encryptedData, Base64.DEFAULT)
    }
    
    private fun decryptWithRSA(encryptedData: String, privateKey: PrivateKey): ByteArray {
        val cipher = Cipher.getInstance(RSA_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
        return cipher.doFinal(encryptedBytes)
    }
    
    data class EncryptedMessage(
        val encryptedContent: String,
        val encryptedKey: String,
        val iv: String
    )
    
    private data class AESEncryptedData(
        val encryptedData: String,
        val iv: String
    )
}
