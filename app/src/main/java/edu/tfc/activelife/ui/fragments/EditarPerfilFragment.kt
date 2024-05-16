package edu.tfc.activelife.ui.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import edu.tfc.activelife.R
import java.io.ByteArrayOutputStream

class EditarPerfilFragment : Fragment() {
    private lateinit var imageViewPerfil: ImageView
    private lateinit var editTextUsername: EditText
    private lateinit var buttonGuardarCambios: Button
    private lateinit var buttonEditarFoto: Button
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var imageBitmap: Bitmap? = null // Guardar el bitmap de la nueva imagen
    private var imageUri: Uri? = null // Guardar el URI de la imagen seleccionada
    private var currentUser = firebaseAuth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_editar_perfil, container, false)
        imageViewPerfil = view.findViewById(R.id.imageViewPerfil)
        editTextUsername = view.findViewById(R.id.editTextUsername)
        buttonGuardarCambios = view.findViewById(R.id.buttonGuardarCambios)
        buttonEditarFoto = view.findViewById(R.id.buttonEditarFoto)

        buttonEditarFoto.setOnClickListener {
            val options = arrayOf<CharSequence>("Tomar Foto", "Elegir de la Galería", "Cancelar")
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Editar Foto de Perfil")
            builder.setItems(options) { dialog, item ->
                when (options[item]) {
                    "Tomar Foto" -> {
                        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                    "Elegir de la Galería" -> {
                        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK)
                    }
                    "Cancelar" -> dialog.dismiss()
                }
            }
            builder.show()
        }

        buttonGuardarCambios.setOnClickListener {
            guardarCambios()
        }

        cargarDatosUsuario()

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    imageBitmap = data?.extras?.get("data") as Bitmap
                    imageViewPerfil.load(imageBitmap) {
                        transformations(CircleCropTransformation())
                    }
                }
                REQUEST_IMAGE_PICK -> {
                    imageUri = data?.data
                    imageViewPerfil.load(imageUri) {
                        transformations(CircleCropTransformation())
                    }
                }
            }
        }
    }

    private fun cargarDatosUsuario() {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")
        val userDocument = currentUser?.let { usersCollection.document(it.uid) }

        userDocument?.get()?.addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val imageUrl = documentSnapshot.getString("imageUrl")
                if (!imageUrl.isNullOrEmpty()) {
                    imageViewPerfil.load(imageUrl) {
                        transformations(CircleCropTransformation())
                    }
                }
                editTextUsername.setText(documentSnapshot.getString("username"))
            }
        }?.addOnFailureListener { exception ->
            Toast.makeText(context, "Error al cargar datos: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarCambios() {
        val username = editTextUsername.text.toString().trim()
        if (username.isBlank()) {
            Toast.makeText(context, "El nombre de usuario no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener referencia a Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference
        val currentUser = firebaseAuth.currentUser
        val userId = currentUser?.uid ?: return
        val imageRef = storageRef.child("profileImages/$userId.jpg")

        // Subir la imagen seleccionada
        val uploadTask = if (imageBitmap != null) {
            val baos = ByteArrayOutputStream()
            imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageData = baos.toByteArray()
            imageRef.putBytes(imageData)
        } else if (imageUri != null) {
            imageRef.putFile(imageUri!!)
        } else {
            Toast.makeText(context, "No se ha seleccionado ninguna imagen", Toast.LENGTH_SHORT).show()
            return
        }

        // Continuar con el guardado después de subir la imagen
        uploadTask.addOnSuccessListener {
            it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                // Actualizar datos de usuario en Firestore con la nueva imagen y el nombre
                val updates = hashMapOf<String, Any>(
                    "username" to username,
                    "imageUrl" to imageUrl
                )
                FirebaseFirestore.getInstance().collection("users").document(userId)
                    .update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error al actualizar el perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Error al subir imagen: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_PICK = 2
    }
}
