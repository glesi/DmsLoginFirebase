package com.udb.dsm.login.firebase.service

import android.util.Log // Para logging de errores
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.AuthResult // Importante para el resultado
import kotlinx.coroutines.tasks.await // Necesario para await()
import java.lang.Exception // Para capturar excepciones generales

class AuthService {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val TAG = "AuthService" // Para logs

    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result: AuthResult = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Log.d(TAG, "Email login successful for: ${it.email}")
                Result.success(it)
            } ?: run {
                Log.w(TAG, "Email login returned null user.")
                Result.failure(Exception("Authentication failed: User is null"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Email login failed", e)
            Result.failure(e) // Devuelve la excepción específica
        }
    }

    suspend fun loginWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result: AuthResult = auth.signInWithCredential(credential).await()
            result.user?.let {
                Log.d(TAG, "Google login successful for: ${it.email}")
                Result.success(it)
            } ?: run {
                Log.w(TAG, "Google login returned null user.")
                Result.failure(Exception("Authentication failed: User is null"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google login failed", e)
            Result.failure(e) // Devuelve la excepción específica
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun logout() {
        auth.signOut()
        // Considerar también desconectar Google Sign-In si es necesario
        // GoogleSignIn.getClient(context, ...).signOut() // Necesitarías el contexto aquí
        Log.d(TAG, "User logged out.")
    }
}