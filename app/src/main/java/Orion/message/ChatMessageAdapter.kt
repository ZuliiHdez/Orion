package Orion.message

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import Orion.message.utils.FirebaseUtil // Asegúrate de tener la clase de utilidad para obtener el usuario actual
import Orion.message.utils.FirebaseUtil.Message

// Adapter para mostrar los mensajes en el RecyclerView
class ChatMessagesAdapter(
    private var messages: MutableList<Message>, // Hacerlo mutable para poder modificarla
    private val currentUserId: String
) : RecyclerView.Adapter<ChatMessagesAdapter.ViewHolder>() {

    // Se llama cuando se necesita crear una nueva vista para un ítem
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflamos el layout para cada mensaje
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return ViewHolder(view)
    }

    // Se llama para asignar los datos al ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    // Devuelve la cantidad de ítems (mensajes) en la lista
    override fun getItemCount(): Int = messages.size

    // Función para agregar un mensaje a la lista
    fun addMessage(message: Message) {
        messages.add(message) // Agregar el nuevo mensaje al final
        notifyItemInserted(messages.size - 1)  // Notificar que se insertó un nuevo ítem
    }

    // ViewHolder que mantiene las vistas de cada mensaje
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val leftChatLayout: LinearLayout = itemView.findViewById(R.id.left_chat_layout)
        private val rightChatLayout: LinearLayout = itemView.findViewById(R.id.right_chat_layout)
        private val leftChatTextView: TextView = itemView.findViewById(R.id.left_chat_textview)
        private val rightChatTextView: TextView = itemView.findViewById(R.id.right_chat_textview)

        // Asigna los datos del mensaje al ViewHolder
        fun bind(message: Message) {
            // Verificar si el mensaje es del usuario actual o de otro usuario
            if (message.senderId == FirebaseUtil.getCurrentUserId()) { // Usamos el método estático para obtener el ID del usuario actual
                // Si el mensaje es del usuario actual, lo mostramos a la derecha
                leftChatLayout.visibility = View.GONE
                rightChatLayout.visibility = View.VISIBLE
                rightChatTextView.text = message.text
            } else {
                // Si el mensaje es de otro usuario, lo mostramos a la izquierda
                rightChatLayout.visibility = View.GONE
                leftChatLayout.visibility = View.VISIBLE
                leftChatTextView.text = message.text
            }
        }
    }
}
