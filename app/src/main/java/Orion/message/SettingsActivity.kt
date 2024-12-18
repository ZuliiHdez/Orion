package Orion.message

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)

        // Obtener referencias a los botones
        val privacyTitle = findViewById<TextView>(R.id.privacyTitle)
        val accountTitle = findViewById<TextView>(R.id.accountTitle)
        val languageTitle = findViewById<TextView>(R.id.languageTitle)
        val notificationTitle = findViewById<TextView>(R.id.notificationTitle)

        val privacyPolicyButton = findViewById<Button>(R.id.privacyPolicyButton)
        val blockListButton = findViewById<Button>(R.id.blockListButton)
        val editProfileButton = findViewById<Button>(R.id.editProfileButton)
        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)
        val notificationSettingsButton = findViewById<Button>(R.id.notificationSettingsButton)
        val changeLanguageButton = findViewById<Button>(R.id.changeLanguageButton)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        // Configuración de textos
        privacyPolicyButton.text = getString(R.string.privacy_policy)
        blockListButton.text = getString(R.string.block_list)
        editProfileButton.text = getString(R.string.edit_profile)
        changePasswordButton.text = getString(R.string.change_password)
        notificationSettingsButton.text = getString(R.string.notification_settings)
        changeLanguageButton.text = getString(R.string.change_language)
        logoutButton.text = getString(R.string.logout)

        privacyTitle.text = getString(R.string.privacy)
        accountTitle.text = getString(R.string.account)
        languageTitle.text = getString(R.string.language)
        notificationTitle.text = getString(R.string.notification)

        // Manejo de clics para cada botón
        privacyPolicyButton.setOnClickListener {
            Toast.makeText(this, "Mostrar Política de Privacidad", Toast.LENGTH_SHORT).show()
            // Aquí puedes abrir un navegador con el enlace a la política de privacidad
        }

        blockListButton.setOnClickListener {
            Toast.makeText(this, "Abrir Lista de Bloqueados", Toast.LENGTH_SHORT).show()
            // Aquí puedes iniciar una nueva actividad para mostrar la lista de bloqueados
        }

        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        changePasswordButton.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        notificationSettingsButton.setOnClickListener {
            Toast.makeText(this, "Configurar Notificaciones", Toast.LENGTH_SHORT).show()
            // Aquí puedes abrir una nueva pantalla para configurar las notificaciones
        }

        changeLanguageButton.setOnClickListener {
            startActivity(Intent(this, ChangeLanguageActivity::class.java))
        }

        // Botón Logout
        logoutButton.setOnClickListener {
            // Cerrar sesión en Firebase
            FirebaseAuth.getInstance().signOut()

            // Borrar las credenciales guardadas en SharedPreferences
            val editor = sharedPreferences.edit()
            editor.remove("email")
            editor.remove("password")
            editor.apply()

            // Mostrar mensaje de sesión cerrada
            Toast.makeText(this, "Sesión Cerrada", Toast.LENGTH_SHORT).show()

            // Redirigir a LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Finaliza SettingsActivity
        }
    }
}
