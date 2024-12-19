package Orion.message

import Orion.message.utils.FirebaseUtil
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ShapeableImageView
    private lateinit var editIcon: ImageView
    private lateinit var fullNameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var statusEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var saveButton: Button

    private val databaseReference = FirebaseUtil.getDatabaseReference("pictures")
    private val db = FirebaseUtil.getDatabaseReference("Users")
    private val userId = FirebaseUtil.getCurrentUserId()
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
            db.child(userId).get().addOnSuccessListener { snapshot ->
                val fullName = snapshot.child("fullName").value as? String ?: ""
                val username = snapshot.child("username").value as? String ?: ""
                val status = snapshot.child("status").value as? String ?: ""
                val email = snapshot.child("email").value as? String ?: ""


                fullNameEditText.setText(fullName)
                usernameEditText.setText(username)
                statusEditText.setText(status)
                emailEditText.setText(email)

                // Primero, verificamos si el usuario tiene una imagen de perfil
                FirebaseUtil.checkIfUserHasImage(username, // Reemplaza con el nombre de usuario que desees
                    callback = { hasImage ->
                        if (hasImage) {
                            // Si el usuario tiene una imagen, la cargamos
                            FirebaseUtil.loadUserProfileImage(this@EditProfileActivity, username) { imageFile ->
                                // Verificamos si tenemos un archivo de imagen
                                imageFile?.let {
                                    // Usamos Glide para cargar la imagen desde el archivo
                                    Glide.with(this@EditProfileActivity)
                                        .load(it)  // Cargar el archivo de imagen
                                        .apply(RequestOptions.circleCropTransform())  // Transformación circular
                                        .into(profileImageView)  // Colocamos la imagen en el ImageView
                                } ?: run {
                                    // Si no se encuentra la imagen, mostramos un error
                                    Toast.makeText(this@EditProfileActivity, "No se encontró la imagen", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            // Si el usuario no tiene imagen de perfil, mostramos un mensaje
                            Toast.makeText(this@EditProfileActivity, "No hay imagen", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onError = { errorMessage ->
                        // En caso de error, mostramos un mensaje
                        Toast.makeText(this@EditProfileActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )


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

                db.child(userId!!).updateChildren(updates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                        uploadProfileImage()

                        // Regresar a la actividad anterior después de guardar los cambios
                        finish()
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
            try {
                // Convertir la imagen a Base64
                val base64Image = convertImageToBase64(uri)

                // Guardar la imagen como Base64 en la base de datos
                databaseReference.child(userId!!).child("profileImage").setValue(base64Image)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Foto de perfil guardada correctamente", Toast.LENGTH_SHORT).show()
                        // Puedes cargar la imagen directamente desde Base64 si es necesario
                        Glide.with(this)
                            .load(base64Image)
                            .apply(RequestOptions.circleCropTransform()) // Imagen circular
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(profileImageView)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar la referencia en la base de datos", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(this, "Error al guardar la foto de perfil: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            Toast.makeText(this, "Error: URI de imagen no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para convertir una imagen a Base64
    fun convertImageToBase64(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

}

