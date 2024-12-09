package Orion.message

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import Orion.message.auth.GoogleAuthHelper
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleAuthHelper: GoogleAuthHelper

    private val RC_SIGN_IN = 100 // Código de solicitud para Google Sign-In

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        firebaseAuth = FirebaseAuth.getInstance()
        googleAuthHelper = GoogleAuthHelper(this, firebaseAuth)

        val googleSignUpButton = findViewById<Button>(R.id.googleSignUpButton)

        googleSignUpButton.setOnClickListener {
            val signInIntent = googleAuthHelper.getSignInIntent()
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            googleAuthHelper.handleSignInResult(data) { success, message ->
                if (success) {
                    // Redirigir al usuario a la siguiente actividad
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Mostrar error
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
