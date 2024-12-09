package Orion.message.auth

import Orion.message.R
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class GoogleAuthHelper(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth
) {

    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    fun handleSignInResult(data: Intent?, onResult: (Boolean, String?) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.let {
                firebaseAuthWithGoogle(it, onResult)
            } ?: run {
                onResult(false, "Google Sign-In failed.")
            }
        } catch (e: ApiException) {
            onResult(false, "Error: ${e.message}")
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserToDatabase(account)
                    onResult(true, "Successfully authenticated")
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    private fun saveUserToDatabase(account: GoogleSignInAccount) {
        val userId = firebaseAuth.currentUser?.uid
        val database = FirebaseDatabase.getInstance()
        val userRef = database.reference.child("Users").child(userId ?: "")

        val userMap = mapOf(
            "username" to (account.displayName ?: "Unknown"),
            "email" to (account.email ?: "No email"),
            "profilePicture" to (account.photoUrl?.toString() ?: "")
        )

        userRef.setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("GoogleAuthHelper", "User saved to database")
                } else {
                    Log.e("GoogleAuthHelper", "Failed to save user: ${task.exception?.message}")
                }
            }
    }
}
