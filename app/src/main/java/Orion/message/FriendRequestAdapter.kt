package Orion.message

import Orion.message.utils.FirebaseUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FriendRequestAdapter(
    private val friendRequests: List<FirebaseUtil.FriendRequest>, // Usar FirebaseUtil.FriendRequest
    private val onAcceptAction: (FirebaseUtil.FriendRequest) -> Unit, // Callback al aceptar
    private val onRejectAction: (FirebaseUtil.FriendRequest) -> Unit  // Callback al rechazar
) : RecyclerView.Adapter<FriendRequestAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_request, parent, false) // Inflar el layout
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = friendRequests[position] // Obtener la solicitud en la posición actual
        holder.bind(request, onAcceptAction, onRejectAction)
    }

    override fun getItemCount(): Int = friendRequests.size // Tamaño de la lista

    // ViewHolder que maneja cada ítem del RecyclerView
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderNameTextView: TextView = itemView.findViewById(R.id.senderNameTextView)
        private val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        private val rejectButton: Button = itemView.findViewById(R.id.rejectButton)

        /**
         * Vincula los datos de la solicitud y asigna los listeners de los botones.
         */
        fun bind(
            request: FirebaseUtil.FriendRequest,
            onAcceptAction: (FirebaseUtil.FriendRequest) -> Unit,
            onRejectAction: (FirebaseUtil.FriendRequest) -> Unit
        ) {
            senderNameTextView.text = request.senderName // Mostrar el nombre del solicitante

            acceptButton.setOnClickListener { onAcceptAction(request) } // Acción al aceptar
            rejectButton.setOnClickListener { onRejectAction(request) } // Acción al rechazar
        }
    }
}
