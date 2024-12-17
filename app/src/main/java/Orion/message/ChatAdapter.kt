package Orion.message

import Orion.message.utils.FirebaseUtil // Importamos la clase utilitaria
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ChatAdapter(private val contacts: List<FirebaseUtil.Contact>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflar el layout para cada ítem del RecyclerView
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Asignar los datos del contacto al ViewHolder
        val contact = contacts[position]
        holder.bind(contact)
    }

    override fun getItemCount(): Int = contacts.size

    // ViewHolder que contiene las vistas de cada ítem
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.contactNameTextView)
        private val profileImageView: ImageView = itemView.findViewById(R.id.contactImageView)

        fun bind(contact: FirebaseUtil.Contact) {
            // Asignar el nombre del contacto al TextView
            nameTextView.text = contact.fullName
            profileImageView.setImageResource(R.drawable.ic_profile_placeholder)

            // Configurar el clic en el contacto
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ChatActivity::class.java)
                // Pasar el nombre de usuario del contacto a la ChatActivity
                intent.putExtra("CONTACT_USERNAME", contact.fullName)
                context.startActivity(intent)
            }
        }
    }
}
