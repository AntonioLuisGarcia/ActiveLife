package edu.tfc.activelife.ui.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
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
    private var currentUser = firebaseAuth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_editar_perfil, container, false)
        imageViewPerfil = view.findViewById(R.id.imageViewPerfil)
        editTextUsername = view.findViewById(R.id.editTextUsername)
        buttonGuardarCambios = view.findViewById(R.id.buttonGuardarCambios)
        buttonEditarFoto = view.findViewById(R.id.buttonEditarFoto)

        buttonEditarFoto.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }

        buttonGuardarCambios.setOnClickListener {
            guardarCambios()
        }

        cargarDatosUsuario()

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            imageBitmap = data?.extras?.get("data") as Bitmap
            imageViewPerfil.load(imageBitmap) {
                transformations(CircleCropTransformation())
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

        val baos = ByteArrayOutputStream()
        imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        // Subir imagen a Firebase Storage
        imageRef.putBytes(imageData)
            .addOnSuccessListener {
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
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al subir imagen: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }
}