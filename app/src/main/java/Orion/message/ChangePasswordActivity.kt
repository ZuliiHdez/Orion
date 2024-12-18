package Orion.message

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        val currentPasswordEditText = findViewById<EditText>(R.id.currentPasswordEditText)
        val newPasswordEditText = findViewById<EditText>(R.id.newPasswordEditText)
        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)

        currentPasswordEditText.hint = getString(R.string.current_password_hint)
        newPasswordEditText.hint = getString(R.string.new_password_hint)
        changePasswordButton.text = getString(R.string.change_password)

        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        changePasswordButton.setOnClickListener {
            val currentPassword = currentPasswordEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()

            if (currentPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                if (isPasswordStrong(newPassword)) {
                    val email = currentUser?.email
                    if (email != null) {
                        val credential = EmailAuthProvider.getCredential(email, currentPassword)

                        currentUser.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                            if (reauthTask.isSuccessful) {
                                currentUser.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        Toast.makeText(
                                            this,
                                            "Contraseña actualizada correctamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Error al actualizar la contraseña: ${updateTask.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    this,
                                    "La contraseña actual es incorrecta",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "La nueva contraseña debe tener al menos 8 caracteres, una letra mayúscula y un número.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isPasswordStrong(password: String): Boolean {
        val passwordPattern = "^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$"
        return password.matches(passwordPattern.toRegex())
    }
}
