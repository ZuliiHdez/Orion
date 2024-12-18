package Orion.message.model

import Orion.message.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import Orion.message.utils.FirebaseUtil
import android.util.Log

class ChatMessagesAdapter(
    private val messages: MutableList<FirebaseUtil.Message>, // Lista de mensajes mutable
    private val currentUserId: String // ID del usuario actual
) : RecyclerView.Adapter<ChatMessagesAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        // Inflar el layout para cada mensaje del RecyclerView
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        // Asignar los datos del mensaje al ViewHolder
        val message = messages[position]
        holder.bind(message, currentUserId)
    }

    override fun getItemCount(): Int = messages.size

    /**
     * Función para actualizar la lista de mensajes.
     */
    fun updateMessages(newMessages: List<FirebaseUtil.Message>) {
        Log.d("ChatMessagesAdapter", "Actualizando mensajes: ${newMessages.size}")
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }


    /**
     * Agregar un nuevo mensaje a la lista.
     */
    fun addMessage(message: FirebaseUtil.Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    // ViewHolder para cada mensaje
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val leftChatLayout: LinearLayout = itemView.findViewById(R.id.left_chat_layout)
        private val rightChatLayout: LinearLayout = itemView.findViewById(R.id.right_chat_layout)
        private val leftChatTextView: TextView = itemView.findViewById(R.id.left_chat_textview)
        private val leftChatTimestamp: TextView = itemView.findViewById(R.id.left_chat_timestamp)
        private val rightChatTextView: TextView = itemView.findViewById(R.id.right_chat_textview)
        private val rightChatTimestamp: TextView = itemView.findViewById(R.id.right_chat_timestamp)

        /**
         * Configurar los datos en las vistas del ViewHolder.
         */
        fun bind(message: FirebaseUtil.Message, currentUserId: String) {
            if (message.senderId == currentUserId) {
                // Mensaje enviado
                rightChatLayout.visibility = View.VISIBLE
                leftChatLayout.visibility = View.GONE
                rightChatTextView.text = message.text
                rightChatTimestamp.text = message.formattedTime
            } else {
                // Mensaje recibido
                leftChatLayout.visibility = View.VISIBLE
                rightChatLayout.visibility = View.GONE
                leftChatTextView.text = message.text
                leftChatTimestamp.text = message.formattedTime
            }
        }
    }
}

