package Orion.message

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class LoggedMainActivity : AppCompatActivity() {

    private var currentUsername: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logged_main)

        val firebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser?.uid
        val userName: TextView = findViewById(R.id.userName)
        val profileImageView: ImageView = findViewById(R.id.profileImage)
        val statusView: TextView = findViewById(R.id.userStatus)
        val settingsButton: ImageView = findViewById(R.id.settingsButton)

        // Botón para ir a configuración
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Escuchar cambios en Firebase en tiempo real
        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val userRef = database.reference.child("Users").child(userId)

            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val fullName = snapshot.child("fullName").value as? String ?: "Usuario Desconocido"
                    val profileImage = snapshot.child("image").value as? String ?: ""
                    val status = snapshot.child("status").value as? String ?: ""

                    userName.text = fullName
                    currentUsername = snapshot.child("username").value as? String ?: "Usuario Desconocido"

                    // Actualizar imagen de perfil
                    Glide.with(this@LoggedMainActivity)
                        .load(profileImage)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(profileImageView)

                    // Actualizar estado
                    statusView.text = status
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LoggedMainActivity, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
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

        // Eventos para añadir contactos
        setupContactActions()
    }

    // Recargar datos al reanudar
    override fun onResume() {
        super.onResume()
        // Aquí, los cambios en tiempo real ya están cubiertos con el ValueEventListener
    }

    private fun setupContactActions() {
        val addContactLabel: TextView = findViewById(R.id.add_contact)
        val addContactIcon: ImageView = findViewById(R.id.add_contact_ic)

        val addContactManager = AddContactManager()
        val addContactAction: (String) -> Unit = { username ->
            addContactManager.sendFriendRequest(
                contactUsername = username,
                onSuccess = {
                    Toast.makeText(this, "Solicitud de amistad enviada exitosamente", Toast.LENGTH_SHORT).show()
                },
                onFailure = { exception ->
                    Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        addContactLabel.setOnClickListener { promptUsername { username -> addContactAction(username) } }
        addContactIcon.setOnClickListener { promptUsername { username -> addContactAction(username) } }

        val requestIcon: ImageView = findViewById(R.id.request_ic)
        val requestText: TextView = findViewById(R.id.request_text)
        val friendRequestManager = FriendRequestManager(this)

        val showFriendRequests: () -> Unit = { showFriendRequestDialog(friendRequestManager) }

        requestIcon.setOnClickListener { showFriendRequests() }
        requestText.setOnClickListener { showFriendRequests() }
    }

    private fun promptUsername(onUsernameEntered: (String) -> Unit) {
        val dialog = AlertDialog.Builder(this)
        val input = EditText(this)
        input.hint = "Ingrese el nombre de usuario"

        val userNameText = TextView(this)
        userNameText.text = "Tu nombre de usuario: $currentUsername"

        val linearLayout = android.widget.LinearLayout(this)
        linearLayout.orientation = android.widget.LinearLayout.VERTICAL
        linearLayout.addView(userNameText)
        linearLayout.addView(input)

        dialog.setTitle("Añadir Contacto")
        dialog.setView(linearLayout)
        dialog.setPositiveButton("Agregar") { _, _ ->
            val username = input.text.toString().trim()
            if (username.isNotEmpty()) {
                onUsernameEntered(username)
            } else {
                Toast.makeText(this, "El nombre de usuario no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }

        userNameText.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = android.content.ClipData.newPlainText("Username", currentUsername)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Nombre de usuario copiado al portapapeles", Toast.LENGTH_SHORT).show()
        }

        dialog.setNegativeButton("Cancelar") { dialogInterface, _ -> dialogInterface.dismiss() }
        dialog.show()
    }

    private fun showFriendRequestDialog(friendRequestManager: FriendRequestManager) {
        val recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@LoggedMainActivity)
        }

        friendRequestManager.fetchFriendRequests(recyclerView)

        AlertDialog.Builder(this)
            .setTitle("Solicitudes de Amistad")
            .setView(recyclerView)
            .setNegativeButton("Cerrar") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}