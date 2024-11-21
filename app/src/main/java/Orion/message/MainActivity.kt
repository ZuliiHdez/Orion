package Orion.message

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import Orion.message.databinding.ActivityMainBinding
import android.content.Intent
import android.widget.Button
import android.widget.TextView

import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val welcomeTextView = findViewById<TextView>(R.id.welcomeTextView)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        val currentUser = auth.currentUser
        welcomeTextView.text = "Bienvenido, ${currentUser?.email}"

        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
