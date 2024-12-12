package Orion.message

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ShapeableImageView
    private lateinit var editIcon: ImageView
    private lateinit var fullNameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var statusEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var saveButton: Button

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().reference.child("Users")
    private val storageReference = FirebaseStorage.getInstance().reference.child("ProfileImages")
    private val userId = firebaseAuth.currentUser?.uid

    private val PICK_IMAGE_REQUEST = 101
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profileImageView = findViewById(R.id.profileImage)
        editIcon = findViewById(R.id.editIcon)
        fullNameEditText = findViewById(R.id.fullNameEditText)
        usernameEditText = findViewById(R.id.usernameEditText)
        statusEditText = findViewById(R.id.statusEditText)
        emailEditText = findViewById(R.id.emailEditText)
        saveButton = findViewById(R.id.saveButton)

        // Cargar datos del usuario
        if (userId != null) {
            databaseReference.child(userId).get().addOnSuccessListener { snapshot ->
                val fullName = snapshot.child("fullName").value as? String ?: ""
                val username = snapshot.child("username").value as? String ?: ""
                val status = snapshot.child("status").value as? String ?: ""
                val email = snapshot.child("email").value as? String ?: ""
                val profileImageUrl = snapshot.child("profileImageUrl").value as? String

                fullNameEditText.setText(fullName)
                usernameEditText.setText(username)
                statusEditText.setText(status)
                emailEditText.setText(email)

                if (!profileImageUrl.isNullOrEmpty()) {
                    Picasso.get().load(profileImageUrl).into(profileImageView)
                }
            }
        }

        // Hacer clic en el ícono de edición para cambiar la foto
        editIcon.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Guardar cambios
        saveButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString().trim()
            val status = statusEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()

            if (fullName.isNotEmpty() && status.isNotEmpty() && email.isNotEmpty()) {
                val updates = mapOf(
                    "fullName" to fullName,
                    "status" to status,
                    "email" to email
                )

                databaseReference.child(userId!!).updateChildren(updates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                        uploadProfileImage()
                    } else {
                        Toast.makeText(this, "Error al actualizar el perfil: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            profileImageView.setImageURI(imageUri)
        }
    }

    private fun uploadProfileImage() {
        imageUri?.let { uri ->
            val fileRef = storageReference.child("$userId.jpg")
            fileRef.putFile(uri).addOnCompleteListener { uploadTask ->
                if (uploadTask.isSuccessful) {
                    fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        databaseReference.child(userId!!).child("profileImageUrl").setValue(downloadUri.toString())
                    }
                } else {
                    Toast.makeText(this, "Error al subir la foto de perfil: ${uploadTask.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
