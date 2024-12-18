package Orion.message

import Orion.message.utils.FirebaseUtil
import android.content.Context
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FriendRequestManager(private val context: Context) {

    private var currentUsername: String = ""

    /**
     * Obtiene y muestra las solicitudes de amistad en un RecyclerView.
     */
    fun fetchFriendRequests(recyclerView: RecyclerView) {
        // Verificar si el usuario está autenticado
        if (!FirebaseUtil.isUserAuthenticated()) {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener el nombre de usuario actual
        FirebaseUtil.getCurrentUsername { username ->
            if (username?.isNotEmpty() == true) {
                currentUsername = username!!
                loadFriendRequests(recyclerView)
            } else {
                Toast.makeText(context, "Error al obtener el nombre de usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Carga las solicitudes de amistad desde Firebase.
     */
    private fun loadFriendRequests(recyclerView: RecyclerView) {
        FirebaseUtil.getFriendRequests(currentUsername) { friendRequests ->
            setupRecyclerView(recyclerView, friendRequests)
        }
    }

    /**
     * Configura el RecyclerView con las solicitudes de amistad recibidas.
     */
    private fun setupRecyclerView(recyclerView: RecyclerView, friendRequests: List<FirebaseUtil.FriendRequest>) {
        val adapter = FriendRequestAdapter(friendRequests, { request ->
            handleAcceptRequest(request)
        }, { request ->
            handleRejectRequest(request)
        })

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    /**
     * Maneja la acción de aceptar una solicitud de amistad.
     */
    private fun handleAcceptRequest(request: FirebaseUtil.FriendRequest) {
        FirebaseUtil.acceptFriendRequest(currentUsername, request.senderName,
            onSuccess = {
                Toast.makeText(context, "Solicitud de ${request.senderName} aceptada", Toast.LENGTH_SHORT).show()
            },
            onFailure = { error ->
                Toast.makeText(context, "Error al aceptar solicitud: $error", Toast.LENGTH_SHORT).show()
            })
    }

    /**
     * Maneja la acción de rechazar una solicitud de amistad.
     */
    private fun handleRejectRequest(request: FirebaseUtil.FriendRequest) {
        FirebaseUtil.rejectFriendRequest(currentUsername, request.senderName,
            onSuccess = {
                Toast.makeText(context, "Solicitud de ${request.senderName} rechazada", Toast.LENGTH_SHORT).show()
            },
            onFailure = { error ->
                Toast.makeText(context, "Error al rechazar solicitud: $error", Toast.LENGTH_SHORT).show()
            })
    }
}
