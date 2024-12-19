package Orion.message

import Orion.message.utils.FirebaseUtil
import android.annotation.SuppressLint
import android.graphics.Color
import android.content.ClipData
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.request.RequestOptions
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


                    // Verificar si el usuario tiene una imagen en el nodo "pictures"
                    FirebaseUtil.checkIfUserHasImage(currentUsername, // Reemplaza con el nombre de usuario que desees
                        callback = { hasImage ->
                            if (hasImage) {
                                // Si el usuario tiene una imagen, la cargamos
                                FirebaseUtil.loadUserProfileImage(this@LoggedMainActivity, currentUsername) { imageFile ->
                                    // Verificamos si tenemos un archivo de imagen
                                    imageFile?.let {
                                        // Usamos Glide para cargar la imagen desde el archivo
                                        Glide.with(this@LoggedMainActivity)
                                            .load(it)  // Cargar el archivo de imagen
                                            .apply(RequestOptions.circleCropTransform())  // Transformación circular
                                            .into(profileImageView)  // Colocamos la imagen en el ImageView
                                    } ?: run {
                                        // Si no se encuentra la imagen, mostramos un error
                                        Toast.makeText(this@LoggedMainActivity, "No se encontró la imagen", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                // Si el usuario no tiene imagen de perfil, mostramos un mensaje
                                Toast.makeText(this@LoggedMainActivity, "No hay imagen", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onError = { errorMessage ->
                            // En caso de error, mostramos un mensaje
                            Toast.makeText(this@LoggedMainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    )


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

    @SuppressLint("ResourceAsColor")
    private fun promptUsername(onUsernameEntered: (String) -> Unit) {
        val dialog = AlertDialog.Builder(this)

        // EditText para ingresar el nombre de usuario
        val input = EditText(this).apply {
            hint = "Ingrese el nombre de usuario"
            setPadding(16, 16, 16, 16) // Agregar relleno alrededor del texto
            background = resources.getDrawable(R.drawable.edit_text_background, null) // Establecer fondo personalizado
            setTextColor(resources.getColor(R.color.black, null)) // Color del texto
        }

        // TextView para mostrar el nombre de usuario actual
        val userNameText = TextView(this).apply {
            text = "Tu nombre de usuario: $currentUsername"
            setPadding(16, 16, 16, 16) // Agregar relleno para que no esté pegado al borde
            setTextColor(resources.getColor(R.color.blue, null)) // Color del texto
        }

        // LinearLayout para organizar los elementos
        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24) // Relleno del contenedor
            addView(userNameText)
            addView(input)
        }

        // Configuración del título y la vista del diálogo
        dialog.setTitle("Añadir Contacto:")
        dialog.setView(linearLayout)

        // Botón positivo (Agregar)
        dialog.setPositiveButton("Agregar") { _, _ ->
            val username = input.text.toString().trim()
            if (username.isNotEmpty()) {
                onUsernameEntered(username)
            } else {
                Toast.makeText(this, "El nombre de usuario no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }

        // Copiar nombre de usuario al hacer clic en el TextView
        userNameText.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Username", currentUsername)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Nombre de usuario copiado al portapapeles", Toast.LENGTH_SHORT).show()
        }

        // Botón negativo (Cancelar)
        dialog.setNegativeButton("Cancelar") { dialogInterface, _ -> dialogInterface.dismiss() }

        // Mostrar el diálogo
        val alertDialog = dialog.create()
        alertDialog.show()

        // Cambiar el color del texto del botón "Agregar" a azul
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#1247A4")) // Color azul

        // Cambiar el color del texto del botón "Cancelar" a rojo
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#FF0000")) // Color rojo
    }



    private fun showFriendRequestDialog(friendRequestManager: FriendRequestManager) {
        val recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@LoggedMainActivity)
        }

        friendRequestManager.fetchFriendRequests(recyclerView)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Solicitudes de Amistad")
            .setView(recyclerView)
            .setNegativeButton("Cerrar") { dialog, _ -> dialog.dismiss() }
            .show()

        // Cambiar el color del texto del botón "Cerrar" a rojo
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        negativeButton.setTextColor(Color.parseColor("#FF0000")) // Establece el color rojo
    }

}