package Orion.message

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase



class AddContactManager() {

    /**
     * Envia una solicitud de amistad al usuario con el nombre de usuario dado.
     * @param contactUsername Nombre de usuario del contacto al que se quiere enviar la solicitud.
     * @param onSuccess Función que se ejecuta cuando la solicitud se envía correctamente.
     * @param onFailure Función que se ejecuta cuando ocurre un error.
     */
    fun sendFriendRequest(contactUsername: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.reference.child("Users")

        //Buscar nombre del usuario actual
        val firebaseAuth = FirebaseAuth.getInstance()
        val userId = firebaseAuth.currentUser?.uid
        var currentUsername=""
        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            database.reference.child("Users").child(userId).get()
                .addOnSuccessListener { snapshot ->
                    currentUsername = snapshot.child("username").value as? String ?: "Usuario Desconocido"
                }
                .addOnFailureListener {
                    currentUsername = "Error al cargar el nombre"
                }
        }

        // Buscar el usuario destinatario por su nombre de usuario
        usersRef.orderByChild("username").equalTo(contactUsername).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists() && snapshot.children.any()) {
                    val contactSnapshot = snapshot.children.firstOrNull()
                    val contactUsername = contactSnapshot?.child("username")?.value as? String

                    if (contactUsername != null) {
                        // Crear la solicitud de amistad
                        val friendRequestRef = database.reference.child("FriendRequests")
                            .child(contactUsername)
                            .child(currentUsername)

                        friendRequestRef.setValue(mapOf(
                            "recipientUsername" to contactUsername,
                            "senderUsername" to currentUsername,
                            "value" to 0
                        ))
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { onFailure(it) }
                    } else {
                        onFailure(Exception("Usuario no encontrado"))
                    }
                } else {
                    onFailure(Exception("Usuario no encontrado"))
                }
            }
            .addOnFailureListener { onFailure(it) }

    }
}
