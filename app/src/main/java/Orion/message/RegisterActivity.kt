package Orion.message

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import Orion.message.auth.GoogleAuthHelper
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RegisterActivity : AppCompatActivity() {


    private lateinit var firebaseAuth: FirebaseAuth
    private val firebaseHelper = FirebaseHelper()
    private lateinit var googleAuthHelper: GoogleAuthHelper

    private val RC_SIGN_IN = 100 // Código de solicitud para Google Sign-In


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        firebaseAuth = FirebaseAuth.getInstance()
        googleAuthHelper = GoogleAuthHelper(this, firebaseAuth)

        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val fullNameEditText = findViewById<EditText>(R.id.fullNameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val googleRegisterButton = findViewById<ImageButton>(R.id.googleRegisterButton)

        googleRegisterButton.setOnClickListener {
            val signInIntent = googleAuthHelper.getSignInIntent()
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val fullName = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && fullName.isNotEmpty() && username.isNotEmpty()) {
                val databaseReference = FirebaseDatabase.getInstance().reference.child("Users")

                // Verificar si el nombre de usuario ya existe
                databaseReference.orderByChild("username").equalTo(username)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                // Nombre de usuario ya registrado
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "El nombre de usuario ya está en uso.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                // Registrar al usuario
                                firebaseAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val userId =
                                                firebaseAuth.currentUser?.uid
                                                    ?: return@addOnCompleteListener
                                            val userMap = mapOf(
                                                "username" to username,
                                                "fullName" to fullName,
                                                "email" to email
                                            )

                                            firebaseHelper.saveUserData(userId, userMap) { success, message ->
                                                if (success) {
                                                    Toast.makeText(
                                                        this@RegisterActivity,
                                                        "Registro exitoso",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    startActivity(
                                                        Intent(
                                                            this@RegisterActivity,
                                                            LoginActivity::class.java
                                                        )
                                                    )
                                                    finish()
                                                } else {
                                                    Toast.makeText(
                                                        this@RegisterActivity,
                                                        "Error al guardar datos: $message",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        } else {
                                            Toast.makeText(
                                                this@RegisterActivity,
                                                "Error: ${task.exception?.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Error al verificar el nombre de usuario: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
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