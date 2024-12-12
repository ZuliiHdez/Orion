package Orion.message

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Obtener referencias a los botones
        val privacyPolicyButton = findViewById<Button>(R.id.privacyPolicyButton)
        val blockListButton = findViewById<Button>(R.id.blockListButton)
        val editProfileButton = findViewById<Button>(R.id.editProfileButton)
        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)
        val notificationSettingsButton = findViewById<Button>(R.id.notificationSettingsButton)
        val changeLanguageButton = findViewById<Button>(R.id.changeLanguageButton)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

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
            Toast.makeText(this, "Cambiar Idioma", Toast.LENGTH_SHORT).show()
            // Aquí puedes implementar la lógica para cambiar el idioma
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Sesión Cerrada", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
