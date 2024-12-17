package Orion.message

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import Orion.message.utils.FirebaseUtil

class ChatsFragment : Fragment() {

    private var currentUsername = ""
    private lateinit var recyclerView: RecyclerView

    // Variable para controlar si ya se mostró el mensaje de "No tienes contactos aún"
    private var hasShownNoContactsMessage = false

    // Runnable para actualizar los contactos cada 5 segundos
    private val updateContactsRunnable: Runnable = object : Runnable {
        override fun run() {
            // Volver a cargar los contactos cada 5 segundos
            loadContacts(recyclerView)

            // Programar la próxima ejecución dentro de 5 segundos
            recyclerView.postDelayed(this, 5000)  // 5000ms = 5 segundos
        }
    }

    // Handler para manejar la ejecución periódica
    private val handler = Handler()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewChats)

        // Configurar el LayoutManager y el DividerItemDecoration
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Añadir el DividerItemDecoration para separar los ítems con una línea
        val divider = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(divider)

        // Verificar si el usuario está autenticado
        FirebaseUtil.executeIfAuthenticated(
            action = {
                // Obtener el nombre de usuario actual
                FirebaseUtil.getCurrentUsername { username ->
                    if (!username.isNullOrEmpty()) {
                        currentUsername = username
                        loadContacts(recyclerView)
                        startContactUpdateTask()  // Iniciar la tarea que actualiza los contactos cada 5 segundos
                    } else {
                        Toast.makeText(context, "Error: No se pudo obtener el nombre de usuario", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onUnauthenticated = {
                Toast.makeText(context, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            }
        )

        return view
    }

    // Función para cargar los contactos en el RecyclerView
    fun loadContacts(recyclerView: RecyclerView) {
        // Usar el método de FirebaseUtil para obtener los contactos en tiempo real
        FirebaseUtil.getUserContacts(currentUsername) { contactsList ->
            // Si la lista de contactos está vacía, mostramos un mensaje solo la primera vez
            if (contactsList.isEmpty()) {
                if (!hasShownNoContactsMessage) {
                    Toast.makeText(context, "No tienes contactos aún", Toast.LENGTH_SHORT).show()
                    hasShownNoContactsMessage = true  // Marcar que ya se mostró el mensaje
                }
            } else {
                // Si la lista no está vacía, mostrar los contactos
                recyclerView.adapter = ChatAdapter(contactsList)
                hasShownNoContactsMessage = false  // Resetear cuando haya contactos
            }
        }
    }

    // Función para iniciar la actualización periódica de los contactos
    private fun startContactUpdateTask() {
        handler.postDelayed(updateContactsRunnable, 5000)  // Inicia la tarea después de 5 segundos
    }

    // Asegurarse de detener el Handler cuando el fragmento sea destruido
    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateContactsRunnable)  // Detener el Runnable cuando la vista sea destruida
    }
}
