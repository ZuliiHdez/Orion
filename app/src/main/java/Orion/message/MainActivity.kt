package Orion.message

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Conectar vistas del XML
        val welcomeTextView = findViewById<TextView>(R.id.welcomeTextView)
        val startButton = findViewById<Button>(R.id.startButton)

        // Configurar el mensaje de bienvenida
        welcomeTextView.text = "Bienvenido a Messenger"

        // Configurar acción del botón
        startButton.setOnClickListener {
            // Ir a LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
