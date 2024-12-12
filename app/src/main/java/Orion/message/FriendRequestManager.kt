package Orion.message

import android.content.Context
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FriendRequestManager(private val context: Context) {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid
    private var currentUsername: String = ""

    /**
     * Obtiene y muestra las solicitudes de amistad en un RecyclerView.
     */
    fun fetchFriendRequests(recyclerView: RecyclerView) {
        // Verifica si el usuario está autenticado
        if (userId == null) {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener el nombre de usuario actual
        val userRef = database.reference.child("Users").child(userId!!)
        userRef.get().addOnSuccessListener { snapshot ->
            currentUsername = snapshot.child("username").value as? String ?: ""
            if (currentUsername.isEmpty()) {
                Toast.makeText(context, "Error al obtener el nombre de usuario", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // Crear la referencia de FriendRequests para el usuario actual
            val friendRequestRef: DatabaseReference = database.reference.child("FriendRequests").child(currentUsername)

            // Obtener todos los hijos de la referencia (las solicitudes)
            friendRequestRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val friendRequestsList = mutableListOf<FriendRequest>()

                    // Iterar sobre todos los hijos (solicitudes) de FriendRequests
                    for (childSnapshot in snapshot.children) {
                        val senderUsername = childSnapshot.child("senderUsername").getValue(String::class.java)
                        val value = childSnapshot.child("value").getValue(Int::class.java) ?: 0

                        // Solo agregamos las solicitudes que están pendientes (value == 0)
                        if (senderUsername != null && value == 0) {
                            friendRequestsList.add(FriendRequest(senderUsername))
                        }
                    }

                    // Llamar al método para configurar el RecyclerView con las solicitudes
                    setupRecyclerView(recyclerView, friendRequestsList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error al cargar solicitudes: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }.addOnFailureListener {
            Toast.makeText(context, "Error al obtener el nombre de usuario", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Configura el RecyclerView con las solicitudes de amistad recibidas.
     *
     * @param recyclerView RecyclerView donde se mostrarán las solicitudes.
     * @param friendRequests Lista de solicitudes de amistad.
     */
    private fun setupRecyclerView(recyclerView: RecyclerView, friendRequests: List<FriendRequest>) {
        val adapter = FriendRequestAdapter(friendRequests, { request ->
            handleFriendRequestAction(request)
        }, { request ->
            handleRejectRequestAction(request)
        })

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    /**
     * Maneja las acciones del usuario sobre una solicitud de amistad (Aceptar).
     *
     * @param request Solicitud de amistad seleccionada.
     */
    private fun handleFriendRequestAction(request: FriendRequest) {
        val senderUsername = request.senderName

        // Acción de aceptar solicitud
        acceptFriendRequest(senderUsername)

        // Aquí puedes agregar un Toast o cualquier otra acción para aceptar la solicitud
        Toast.makeText(context, "Solicitud de ${request.senderName} aceptada", Toast.LENGTH_SHORT).show()
    }

    /**
     * Maneja la acción de rechazar la solicitud de amistad.
     *
     * @param request Solicitud de amistad seleccionada.
     */
    private fun handleRejectRequestAction(request: FriendRequest) {
        val senderUsername = request.senderName
        // Aquí puedes agregar lógica para rechazar la solicitud
        // Ejemplo: eliminar la solicitud de amistad
        rejectFriendRequest(senderUsername)
        Toast.makeText(context, "Solicitud de ${request.senderName} rechazada", Toast.LENGTH_SHORT).show()
    }

    /**
     * Acepta la solicitud de amistad de un usuario y lo agrega a los contactos de ambos usuarios.
     * @param senderUsername El nombre de usuario del que envió la solicitud.
     */
    private fun acceptFriendRequest(senderUsername: String) {
        // Asegurarse de que el username esté disponible antes de continuar
        if (currentUsername.isEmpty()) {
            Toast.makeText(context, "Error: No se encontró el nombre de usuario actual", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = database.reference.child("Users")

        // Añadir el senderUsername a los contactos del usuario actual
        userRef.child(currentUsername).child("contacts").child(senderUsername).setValue(true)

        // Añadir el currentUsername a los contactos del usuario que envió la solicitud
        userRef.child(senderUsername).child("contacts").child(currentUsername).setValue(true)

        // Eliminar la solicitud de amistad
        database.reference.child("FriendRequests").child(currentUsername).child(senderUsername).removeValue()
    }

    /**
     * Rechaza la solicitud de amistad de un usuario y elimina la solicitud.
     * @param senderUsername El nombre de usuario del que envió la solicitud.
     */
    private fun rejectFriendRequest(senderUsername: String) {
        // Eliminar la solicitud de amistad
        database.reference.child("FriendRequests").child(currentUsername).child(senderUsername).removeValue()
    }

    /**
     * Clase de datos para representar una solicitud de amistad.
     */
    data class FriendRequest(val senderName: String)
}