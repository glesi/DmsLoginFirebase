package com.udb.dsm.login.firebase.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.udb.dsm.login.firebase.R // ¡Importante!
import com.udb.dsm.login.firebase.config.GoogleSignInConfig
import com.udb.dsm.login.firebase.databinding.ActivityLoginBinding // Import para ViewBinding
import com.udb.dsm.login.firebase.service.AuthService
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding // Instancia de ViewBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val authService = AuthService()
    private val TAG = "LoginActivity" // Para logs

    // Lanzador moderno para el resultado de Google Sign-In
    private val googleLoginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Google Sign In fue exitoso, autentica con Firebase
                val account = task.getResult(ApiException::class.java)!! // Lanza excepción si falla
                Log.d(TAG, "Google Sign-In successful, attempting Firebase auth.")
                account.idToken?.let { idToken ->
                    firebaseAuthWithGoogle(idToken)
                } ?: run {
                    Log.e(TAG, "Google Sign-In succeeded but idToken is null.")
                    showLoginError(getString(R.string.google_sign_in_failed, "ID Token missing"))
                }
            } catch (e: ApiException) {
                // Google Sign In falló. Maneja el error apropiadamente.
                Log.e(TAG, "Google Sign-In failed", e)
                showLoginError(getString(R.string.google_sign_in_failed, e.statusCode.toString()))
            }
        } else {
            Log.w(TAG, "Google Sign-In cancelled or failed. Result code: ${result.resultCode}")
            if (result.resultCode != Activity.RESULT_CANCELED) {
                showLoginError(getString(R.string.google_sign_in_failed, "Result code ${result.resultCode}"))
            } else {
                // Opcional: Mostrar mensaje si el usuario cancela
                // Toast.makeText(this, R.string.login_cancelled, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater) // Inflar usando ViewBinding
        setContentView(binding.root) // Establecer la vista raíz del binding

        // Configurar Google Sign-In Client
        googleSignInClient = GoogleSignInConfig.getClient(this)

        // --- Listeners para los botones ---
        binding.btnLogin.setOnClickListener {
            handleEmailPasswordLogin()
        }

        binding.btnGoogle.setOnClickListener {
            startGoogleSignIn()
        }

        // Opcional: Si ya hay un usuario logueado, ir directo a MainActivity
        // if (authService.getCurrentUser() != null) {
        //     navigateToMain()
        // }
    }

    // --- Lógica de Autenticación ---

    private fun handleEmailPasswordLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Validación básica
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.input_error_email)
            return
        } else {
            binding.tilEmail.error = null // Limpiar error
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.input_error_password)
            return
        } else {
            binding.tilPassword.error = null // Limpiar error
        }

        // Iniciar corrutina para la operación de red/autenticación
        lifecycleScope.launch {
            val result = authService.loginWithEmail(email, password)
            result.onSuccess { user ->
                Log.d(TAG, "Email/Password login successful: ${user.email}")
                showLoginSuccess(user)
                navigateToMain()
            }.onFailure { exception ->
                Log.e(TAG, "Email/Password login failed", exception)
                showLoginError(getString(R.string.email_login_failed, exception.localizedMessage ?: "Unknown error"))
            }
        }
    }

    private fun startGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleLoginLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        lifecycleScope.launch {
            val result = authService.loginWithGoogle(idToken)
            result.onSuccess { user ->
                Log.d(TAG, "Firebase auth with Google successful: ${user.email}")
                showLoginSuccess(user)
                navigateToMain()
            }.onFailure { exception ->
                Log.e(TAG, "Firebase auth with Google failed", exception)
                showLoginError(getString(R.string.google_sign_in_failed, exception.localizedMessage ?: "Firebase auth error"))
            }
        }
    }

    // --- Navegación y UI ---

    private fun navigateToMain() {
        // Ir a la actividad principal después del login exitoso
        val intent = Intent(this, MainActivity::class.java)
        // Flags para limpiar el stack de actividades (opcional pero común en logins)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Cierra LoginActivity para que el usuario no pueda volver atrás
    }

    private fun showLoginSuccess(user: FirebaseUser) {
        Toast.makeText(this, getString(R.string.welcome_message, user.email ?: "Usuario"), Toast.LENGTH_LONG).show()
    }

    private fun showLoginError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        // Podrías mostrar el error en un TextView en lugar de un Toast
        // binding.tvError.text = message
        // binding.tvError.visibility = View.VISIBLE
    }
}