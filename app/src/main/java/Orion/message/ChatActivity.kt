package Orion.message

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import Orion.message.utils.FirebaseUtil
import Orion.message.utils.FirebaseUtil.Message
import Orion.message.utils.FirebaseUtil.getCurrentUsername
import Orion.message.utils.FirebaseUtil.getUserIdByUsername
import Orion.message.model.ChatMessagesAdapter
import Orion.message.utils.FirebaseUtil.checkChatroomExists
import Orion.message.utils.FirebaseUtil.getCurrentUserId
import java.text.SimpleDateFormat
import java.util.Locale

class ChatActivity : AppCompatActivity() {

    private lateinit var chatUsername: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatMessageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var otherUsernameTextView: TextView
    private lateinit var backButton: ImageButton

    private lateinit var chatMessagesAdapter: ChatMessagesAdapter
    private lateinit var currentUserId: String
    private lateinit var currentUserName: String
    private lateinit var chatroomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Obtener el nombre de usuario del contacto desde el Intent
        chatUsername = intent.getStringExtra("CONTACT_USERNAME") ?: ""

        if (chatUsername.isNotEmpty()) {
            initializeUI()

            currentUserId = getCurrentUserId()!!

            // Obtener el nombre de usuario actual y continuar si es válido
            getCurrentUsername { currentUserName ->
                if (currentUserName == null) {
                    Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
                    finish() // Terminar la actividad si el nombre de usuario es nulo
                    return@getCurrentUsername
                }

                this.currentUserName = currentUserName
                val chatFullName = intent.getStringExtra("CONTACT_FULLNAME") ?: ""

                if (chatUsername.isNotEmpty() && chatFullName.isNotEmpty()) {
                    otherUsernameTextView.text = chatFullName
                    getUserIdByUsername(chatUsername) { chatUserId ->
                        if (chatUserId != null) {
                            chatroomId = generateChatroomId(currentUserId, chatUserId)
                            setupRecyclerView()
                            loadMessages()
                            setupListeners()
                        } else {
                            Toast.makeText(this, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Error: Usuario no válido", Toast.LENGTH_SHORT).show()
                    finish() // Terminar la actividad si los datos son inválidos
                }
            }
        } else {
            Toast.makeText(this, "Error: Usuario no válido", Toast.LENGTH_SHORT).show()
            finish() // Terminar la actividad si no hay un nombre de usuario válido
        }
    }

    private fun initializeUI() {
        recyclerView = findViewById(R.id.chat_recycler_view)
        chatMessageInput = findViewById(R.id.chat_message_input)
        sendButton = findViewById(R.id.message_send_btn)
        otherUsernameTextView = findViewById(R.id.other_username)
        backButton = findViewById(R.id.back_btn)
        otherUsernameTextView.text = chatUsername
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        chatMessagesAdapter = ChatMessagesAdapter(mutableListOf(), currentUserId)
        recyclerView.adapter = chatMessagesAdapter
    }

    private fun loadMessages() {
        checkChatroomExists(chatroomId){ exist ->
            if(exist){
                    FirebaseUtil.getMessagesForChat(chatroomId) { messages ->
                        chatMessagesAdapter.updateMessages(messages)
                        recyclerView.smoothScrollToPosition(chatMessagesAdapter.itemCount - 1)
                    }
            } else {
                Toast.makeText(this, "Error: No se pudo encontrar la sala de chat", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessage() {
        val messageText = chatMessageInput.text.toString().trim()

        if (messageText.isNotEmpty()) {
            FirebaseUtil.createChatRoom(chatroomId, chatUsername, currentUserName) { chatroomIdResult ->
                if (chatroomIdResult != null) {
                    chatroomId = chatroomIdResult
                    val newMessage = Message(
                        senderId = currentUserId,
                        text = messageText,
                        formattedTime = getFormattedTime(System.currentTimeMillis()),
                        type = "text",
                        timestamp = System.currentTimeMillis()
                    )
                    FirebaseUtil.sendMessageToChat(chatroomId, newMessage) {
                        chatMessageInput.text.clear()
                        //chatMessagesAdapter.addMessage(newMessage)
                        recyclerView.smoothScrollToPosition(chatMessagesAdapter.itemCount - 1)
                    }
                } else {
                    Toast.makeText(this, "Error al crear la sala de chat", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "El mensaje no puede estar vacío", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateChatroomId(userId1: String, userId2: String): String {
        val sortedIds = listOf(userId1, userId2).sorted()
        return sortedIds.joinToString("_")
    }

    fun getFormattedTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(timestamp)
    }

    private fun setupListeners() {
        sendButton.setOnClickListener {
            sendMessage()
        }

        backButton.setOnClickListener {
            onBackPressed() // Volver a la actividad anterior
        }
    }
}
