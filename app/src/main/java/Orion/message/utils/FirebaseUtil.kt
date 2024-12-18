package Orion.message.utils

import android.util.Log
import android.widget.Toast
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

    // Verificar si el usuario está autenticado
    fun isUserAuthenticated(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }


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

    fun getUserIdByUsername(username: String, callback: (String?) -> Unit) {
        val userRef = getDatabaseReference("Users").orderByChild("username").equalTo(username)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Tomar el primer usuario encontrado con ese nombre de usuario
                    val userSnapshot = snapshot.children.firstOrNull()
                    val userId = userSnapshot?.key // El userId es la clave del nodo
                    callback(userId)
                } else {
                    callback(null) // No se encontró el usuario
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null) // En caso de error, devolver null
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

    // Crear un chatroom si no existe
    fun createChatRoom(chatroomId: String, contactUsername: String, currentUsername: String, callback: (chatroomId: String?) -> Unit) {
        // Referencia al chatroom
        val chatroomRef = getDatabaseReference("chatrooms").child(chatroomId)

        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            Log.e("createChatRoom", "No se pudo obtener el currentUserId")
            callback(null)
            return
        }

        // Obtener el ID del contacto de forma asíncrona
        getUserIdByUsername(contactUsername) { contactId ->
            if (contactId == null) {
                Log.e("createChatRoom", "No se pudo encontrar el ID del contacto")
                callback(null)
                return@getUserIdByUsername
            }

            // Verificar si el chatroom ya existe
            chatroomRef.get().addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    // Crear un nuevo chatroom
                    val chatroomData = mapOf(
                        "users" to mapOf(
                            currentUserId to mapOf("username" to currentUsername, "state" to "online"),
                            contactId to mapOf("username" to contactUsername, "state" to "offline")
                        )
                    )

                    chatroomRef.setValue(chatroomData).addOnSuccessListener {
                        callback(chatroomId)
                    }.addOnFailureListener { e ->
                        Log.e("createChatRoom", "Error al crear chatroom: ${e.message}")
                        callback(null)
                    }
                } else {
                    // Si ya existe, devolver el chatroomId
                    callback(chatroomId)
                }
            }.addOnFailureListener { e ->
                Log.e("createChatRoom", "Error al verificar el chatroom: ${e.message}")
                callback(null)
            }
        }
    }


    fun checkChatroomExists(chatroomId: String, callback: (Boolean) -> Unit) {
        val chatroomRef = FirebaseUtil.getDatabaseReference("chatrooms").child(chatroomId)

        chatroomRef.get().addOnSuccessListener { snapshot ->
            callback(snapshot.exists()) // Devuelve true si el chatroom existe, false si no
        }.addOnFailureListener { e ->
            Log.e("checkChatroomExists", "Error al verificar el chatroom: ${e.message}")
            callback(false) // En caso de error, devuelve false
        }
    }


    // Enviar un mensaje al chatroom
    fun sendMessageToChat(chatroomId: String, message: Message, callback: (Boolean) -> Unit) {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            Log.e("sendMessage", "Usuario no autenticado.")
            callback(false)
            return
        }

        // Referencia a los mensajes del chatroom
        val messagesRef = getDatabaseReference("chatrooms").child(chatroomId).child("messages")

        // Crear un nuevo nodo para el mensaje
        val newMessageRef = messagesRef.push()
        val messageData = message.toMap()

        newMessageRef.setValue(messageData).addOnSuccessListener {
            callback(true)
        }.addOnFailureListener { e ->
            Log.e("sendMessage", "Error al enviar mensaje: ${e.message}")
            callback(false)
        }
    }

    // Obtener los mensajes del chatroom
    fun getMessagesForChat(chatroomId: String, callback: (List<Message>) -> Unit) {
        val messagesRef = FirebaseDatabase.getInstance().getReference("chatrooms").child(chatroomId).child("messages")
        messagesRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()

                // Recorrer todas las instancias de "messages" en el snapshot
                for (child in snapshot.children) {
                    val message = child.getValue(Message::class.java)  // Deserializa cada mensaje
                    if (message != null) {
                        Log.d("getMessagesForChat", "Mensaje: $message")
                        messages.add(message)  // Agrega el mensaje a la lista
                    }
                }

                // Al finalizar la lectura, invoca el callback con la lista de mensajes
                callback(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("getMessagesForChat", "Error al obtener mensajes: ${error.message}")
                callback(emptyList())  // En caso de error, devuelve una lista vacía
            }
        })
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
        val type: String = "",
        val formattedTime: String = "",
        val timestamp: Long = 0L // Establecemos un valor predeterminado para timestamp
    ) {
        // Método para convertir el objeto Message a un mapa (útil para Firebase)
        fun toMap(): Map<String, Any> {
            return mapOf(
                "senderId" to senderId,
                "text" to text,
                "timestamp" to timestamp,
                "type" to type,
                "formattedTime" to formattedTime
            )
        }
    }


}
