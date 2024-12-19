package Orion.message

import Orion.message.utils.FirebaseUtil
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

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
    private val CAPTURE_IMAGE_REQUEST = 102
    private val DELETE_IMAGE_REQUEST = 103
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

                FirebaseUtil.loadUserProfileImage(this, username) { imageFile ->
                    imageFile?.let {
                        Glide.with(this)
                            .load(it)
                            .apply(RequestOptions.circleCropTransform())
                            .into(profileImageView)
                    }
                }
            }
        }

        // Abrir el diálogo para cambiar la foto de perfil
        editIcon.setOnClickListener {
            showImageSelectionDialog()
        }

        // Guardar cambios de perfil
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
                        finish()                    } else {
                        Toast.makeText(this, "Error al actualizar el perfil: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Muestra un diálogo para seleccionar entre opciones
    private fun showImageSelectionDialog() {
        val options = arrayOf("Tomar una foto", "Elegir de la galería", "Eliminar foto actual")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona una opción")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> takePhoto() // Tomar una foto con la cámara
                    1 -> choosePhoto() // Elegir una foto desde la galería
                    2 -> removePhoto() // Eliminar la foto actual
                }
            }
        builder.show()
    }

    // Tomar una foto con la cámara
    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAPTURE_IMAGE_REQUEST)
    }

    // Elegir una foto desde la galería
    private fun choosePhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Eliminar la foto actual y poner la imagen por defecto
    private fun removePhoto() {
        profileImageView.setImageResource(R.drawable.ic_profile_placeholder) // Imagen por defecto
        FirebaseUtil.deleteUserProfileImageFromDatabase(this) // Eliminar la imagen en Firebase
    }

    // Al recibir la respuesta de la actividad
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    imageUri = data?.data
                    profileImageView.setImageURI(imageUri)
                    uploadProfileImage()
                }
                CAPTURE_IMAGE_REQUEST -> {
                    val photo = data?.extras?.get("data") as Bitmap
                    imageUri = getImageUri(photo)
                    profileImageView.setImageBitmap(photo)
                    uploadProfileImage()
                }
            }
        }
    }

    private fun getImageUri(photo: Bitmap): Uri {
        val path = MediaStore.Images.Media.insertImage(contentResolver, photo, "profile_photo", null)
        return Uri.parse(path)
    }

    // Subir la imagen a Firebase
    private fun uploadProfileImage() {
        imageUri?.let { uri ->
            contentResolver.openInputStream(uri)?.let { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val base64Image = convertImageToBase64(bitmap)

                databaseReference.child(userId!!).child("profileImage").setValue(base64Image)
                    .addOnSuccessListener {
                        Glide.with(this)
                            .load(uri)
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(profileImageView)
                        Toast.makeText(this, "Foto de perfil guardada correctamente", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al guardar la foto: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // Función para convertir la imagen a Base64
    private fun convertImageToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}
