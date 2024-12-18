package Orion.message

import Orion.message.auth.GoogleAuthHelper
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class RegisterActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private val RC_SIGN_IN = 100 // Código de solicitud para Google Sign-In

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializa FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val fullNameEditText = findViewById<EditText>(R.id.fullNameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val googleRegisterButton = findViewById<ImageButton>(R.id.googleRegisterButton)
        val continueWith = findViewById<TextView>(R.id.continueWith)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)

        usernameEditText.hint = getString(R.string.username_hint)
        fullNameEditText.hint = getString(R.string.fullname_hint)
        emailEditText.hint = getString(R.string.email_hint)
        passwordEditText.hint = getString(R.string.password_hint)
        registerButton.text = getString(R.string.register)
        continueWith.text = getString(R.string.continue_with)
        confirmPasswordEditText.hint = getString(R.string.repeat_password_hint)

        // Configuración para Google Sign-In
        googleRegisterButton.setOnClickListener {
            val signInIntent = GoogleAuthHelper(this, firebaseAuth).getSignInIntent()
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        // Registro con email y contraseña
        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val fullName = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && fullName.isNotEmpty() && username.isNotEmpty()) {
                if (password == confirmPassword) {
                    if (isPasswordStrong(password)) {
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
                                                    val userId = firebaseAuth.currentUser?.uid
                                                    if (userId != null) {
                                                        val userMap = mapOf(
                                                            "username" to username,
                                                            "fullName" to fullName,
                                                            "email" to email
                                                        )

                                                        // Guardar los datos del usuario en el nodo correspondiente
                                                        FirebaseDatabase.getInstance()
                                                            .reference.child("Users")
                                                            .child(userId)
                                                            .setValue(userMap)
                                                            .addOnCompleteListener { saveTask ->
                                                                if (saveTask.isSuccessful) {
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
                                                                        "Error al guardar datos: ${saveTask.exception?.message}",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
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
                        Toast.makeText(
                            this,
                            "La contraseña debe tener al menos 8 caracteres, una letra mayúscula y un número.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    
    private fun isPasswordStrong(password: String): Boolean {
        val passwordPattern = "^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$"
        return password.matches(passwordPattern.toRegex())
    }

    // Resultados de Google Sign-In
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val googleAuthHelper = GoogleAuthHelper(this, firebaseAuth)
            googleAuthHelper.handleSignInResult(data) { success, message ->
                if (success) {
                    val user = firebaseAuth.currentUser
                    user?.let {
                        val userId = it.uid
                        val username = it.displayName ?: "Usuario"
                        val email = it.email ?: "Sin correo"

                        val userMap = mapOf(
                            "username" to username,
                            "fullName" to username,  // Guardamos el nombre como fullName
                            "email" to email
                        )

                        // Guardar datos del usuario en Firebase Realtime Database
                        FirebaseDatabase.getInstance()
                            .reference.child("Users")
                            .child(userId)
                            .setValue(userMap)
                            .addOnCompleteListener { saveTask ->
                                if (saveTask.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Registro con Google exitoso",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Error al guardar datos: ${saveTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                } else {
                    // Mostrar error
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}