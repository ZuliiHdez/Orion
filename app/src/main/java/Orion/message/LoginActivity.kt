package Orion.message

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInHelper: GoogleSignInHelper
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerTextView = findViewById<TextView>(R.id.registerTextView)
        val forgotPasswordTextView = findViewById<TextView>(R.id.forgotPasswordTextView)
        val rememberMeCheckBox = findViewById<CheckBox>(R.id.rememberMeCheckBox)
        val googleSignInButton = findViewById<ImageButton>(R.id.googleSignInButton)
        val passwordToggleIcon = findViewById<ImageView>(R.id.passwordToggleIcon)

        val savedEmail = sharedPreferences.getString("email", "")
        val savedPassword = sharedPreferences.getString("password", "")

        if (savedEmail?.isNotEmpty() == true && savedPassword?.isNotEmpty() == true) {
            // Si las credenciales guardadas existen, saltamos el login y vamos directamente al main
            val intent = Intent(this, LoggedMainActivity::class.java)
            startActivity(intent)
            finish()
        }

        passwordToggleIcon.setImageResource(android.R.drawable.ic_menu_view)

        passwordToggleIcon.setOnClickListener {
            if (passwordEditText.inputType == 129) {  // 129 es para texto de contraseña
                passwordEditText.inputType = 1  // 1 es para texto normal
                passwordToggleIcon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel) // Icono para mostrar
            } else {
                passwordEditText.inputType = 129 // Restablecer al tipo contraseña
                passwordToggleIcon.setImageResource(android.R.drawable.ic_menu_view) // Icono para ocultar
            }
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        // Inicializa GoogleSignInHelper y ActivityResultLauncher
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback { result ->
            googleSignInHelper.handleSignInResult(result.data)
        })

        googleSignInHelper = GoogleSignInHelper(
            context = this,
            auth = auth,
            activityResultLauncher = googleSignInLauncher,
            onSignInSuccess = { account ->
                // Manejar éxito de inicio de sesión
                Toast.makeText(this, "Bienvenido, ${account.displayName}", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoggedMainActivity::class.java)
                startActivity(intent)
                finish()
            },
            onSignInFailure = { error ->
                // Manejar error de inicio de sesión
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )

        googleSignInButton.setOnClickListener {
            googleSignInHelper.signInWithGoogle()
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password, rememberMeCheckBox.isChecked)
            }
        }

        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        forgotPasswordTextView.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_email), Toast.LENGTH_SHORT).show()
            } else {
                sendPasswordResetEmail(email)
            }
        }
    }

    private fun loginUser(email: String, password: String, rememberMe: Boolean) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Si el inicio de sesión es exitoso, se guarda el estado si "Recordar" está marcado
                    if (rememberMe) {
                        val editor = sharedPreferences.edit()
                        editor.putString("email", email)
                        editor.putString("password", password) // Recuerda la contraseña (puedes cifrarla si es necesario)
                        editor.apply()
                    } else {
                        // Si no está marcado "Recordar", borramos las credenciales guardadas
                        val editor = sharedPreferences.edit()
                        editor.remove("email")
                        editor.remove("password")
                        editor.apply()
                    }

                    // Se redirige a la actividad principal
                    val intent = Intent(this, LoggedMainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, getString(R.string.login_error, task.exception?.message), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.password_reset_success, email), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.password_reset_error, task.exception?.message), Toast.LENGTH_SHORT).show()
                }
            }
    }
}
