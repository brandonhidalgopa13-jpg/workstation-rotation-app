package com.workstation.rotation.data.cloud

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 🔐 GESTOR DE AUTENTICACIÓN EN LA NUBE
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 📋 FUNCIONES PRINCIPALES:
 * 
 * 🔑 1. AUTENTICACIÓN CON GOOGLE
 *    - Inicio de sesión con cuenta de Google
 *    - Gestión de tokens de autenticación
 *    - Manejo de estados de sesión
 * 
 * 👤 2. GESTIÓN DE USUARIOS
 *    - Información del usuario actual
 *    - Estado de autenticación
 *    - Cierre de sesión seguro
 * 
 * 🔒 3. SEGURIDAD
 *    - Validación de credenciales
 *    - Manejo de errores de autenticación
 *    - Renovación automática de tokens
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
class CloudAuthManager(private val context: Context) {
    
    private val auth: FirebaseAuth? = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    private val oneTapClient: SignInClient? = try { Identity.getSignInClient(context) } catch (e: Exception) { null }
    
    companion object {
        private const val WEB_CLIENT_ID = "your-web-client-id-here" // Se debe configurar
    }
    
    /**
     * Obtiene el usuario actual autenticado.
     */
    fun getCurrentUser(): FirebaseUser? = auth?.currentUser
    
    /**
     * Verifica si hay un usuario autenticado.
     */
    fun isUserSignedIn(): Boolean = auth != null && getCurrentUser() != null
    
    /**
     * Obtiene el ID único del usuario actual.
     */
    fun getCurrentUserId(): String? = getCurrentUser()?.uid
    
    /**
     * Obtiene el email del usuario actual.
     */
    fun getCurrentUserEmail(): String? = getCurrentUser()?.email
    
    /**
     * Obtiene el nombre del usuario actual.
     */
    fun getCurrentUserName(): String? = getCurrentUser()?.displayName
    
    /**
     * Verifica si Firebase está disponible.
     */
    fun isFirebaseAvailable(): Boolean = auth != null && oneTapClient != null
    
    /**
     * Inicia el proceso de autenticación con Google One Tap.
     */
    suspend fun beginSignIn(): IntentSenderRequest? {
        if (!isFirebaseAvailable()) return null
        return try {
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(WEB_CLIENT_ID)
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .setAutoSelectEnabled(true)
                .build()
            
            val result = oneTapClient?.beginSignIn(signInRequest)?.await()
            result?.let { IntentSenderRequest.Builder(it.pendingIntent.intentSender).build() }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Completa la autenticación con el resultado de Google Sign-In.
     */
    suspend fun signInWithGoogle(data: Intent): AuthResult {
        return try {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val googleIdToken = credential.googleIdToken
            
            if (googleIdToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                
                AuthResult.Success(authResult.user)
            } else {
                AuthResult.Error("No se pudo obtener el token de Google")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error desconocido en la autenticación")
        }
    }
    
    /**
     * Inicia sesión de forma anónima para usuarios que no quieren crear cuenta.
     */
    suspend fun signInAnonymously(): AuthResult {
        return try {
            val result = auth.signInAnonymously().await()
            AuthResult.Success(result.user)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error en el inicio de sesión anónimo")
        }
    }
    
    /**
     * Cierra la sesión del usuario actual.
     */
    suspend fun signOut(): Boolean {
        return try {
            auth.signOut()
            oneTapClient.signOut().await()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Elimina la cuenta del usuario actual.
     */
    suspend fun deleteAccount(): AuthResult {
        return try {
            val user = getCurrentUser()
            if (user != null) {
                user.delete().await()
                AuthResult.Success(null)
            } else {
                AuthResult.Error("No hay usuario autenticado")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error al eliminar la cuenta")
        }
    }
    
    /**
     * Reautentica al usuario para operaciones sensibles.
     */
    suspend fun reauthenticate(): AuthResult {
        return try {
            val user = getCurrentUser()
            if (user != null && !user.isAnonymous) {
                // Para usuarios de Google, necesitamos reautenticar
                val signInRequest = beginSignIn()
                if (signInRequest != null) {
                    AuthResult.ReauthRequired(signInRequest)
                } else {
                    AuthResult.Error("No se pudo iniciar la reautenticación")
                }
            } else {
                AuthResult.Success(user)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Error en la reautenticación")
        }
    }
    
    /**
     * Resultado de las operaciones de autenticación.
     */
    sealed class AuthResult {
        data class Success(val user: FirebaseUser?) : AuthResult()
        data class Error(val message: String) : AuthResult()
        data class ReauthRequired(val intentSender: IntentSenderRequest) : AuthResult()
    }
}