package com.workstation.rotation.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Gestor de claves de encriptación usando Android Keystore
 * Implementa AES-256-GCM para máxima seguridad
 */
class KeyManager(private val context: Context) {
    
    companion object {
        private const val KEY_ALIAS = "WorkerRotationMasterKey"
        private const val DATABASE_KEY_ALIAS = "DatabaseEncryptionKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
        
        @Volatile
        private var INSTANCE: KeyManager? = null
        
        fun getInstance(context: Context): KeyManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: KeyManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }
    
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    /**
     * Genera una clave maestra para la aplicación
     */
    fun generateMasterKey(): Boolean {
        return try {
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setUserAuthenticationRequired(false) // Para acceso automático
                    .build()
                
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
            true
        } catch (e: Exception) {
            SecurityLogger.logError("Error generando clave maestra", e)
            false
        }
    }
    
    /**
     * Genera clave específica para encriptación de base de datos
     */
    fun generateDatabaseKey(): String? {
        return try {
            if (!keyStore.containsAlias(DATABASE_KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    DATABASE_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
            
            // Generar una clave derivada para SQLCipher
            generateDerivedDatabasePassword()
        } catch (e: Exception) {
            SecurityLogger.logError("Error generando clave de base de datos", e)
            null
        }
    }
    
    /**
     * Obtiene la clave de la base de datos
     */
    fun getDatabaseKey(): String? {
        return try {
            val encryptedPrefs = getEncryptedSharedPreferences()
            encryptedPrefs.getString("database_key", null) ?: generateDatabaseKey()
        } catch (e: Exception) {
            SecurityLogger.logError("Error obteniendo clave de base de datos", e)
            null
        }
    }
    
    /**
     * Encripta datos usando la clave maestra
     */
    fun encryptData(data: String): EncryptedData? {
        return try {
            val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance(AES_MODE)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(data.toByteArray())
            
            EncryptedData(
                encryptedData = encryptedBytes,
                iv = iv
            )
        } catch (e: Exception) {
            SecurityLogger.logError("Error encriptando datos", e)
            null
        }
    }
    
    /**
     * Desencripta datos usando la clave maestra
     */
    fun decryptData(encryptedData: EncryptedData): String? {
        return try {
            val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance(AES_MODE)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, encryptedData.iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            
            val decryptedBytes = cipher.doFinal(encryptedData.encryptedData)
            String(decryptedBytes)
        } catch (e: Exception) {
            SecurityLogger.logError("Error desencriptando datos", e)
            null
        }
    }
    
    /**
     * Verifica si las claves están disponibles
     */
    fun areKeysAvailable(): Boolean {
        return try {
            keyStore.containsAlias(KEY_ALIAS) && keyStore.containsAlias(DATABASE_KEY_ALIAS)
        } catch (e: Exception) {
            SecurityLogger.logError("Error verificando disponibilidad de claves", e)
            false
        }
    }
    
    /**
     * Elimina todas las claves (para reset de seguridad)
     */
    fun deleteAllKeys(): Boolean {
        return try {
            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.deleteEntry(KEY_ALIAS)
            }
            if (keyStore.containsAlias(DATABASE_KEY_ALIAS)) {
                keyStore.deleteEntry(DATABASE_KEY_ALIAS)
            }
            
            // Limpiar SharedPreferences encriptadas
            val encryptedPrefs = getEncryptedSharedPreferences()
            encryptedPrefs.edit().clear().apply()
            
            true
        } catch (e: Exception) {
            SecurityLogger.logError("Error eliminando claves", e)
            false
        }
    }
    
    /**
     * Obtiene SharedPreferences encriptadas
     */
    fun getEncryptedSharedPreferences(): android.content.SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * Genera una contraseña derivada para SQLCipher
     */
    private fun generateDerivedDatabasePassword(): String {
        val password = java.util.UUID.randomUUID().toString() + System.currentTimeMillis()
        val encryptedPrefs = getEncryptedSharedPreferences()
        encryptedPrefs.edit().putString("database_key", password).apply()
        return password
    }
    
    /**
     * Rota las claves de encriptación (para mantenimiento de seguridad)
     */
    fun rotateKeys(): Boolean {
        return try {
            // Respaldar datos importantes antes de rotar
            val oldDatabaseKey = getDatabaseKey()
            
            // Eliminar claves actuales
            deleteAllKeys()
            
            // Generar nuevas claves
            generateMasterKey() && generateDatabaseKey() != null
        } catch (e: Exception) {
            SecurityLogger.logError("Error rotando claves", e)
            false
        }
    }
}

/**
 * Clase de datos para almacenar información encriptada
 */
data class EncryptedData(
    val encryptedData: ByteArray,
    val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as EncryptedData
        
        if (!encryptedData.contentEquals(other.encryptedData)) return false
        if (!iv.contentEquals(other.iv)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = encryptedData.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}