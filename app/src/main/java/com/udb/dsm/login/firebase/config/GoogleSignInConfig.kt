package com.udb.dsm.login.firebase.config

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.udb.dsm.login.firebase.R // Importante para R.string...

object GoogleSignInConfig {
    fun getClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // Solicita el ID Token, necesario para autenticar con Firebase
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail() // Solicita el email del usuario
            .build()
        return GoogleSignIn.getClient(context, gso)
    }
}