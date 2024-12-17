package Orion.message.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtil {

    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    data class FriendRequest(val senderName: String)
    private val db = FirebaseFirestore.getInstance()

    // Crea un chatroom si no existe
    fun createChatRoom(contactUsername: String, currentUserId: String, callback: (chatroomId: String?) -> Unit) {
        val chatroomId = getChatRoomId(currentUserId, contactUsername)

        // Verifica si la sala de chat ya existe
        db.collection("chatrooms")
            .document(chatroomId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    // Si no existe, creamos un nuevo chatroom
                    val chatroomData = hashMapOf(
                        "users" to mapOf(
                            currentUserId to mapOf("username" to "Current User", "lastMessage" to ""),
                            contactUsername to mapOf("username" to contactUsername, "lastMessage" to "")
                        ),
                        "messages" to emptyList<String>()
                    )

                    // Crear el chatroom en la base de datos
                    db.collection("chatrooms")
                        .document(chatroomId)
                        .set(chatroomData)
                        .addOnSuccessListener {
                            callback(chatroomId)  // Devuelve el chatroomId
                        }
                        .addOnFailureListener { e ->
                            callback(null)
                        }
                } else {
                    // Si ya existe, solo devolvemos el chatroomId
                    callback(chatroomId)
                }
            }
            .addOnFailureListener { exception ->
                callback(null)
            }
    }

    // Función para obtener un ID único para el chatroom
    private fun getChatRoomId(currentUserId: String, contactUsername: String): String {
        return if (currentUserId < contactUsername) {
            currentUserId + "_" + contactUsername
        } else {
            contactUsername + "_" + currentUserId
        }
    }

    // Verificar si el usuario está autenticado
    fun isUserAuthenticated(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    fun logout() { FirebaseAuth.getInstance().signOut() }

    // Obtener el nombre de usuario actual
    fun getCurrentUsername(callback: (String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
        userRef.get().addOnSuccessListener { snapshot ->
            val username = snapshot.child("username").value as? String ?: ""
            callback(username)
        }.addOnFailureListener {
            callback("")
        }
    }

    // Obtener solicitudes de amistad
    fun getFriendRequests(currentUsername: String, callback: (List<FriendRequest>) -> Unit) {
        val friendRequestsRef = FirebaseDatabase.getInstance().reference.child("FriendRequests").child(currentUsername)
        friendRequestsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val friendRequests = mutableListOf<FriendRequest>()
                for (child in snapshot.children) {
                    val senderUsername = child.child("senderUsername").getValue(String::class.java)
                    val value = child.child("value").getValue(Int::class.java) ?: 0
                    if (senderUsername != null && value == 0) {
                        friendRequests.add(FriendRequest(senderUsername))
                    }
                }
                callback(friendRequests)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }

    // Aceptar solicitud de amistad
    fun acceptFriendRequest(currentUsername: String, senderUsername: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val contactsRef = FirebaseDatabase.getInstance().reference.child("Contacts")
        contactsRef.child(senderUsername).child(currentUsername).setValue(true)
        contactsRef.child(currentUsername).child(senderUsername).setValue(true)
            .addOnSuccessListener {
                rejectFriendRequest(currentUsername, senderUsername, onSuccess, onFailure)
            }
            .addOnFailureListener { onFailure(it.message ?: "Error al aceptar solicitud") }
    }

    // Rechazar solicitud de amistad
    fun rejectFriendRequest(currentUsername: String, senderUsername: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val friendRequestsRef = FirebaseDatabase.getInstance().reference.child("FriendRequests").child(currentUsername).child(senderUsername)
        friendRequestsRef.removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message ?: "Error al rechazar solicitud") }
    }

    // Ejecutar una acción solo si el usuario está autenticado
    fun executeIfAuthenticated(action: () -> Unit, onUnauthenticated: () -> Unit) {
        if (isUserAuthenticated()) {
            action()
        } else {
            onUnauthenticated()
        }
    }

    // Obtener UID del usuario autenticado
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Obtener referencia a un nodo específico
    fun getDatabaseReference(path: String): DatabaseReference {
        return database.reference.child(path)
    }

    // Obtener detalles del usuario actual (por UID)
    fun getCurrentUserDetails(callback: (Map<String, Any?>?) -> Unit) {
        val userId = getCurrentUserId()
        if (userId != null) {
            val userRef = getDatabaseReference("Users").child(userId)
            userRef.get().addOnSuccessListener { snapshot ->
                callback(snapshot.value as? Map<String, Any?>)
            }.addOnFailureListener {
                callback(null)
            }
        } else {
            callback(null)
        }
    }


    // Comprobar si un usuario existe por su nombre de usuario
    fun checkIfUserExists(username: String, callback: (Boolean) -> Unit) {
        val userRef = getDatabaseReference("Users").orderByChild("username").equalTo(username)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }

    // Comprobar si un contacto ya existe en la lista de contactos del usuario
    fun checkIfContactExists(currentUsername: String, contactUsername: String, callback: (Boolean) -> Unit) {
        val contactRef = getDatabaseReference("Contacts").child(currentUsername)
        contactRef.orderByChild("username").equalTo(contactUsername)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot.exists())
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false)
                }
            })
    }

    fun getUserContacts(username: String, callback: (List<Contact>) -> Unit) {
        val contactsRef = getDatabaseReference("Contacts").child(username)
        contactsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contactsList = mutableListOf<Contact>()
                val contactsCount = snapshot.childrenCount
                var loadedContacts = 0

                // Verificar si hay contactos
                if (contactsCount == 0L) {
                    callback(contactsList) // No hay contactos, retorna lista vacía
                    return
                }

                // Iterar sobre los contactos
                for (contactSnapshot in snapshot.children) {
                    val contactUsername = contactSnapshot.key // Este es el nombre del contacto
                    if (contactUsername != null) {
                        // Obtener los detalles del usuario de cada contacto
                        getUserDetailsByUsername(contactUsername) { userDetails ->
                            userDetails?.let { details ->
                                val contact = Contact(
                                    username = contactUsername,
                                    fullName = details["fullName"] as? String ?: "Desconocido",
                                    image = details["image"] as? String ?: ""
                                )
                                contactsList.add(contact)
                            }
                            // Asegurarse de que el callback se llame después de que todos los contactos se hayan cargado
                            loadedContacts++
                            if (loadedContacts == contactsCount.toInt()) {
                                callback(contactsList) // Retornar los contactos solo cuando todos estén cargados
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList()) // En caso de error, retorna lista vacía
            }
        })
    }

    fun getUserDetailsByUsername(username: String, callback: (Map<String, Any?>?) -> Unit) {
        val userRef = getDatabaseReference("Users").orderByChild("username").equalTo(username)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Verificar si encontramos algún usuario con ese nombre de usuario
                val userSnapshot = snapshot.children.firstOrNull()

                // Devolver los detalles del usuario si existe
                callback(userSnapshot?.value as? Map<String, Any?>)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null) // En caso de error, devolver null
            }
        })
    }

    fun checkIfContactsMatch(currentUsername: String, contactsList: List<Contact>, callback: (Boolean) -> Unit) {
        // Referencia a la base de datos en el nodo de contactos
        val contactsRef = getDatabaseReference("Contacts").child(currentUsername)

        // Recuperamos los contactos desde Firebase
        contactsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contactsFromDb = mutableListOf<Contact>()

                // Creamos una lista de contactos que tenemos en la base de datos
                for (contactSnapshot in snapshot.children) {
                    val contactUsername = contactSnapshot.key
                    if (contactUsername != null) {
                        // Obtener los detalles del usuario
                        getUserDetailsByUsername(contactUsername) { userDetails ->
                            userDetails?.let { details ->
                                val contact = Contact(
                                    username = contactUsername,
                                    fullName = details["fullName"] as? String ?: "Desconocido",
                                    image = details["image"] as? String ?: ""
                                )
                                contactsFromDb.add(contact)

                                // Si hemos obtenido todos los contactos, comparamos las dos listas
                                if (contactsFromDb.size == snapshot.childrenCount.toInt()) {
                                    val isMatch = compareContactsLists(contactsList, contactsFromDb)
                                    callback(isMatch) // Devolvemos si las listas coinciden
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false) // Si ocurre un error, devolvemos false
            }
        })
    }

    // Método para comparar dos listas de contactos
    private fun compareContactsLists(list1: List<Contact>, list2: List<Contact>): Boolean {
        if (list1.size != list2.size) return false

        // Compara cada contacto en las dos listas
        for (contact1 in list1) {
            val foundMatch = list2.any { contact2 ->
                contact1.username == contact2.username && contact1.fullName == contact2.fullName && contact1.image == contact2.image
            }
            if (!foundMatch) return false
        }

        return true
    }


    fun sendMessageToChat(chatroomId: String, messageText: String, callback: () -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val message = hashMapOf(
            "senderId" to currentUserId,
            "text" to messageText,
            "timestamp" to System.currentTimeMillis(),
            "type" to "text"
        )

        db.collection("chatrooms")
            .document(chatroomId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                callback()  // Llamar al callback cuando el mensaje se haya enviado correctamente
            }
            .addOnFailureListener {
                callback()  // Manejar errores si es necesario
            }
    }

    // Obtener los mensajes del chatroom
    fun getMessagesForChat(chatroomId: String, callback: (List<Message>) -> Unit) {
        db.collection("chatrooms")
            .document(chatroomId)
            .collection("messages")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val messages = mutableListOf<Message>()
                querySnapshot.forEach { document ->
                    val message = document.toObject(Message::class.java)
                    messages.add(message)
                }
                callback(messages)
            }
    }

    // Clase de datos Contact para manejar los contactos
    data class Contact(
        val username: String,
        val fullName: String,
        val image: String
    )
    // Clase de datos Contact para manejar los mensajes
    data class Message(
        val senderId: String = "",
        val text: String = "",
        val timestamp: Long = 0L,
        val type: String = ""
    )

}
