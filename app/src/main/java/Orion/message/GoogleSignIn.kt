@file:Suppress("DEPRECATION")

package Orion.message

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class GoogleSignInHelper(
    context: Context,
    private val auth: FirebaseAuth,
    private val activityResultLauncher: ActivityResultLauncher<Intent>,
    private val onSignInSuccess: (GoogleSignInAccount) -> Unit,
    private val onSignInFailure: (String) -> Unit
) {

    private val googleSignInClient: GoogleSignInClient

    init {
        // Configurar opciones de Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id)) // Asegúrate de usar el ID del cliente correcto
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    // Método para iniciar sesión con Google
    fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        activityResultLauncher.launch(signInIntent)
    }

    // Método que se llamará después de recibir el resultado de Google Sign-In
    fun handleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            account?.let {
                firebaseAuthWithGoogle(it)
            }
        } catch (e: ApiException) {
            onSignInFailure("Error al iniciar sesión con Google: ${e.message}")
        }
    }

    // Autenticar el usuario con Firebase usando el token de Google
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSignInSuccess(account)
                } else {
                    onSignInFailure("Error al autenticar con Firebase: ${task.exception?.message}")
                }
            }
    }
}
