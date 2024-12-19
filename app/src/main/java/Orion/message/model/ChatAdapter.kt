package Orion.message.model

import Orion.message.ChatActivity
import Orion.message.R
import Orion.message.utils.FirebaseUtil // Importamos la clase utilitaria
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class ChatAdapter(private var contacts: List<FirebaseUtil.Contact>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    // Método para actualizar la lista de contactos
    fun updateContacts(newContacts: List<FirebaseUtil.Contact>) {
        contacts = newContacts
        notifyDataSetChanged()  // Notificar al RecyclerView que la lista de datos ha cambiado
    }

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
        private var currentImageUrl: String? = null  // Para almacenar la URL de la imagen

        fun bind(contact: FirebaseUtil.Contact) {
            // Asignar el nombre completo del contacto al TextView
            nameTextView.text = contact.fullName

            // Obtener el contexto correctamente
            val context = itemView.context

            FirebaseUtil.checkIfUserHasImage(contact.username, callback = { hasImage ->
                if (hasImage) {
                    // Si el usuario tiene una imagen, cargarla solo si ha cambiado
                    FirebaseUtil.loadUserProfileImage(context, contact.username) { imageFile ->
                        imageFile?.let {
                            val newImageUrl = it.path // Usa la URL o el path de la imagen

                            // Solo recargar la imagen si la URL ha cambiado
                            if (newImageUrl != currentImageUrl) {
                                Glide.with(context)
                                    .load(it)
                                    .apply(RequestOptions.circleCropTransform())
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)  // Cachea la imagen
                                    .into(profileImageView)

                                // Actualizar la URL almacenada
                                currentImageUrl = newImageUrl
                            }
                        }
                    }
                }
            }, onError = { errorMessage ->
                // No mostrar Toast en caso de error
            })

            // Configurar el clic en el contacto
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ChatActivity::class.java)
                // Pasar tanto el nombre de usuario como el nombre completo del contacto a la ChatActivity
                intent.putExtra("CONTACT_USERNAME", contact.username)
                intent.putExtra("CONTACT_FULLNAME", contact.fullName)
                context.startActivity(intent)
            }
        }
    }
}



