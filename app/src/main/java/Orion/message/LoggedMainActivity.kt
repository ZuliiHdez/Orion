package Orion.message

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoggedMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logged_main)

        val firebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser?.uid
        val userNameTextView: TextView = findViewById(R.id.userName)

        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            database.reference.child("Users").child(userId).get()
                .addOnSuccessListener { snapshot ->
                    val fullName = snapshot.child("fullName").value as? String ?: "Usuario Desconocido"
                    userNameTextView.text = fullName
                }
                .addOnFailureListener {
                    userNameTextView.text = "Error al cargar el nombre"
                }
        }

        // Configuración de pestañas y ViewPager
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Chats"
                1 -> "Groups"
                else -> null
            }
        }.attach()
    }
}
