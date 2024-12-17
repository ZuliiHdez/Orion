package Orion.message

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.bumptech.glide.Glide
import Orion.message.utils.FirebaseUtil

class ChatActivity : AppCompatActivity() {

    private lateinit var chatUsername: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatMessageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var otherUsernameTextView: TextView
    private lateinit var backButton: ImageButton

    private lateinit var chatMessagesAdapter: ChatMessagesAdapter
    private lateinit var currentUserId: String
    private lateinit var chatroomId: String
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Obtener el nombre de usuario del contacto desde el Intent
        chatUsername = intent.getStringExtra("CONTACT_USERNAME") ?: ""

        if (chatUsername.isNotEmpty()) {
            // Inicializar las vistas
            recyclerView = findViewById(R.id.chat_recycler_view)
            chatMessageInput = findViewById(R.id.chat_message_input)
            sendButton = findViewById(R.id.message_send_btn)
            otherUsernameTextView = findViewById(R.id.other_username)
            backButton = findViewById(R.id.back_btn)

            // Mostrar el nombre de usuario del contacto en el Toolbar
            otherUsernameTextView.text = chatUsername

            // Obtener el ID del usuario actual desde Firebase Auth
            currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            // Obtener o generar un chatroomId basado en los dos usuarios (por ejemplo, usando el ID de ambos usuarios en orden alfabético)
            chatroomId = generateChatroomId(currentUserId, chatUsername)

            // Configurar RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.setHasFixedSize(true)

            // Inicializar el adaptador
            chatMessagesAdapter = ChatMessagesAdapter(mutableListOf(), currentUserId)
            recyclerView.adapter = chatMessagesAdapter

            // Cargar los mensajes del chat
            loadMessages()

            // Configurar el botón de "enviar" para enviar mensajes
            sendButton.setOnClickListener {
                sendMessage()
            }

            // Configurar el evento del botón de retroceso
            backButton.setOnClickListener {
                onBackPressed() // Volver a la actividad anterior
            }

        } else {
            Toast.makeText(this, "Error: Usuario no válido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMessages() {
        // Cargar los mensajes de la subcolección "messages" del chatroom correspondiente
        firestore.collection("chatrooms")
            .document(chatroomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING) // Ordenar los mensajes por timestamp
            .get()
            .addOnSuccessListener { snapshot ->
                val messages = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirebaseUtil.Message::class.java)
                }.toMutableList() // Convertimos a MutableList

                // Actualizar el adaptador con los mensajes cargados
                chatMessagesAdapter = ChatMessagesAdapter(messages, currentUserId)
                recyclerView.adapter = chatMessagesAdapter
                recyclerView.smoothScrollToPosition(chatMessagesAdapter.itemCount - 1) // Hacer scroll hacia el último mensaje
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudieron cargar los mensajes", Toast.LENGTH_SHORT).show()
            }
    }


    private fun sendMessage() {
        val messageText = chatMessageInput.text.toString().trim()

        if (messageText.isNotEmpty()) {
            val message = FirebaseUtil.Message(
                senderId = currentUserId,
                text = messageText,
                timestamp = System.currentTimeMillis(),
                type = "text" // Aquí puedes cambiar el tipo según el tipo de mensaje (texto, imagen, etc.)
            )

            // Guardar el mensaje en Firestore
            firestore.collection("chatrooms")
                .document(chatroomId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener {
                    // Limpiar el campo de texto después de enviar el mensaje
                    chatMessageInput.text.clear()

                    // Agregar el nuevo mensaje al adaptador
                    chatMessagesAdapter.addMessage(message)

                    // Hacer scroll hacia el último mensaje
                    recyclerView.smoothScrollToPosition(chatMessagesAdapter.itemCount - 1)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "No se pudo enviar el mensaje", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "El mensaje no puede estar vacío", Toast.LENGTH_SHORT).show()
        }
    }

    // Método para generar el chatroomId de manera consistente (orden alfabético de los IDs de los usuarios)
    private fun generateChatroomId(userId1: String, userId2: String): String {
        val sortedIds = listOf(userId1, userId2).sorted()
        return sortedIds.joinToString("_") // Por ejemplo: userId1_userId2
    }
}
