package com.workstation.rotation.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
// import com.scottyab.rootbeer.RootBeer // Comentado temporalmente
import java.io.File

/**
 * Verificador de seguridad del dispositivo
 * Detecta dispositivos rooteados, debugging, y otras vulnerabilidades
 */
class DeviceSecurityChecker(private val context: Context) {
    
    companion object {
        private const val TAG = "DeviceSecurityChecker"
        
        // Aplicaciones conocidas de root
        private val ROOT_APPS = listOf(
            "com.noshufou.android.su",
            "com.noshufou.android.su.elite",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.yellowes.su",
            "com.topjohnwu.magisk",
            "com.kingroot.kinguser",
            "com.kingo.root",
            "com.smedialink.oneclickroot",
            "com.zhiqupk.root.global",
            "com.alephzain.framaroot"
        )
        
        // Paths comunes de binarios de root
        private val ROOT_PATHS = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
    }
    
    // private val rootBeer = RootBeer(context) // Comentado temporalmente
    
    /**
     * Realiza una verificación completa de seguridad del dispositivo
     */
    fun performSecurityCheck(): DeviceSecurityStatus {
        SecurityLogger.logInfo("Iniciando verificación de seguridad del dispositivo")
        
        val isRooted = checkRootStatus()
        val isDebuggingEnabled = checkDebuggingStatus()
        val hasUnknownSources = checkUnknownSources()
        val hasSuspiciousApps = checkSuspiciousApps()
        val isEmulator = checkEmulator()
        val hasDeveloperOptions = checkDeveloperOptions()
        
        val securityLevel = calculateSecurityLevel(
            isRooted, isDebuggingEnabled, hasUnknownSources, 
            hasSuspiciousApps, isEmulator, hasDeveloperOptions
        )
        
        val status = DeviceSecurityStatus(
            isRooted = isRooted,
            isDebuggingEnabled = isDebuggingEnabled,
            hasUnknownSources = hasUnknownSources,
            hasSuspiciousApps = hasSuspiciousApps,
            isEmulator = isEmulator,
            hasDeveloperOptions = hasDeveloperOptions,
            securityLevel = securityLevel,
            timestamp = System.currentTimeMillis()
        )
        
        logSecurityStatus(status)
        return status
    }
    
    /**
     * Verifica si el dispositivo está rooteado
     */
    private fun checkRootStatus(): Boolean {
        return try {
            // Usar solo verificaciones manuales por ahora
            val manualCheck = checkRootManually()
            
            if (manualCheck) {
                SecurityLogger.logCritical(
                    "Dispositivo rooteado detectado",
                    "Detección manual: $manualCheck"
                )
            }
            
            manualCheck
        } catch (e: Exception) {
            SecurityLogger.logError("Error verificando root status", e)
            false
        }
    }
    
    /**
     * Verificación manual de root
     */
    private fun checkRootManually(): Boolean {
        // Verificar binarios de su
        for (path in ROOT_PATHS) {
            if (File(path).exists()) {
                return true
            }
        }
        
        // Verificar aplicaciones de root instaladas
        for (packageName in ROOT_APPS) {
            if (isPackageInstalled(packageName)) {
                return true
            }
        }
        
        return false
    }
} 
   
    /**
     * Verifica si el debugging está habilitado
     */
    private fun checkDebuggingStatus(): Boolean {
        return try {
            val adbEnabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Global.ADB_ENABLED, 0
            ) == 1
            
            val developmentEnabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
            ) == 1
            
            val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            
            val debuggingEnabled = adbEnabled || developmentEnabled || isDebuggable
            
            if (debuggingEnabled) {
                SecurityLogger.logWarning(
                    "Debugging habilitado detectado",
                    "ADB: $adbEnabled, Dev: $developmentEnabled, Debuggable: $isDebuggable"
                )
            }
            
            debuggingEnabled
        } catch (e: Exception) {
            SecurityLogger.logError("Error verificando debugging status", e)
            false
        }
    }
    
    /**
     * Verifica si las fuentes desconocidas están habilitadas
     */
    private fun checkUnknownSources(): Boolean {
        return try {
            val unknownSources = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.packageManager.canRequestPackageInstalls()
            } else {
                @Suppress("DEPRECATION")
                Settings.Secure.getInt(
                    context.contentResolver,
                    Settings.Secure.INSTALL_NON_MARKET_APPS, 0
                ) == 1
            }
            
            if (unknownSources) {
                SecurityLogger.logWarning("Fuentes desconocidas habilitadas")
            }
            
            unknownSources
        } catch (e: Exception) {
            SecurityLogger.logError("Error verificando fuentes desconocidas", e)
            false
        }
    }
    
    /**
     * Verifica aplicaciones sospechosas instaladas
     */
    private fun checkSuspiciousApps(): Boolean {
        return try {
            val packageManager = context.packageManager
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            
            val suspiciousApps = mutableListOf<String>()
            
            for (app in installedApps) {
                // Verificar aplicaciones de root
                if (ROOT_APPS.contains(app.packageName)) {
                    suspiciousApps.add(app.packageName)
                }
                
                // Verificar aplicaciones de hacking/debugging
                if (app.packageName.contains("xposed") || 
                    app.packageName.contains("substrate") ||
                    app.packageName.contains("frida")) {
                    suspiciousApps.add(app.packageName)
                }
            }
            
            if (suspiciousApps.isNotEmpty()) {
                SecurityLogger.logWarning(
                    "Aplicaciones sospechosas detectadas",
                    "Apps: ${suspiciousApps.joinToString(", ")}"
                )
            }
            
            suspiciousApps.isNotEmpty()
        } catch (e: Exception) {
            SecurityLogger.logError("Error verificando aplicaciones sospechosas", e)
            false
        }
    }
    
    /**
     * Verifica si se está ejecutando en un emulador
     */
    private fun checkEmulator(): Boolean {
        return try {
            val isEmulator = (Build.FINGERPRINT.startsWith("generic") ||
                    Build.FINGERPRINT.startsWith("unknown") ||
                    Build.MODEL.contains("google_sdk") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.MODEL.contains("Android SDK built for x86") ||
                    Build.MANUFACTURER.contains("Genymotion") ||
                    (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                    "google_sdk" == Build.PRODUCT)
            
            if (isEmulator) {
                SecurityLogger.logWarning("Ejecución en emulador detectada")
            }
            
            isEmulator
        } catch (e: Exception) {
            SecurityLogger.logError("Error verificando emulador", e)
            false
        }
    }
    
    /**
     * Verifica si las opciones de desarrollador están habilitadas
     */
    private fun checkDeveloperOptions(): Boolean {
        return try {
            val developerOptions = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
            ) == 1
            
            if (developerOptions) {
                SecurityLogger.logInfo("Opciones de desarrollador habilitadas")
            }
            
            developerOptions
        } catch (e: Exception) {
            SecurityLogger.logError("Error verificando opciones de desarrollador", e)
            false
        }
    }
    
    /**
     * Verifica si un paquete está instalado
     */
    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * Calcula el nivel de seguridad basado en las verificaciones
     */
    private fun calculateSecurityLevel(
        isRooted: Boolean,
        isDebuggingEnabled: Boolean,
        hasUnknownSources: Boolean,
        hasSuspiciousApps: Boolean,
        isEmulator: Boolean,
        hasDeveloperOptions: Boolean
    ): SecurityLevel {
        return when {
            isRooted || hasSuspiciousApps -> SecurityLevel.CRITICAL
            isDebuggingEnabled || isEmulator -> SecurityLevel.HIGH
            hasUnknownSources || hasDeveloperOptions -> SecurityLevel.MEDIUM
            else -> SecurityLevel.LOW
        }
    }
    
    /**
     * Registra el estado de seguridad en los logs
     */
    private fun logSecurityStatus(status: DeviceSecurityStatus) {
        val details = buildString {
            appendLine("=== DEVICE SECURITY STATUS ===")
            appendLine("Rooted: ${status.isRooted}")
            appendLine("Debugging: ${status.isDebuggingEnabled}")
            appendLine("Unknown Sources: ${status.hasUnknownSources}")
            appendLine("Suspicious Apps: ${status.hasSuspiciousApps}")
            appendLine("Emulator: ${status.isEmulator}")
            appendLine("Developer Options: ${status.hasDeveloperOptions}")
            appendLine("Security Level: ${status.securityLevel}")
        }
        
        when (status.securityLevel) {
            SecurityLevel.CRITICAL -> SecurityLogger.logCritical("Verificación de seguridad completada", details)
            SecurityLevel.HIGH -> SecurityLogger.logError("Verificación de seguridad completada", Exception(details))
            SecurityLevel.MEDIUM -> SecurityLogger.logWarning("Verificación de seguridad completada", details)
            SecurityLevel.LOW -> SecurityLogger.logInfo("Verificación de seguridad completada", details)
        }
    }
}

/**
 * Estado de seguridad del dispositivo
 */
data class DeviceSecurityStatus(
    val isRooted: Boolean,
    val isDebuggingEnabled: Boolean,
    val hasUnknownSources: Boolean,
    val hasSuspiciousApps: Boolean,
    val isEmulator: Boolean,
    val hasDeveloperOptions: Boolean,
    val securityLevel: SecurityLevel,
    val timestamp: Long
)

/**
 * Niveles de seguridad
 */
enum class SecurityLevel {
    LOW,      // Dispositivo seguro
    MEDIUM,   // Algunas configuraciones de riesgo
    HIGH,     // Configuraciones peligrosas
    CRITICAL  // Dispositivo comprometido
}