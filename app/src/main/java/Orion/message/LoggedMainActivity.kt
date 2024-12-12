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
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoggedMainActivity : AppCompatActivity() {


    private var currentUsername: String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logged_main)

        val firebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser?.uid
        val userName: TextView = findViewById(R.id.userName)
        val settingsButton: ImageView = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            // Crear un Intent para abrir SettingsActivity
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Obtener nombre de usuario del Firebase
        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            database.reference.child("Users").child(userId).get()
                .addOnSuccessListener { snapshot ->
                    val fullName = snapshot.child("fullName").value as? String ?: "Usuario Desconocido"
                    currentUsername = snapshot.child("username").value as? String ?: "Usuario Desconocido"
                    userName.text = fullName
                }
                .addOnFailureListener {
                    userName.text = "Error al cargar el nombre"
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

        // Configuración del evento para añadir contactos
        val addContactLabel: TextView = findViewById(R.id.add_contact)
        val addContactIcon: ImageView = findViewById(R.id.add_contact_ic)

        // Manager para añadir contactos (usando el username actual)
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

        // Evento en el label
        addContactLabel.setOnClickListener {
            promptUsername { username ->
                addContactAction(username)
            }
        }

        // Evento en el icono
        addContactIcon.setOnClickListener {
            promptUsername { username ->
                addContactAction(username)
            }
        }

        // Configuración del evento para mostrar solicitudes de amistad
        val requestIcon: ImageView = findViewById(R.id.request_ic)
        val requestText: TextView = findViewById(R.id.request_text)

        val friendRequestManager = FriendRequestManager(this)

        val showFriendRequests: () -> Unit = {
            showFriendRequestDialog(friendRequestManager)
        }

        requestIcon.setOnClickListener { showFriendRequests() }
        requestText.setOnClickListener { showFriendRequests() }
    }

    /**
     * Muestra un diálogo para ingresar el nombre de usuario del contacto.
     * @param onUsernameEntered Callback para manejar el nombre de usuario ingresado.
     */
    /**
     * Muestra un diálogo para ingresar el nombre de usuario del contacto.
     * @param onUsernameEntered Callback para manejar el nombre de usuario ingresado.
     */
    private fun promptUsername(onUsernameEntered: (String) -> Unit) {
        val dialog = AlertDialog.Builder(this)
        val input = EditText(this)
        input.hint = "Ingrese el nombre de usuario"

        // Mostrar el nombre de usuario actual
        val userNameText = TextView(this)
        userNameText.text = "Tu nombre de usuario: $currentUsername"

        // Creación del LinearLayout para contener los TextViews y el EditText
        val linearLayout = android.widget.LinearLayout(this)
        linearLayout.orientation = android.widget.LinearLayout.VERTICAL

        // Añadimos los TextViews y el EditText al LinearLayout
        linearLayout.addView(userNameText)  // Nombre de usuario actual
        linearLayout.addView(input)  // Campo para ingresar el nombre de usuario del contacto

        dialog.setTitle("Añadir Contacto")
        dialog.setView(linearLayout)  // Establecemos el LinearLayout como vista del diálogo

        dialog.setPositiveButton("Agregar") { _, _ ->
            val username = input.text.toString().trim()
            if (username.isNotEmpty()) {
                onUsernameEntered(username)  // Llamamos al callback con el nombre de usuario ingresado
            } else {
                Toast.makeText(this, "El nombre de usuario no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }

        // Función para copiar al portapapeles el nombre de usuario actual
        userNameText.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = android.content.ClipData.newPlainText("Username", currentUsername)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Nombre de usuario copiado al portapapeles", Toast.LENGTH_SHORT).show()
        }

        dialog.setNegativeButton("Cancelar") { dialogInterface, _ ->
            dialogInterface.dismiss()  // Cerrar el diálogo
        }

        // Mostramos el diálogo
        dialog.show()
    }

    /**
     * Muestra un cuadro de diálogo con las solicitudes de amistad en un RecyclerView.
     * @param friendRequestManager Instancia de FriendRequestManager para cargar las solicitudes.
     */
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