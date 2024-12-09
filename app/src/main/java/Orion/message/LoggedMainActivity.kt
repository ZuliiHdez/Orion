package Orion.message

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth

class LoggedMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logged_main)

        // Obtén el usuario actual desde Firebase Auth
        val user = FirebaseAuth.getInstance().currentUser
        val userName = user?.displayName ?: "Usuario Desconocido"

        // Muestra el nombre del usuario en el TextView
        val userNameTextView: TextView = findViewById(R.id.userName)
        userNameTextView.text = userName
        // Otros componentes en la actividad
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        // Configuración de pestañas
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Chats"
                1 -> "Groups"
                else -> null
            }
        }.attach()
    }

}