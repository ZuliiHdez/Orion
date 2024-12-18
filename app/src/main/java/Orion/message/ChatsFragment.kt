package Orion.message

import Orion.message.model.ChatAdapter
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import Orion.message.utils.FirebaseUtil
import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher

class ChatsFragment : Fragment() {

    private var currentUsername = ""
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private var contactsList: List<FirebaseUtil.Contact> = emptyList()  // Almacenar los contactos localmente
    private var hasShownNoContactsMessage = false

    // Runnable para actualizar los contactos cada 5 segundos
    private val updateContactsRunnable: Runnable = object : Runnable {
        override fun run() {
            loadContacts()  // Volver a cargar los contactos cada 5 segundos
            recyclerView.postDelayed(this, 5000)  // Repetir cada 5 segundos
        }
    }

    // Handler para manejar la ejecución periódica
    private val handler = Handler()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewChats)

        // Aquí, buscamos el `searchBar` desde la actividad que contiene el fragmento
        val activity = activity as? LoggedMainActivity
        searchBar = activity?.findViewById(R.id.searchBar) ?: return view // Si no lo encuentra, no continuamos

        // Configuración del RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val divider = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(divider)

        // Verificar si el usuario está autenticado y cargar los contactos
        FirebaseUtil.executeIfAuthenticated(
            action = {
                FirebaseUtil.getCurrentUsername { username ->
                    if (!username.isNullOrEmpty()) {
                        currentUsername = username
                        loadContacts() // Cargar los contactos
                        startContactUpdateTask()  // Iniciar actualización periódica
                    } else {
                        Toast.makeText(context, "Error: No se pudo obtener el nombre de usuario", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onUnauthenticated = {
                Toast.makeText(context, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            }
        )

        // Agregar el TextWatcher al searchBar
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                val query = charSequence.toString().trim()
                filterContacts(query) // Filtrar contactos según la búsqueda
            }

            override fun afterTextChanged(editable: Editable?) {}
        })

        return view
    }

    // Función para cargar los contactos en el RecyclerView
    private fun loadContacts() {
        FirebaseUtil.getUserContacts(currentUsername) { contacts ->
            contactsList = contacts // Almacenar los contactos localmente
            if (contacts.isEmpty() && !hasShownNoContactsMessage) {
                Toast.makeText(context, "No tienes contactos aún", Toast.LENGTH_SHORT).show()
                hasShownNoContactsMessage = true
            } else {
                recyclerView.adapter = ChatAdapter(contacts)
                hasShownNoContactsMessage = false
            }
        }
    }

    // Función para filtrar los contactos en función de la búsqueda
    private fun filterContacts(query: String) {
        val filteredList = contactsList.filter {
            it.fullName.contains(query, ignoreCase = true) // Comparar nombres ignorando mayúsculas/minúsculas
        }
        recyclerView.adapter = ChatAdapter(filteredList)  // Actualizar el RecyclerView con la lista filtrada
    }

    // Iniciar la actualización periódica de los contactos
    private fun startContactUpdateTask() {
        handler.postDelayed(updateContactsRunnable, 5000)
    }

    // Detener el handler cuando el fragmento sea destruido
    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateContactsRunnable) // Detener el Runnable
    }
}
