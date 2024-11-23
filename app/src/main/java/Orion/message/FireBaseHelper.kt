package Orion.message

import com.google.firebase.database.FirebaseDatabase

class FirebaseHelper {

    private val database = FirebaseDatabase.getInstance()
    private val userRef = database.reference.child("Users")

    fun saveUserData(userId: String, userMap: Map<String, String>, onComplete: (Boolean, String?) -> Unit) {
        userRef.child(userId).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }
}
