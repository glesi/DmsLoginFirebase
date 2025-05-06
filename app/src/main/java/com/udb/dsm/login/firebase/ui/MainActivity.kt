package com.udb.dsm.login.firebase.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.udb.dsm.login.firebase.R
import com.udb.dsm.login.firebase.databinding.ActivityMainBinding
import com.udb.dsm.login.firebase.service.AuthService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authService = AuthService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = authService.getCurrentUser()
        if (currentUser == null) {
            // Si por alguna razón no hay usuario, volver al Login
            navigateToLogin()
            return // Salir de onCreate para evitar más procesamiento
        }

        // Mostrar información del usuario
        binding.tvUserInfo.text = getString(R.string.logged_in_as, currentUser.email ?: "Unknown User")

        // Botón de Logout
        binding.btnLogout.setOnClickListener {
            // Cerrar sesión en Firebase
            authService.logout()

            // Cerrar sesión en Google (Importante para que pueda elegir otra cuenta la próxima vez)
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut().addOnCompleteListener {
                // Navegar de vuelta al Login después de cerrar sesión de Google y Firebase
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}