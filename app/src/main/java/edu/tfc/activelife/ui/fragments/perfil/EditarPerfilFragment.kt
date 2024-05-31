package edu.tfc.activelife.ui.fragments.perfil

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import edu.tfc.activelife.R
import edu.tfc.activelife.utils.Utils
import java.io.ByteArrayOutputStream

class EditarPerfilFragment : Fragment() {

    private lateinit var imageViewPerfil: ImageView
    private lateinit var editTextUsername: EditText
    private lateinit var buttonGuardarCambios: Button
    private lateinit var buttonEditarFoto: Button
    private lateinit var buttonEliminarFoto: ImageButton
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var imageBitmap: Bitmap? = null
    private var imageUri: Uri? = null
    private var currentUser = firebaseAuth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_editar_perfil, container, false)
        imageViewPerfil = view.findViewById(R.id.imageViewPerfil)
        editTextUsername = view.findViewById(R.id.editTextUsername)
        buttonGuardarCambios = view.findViewById(R.id.buttonGuardarCambios)
        buttonEditarFoto = view.findViewById(R.id.buttonEditarFoto)
        buttonEliminarFoto = view.findViewById(R.id.buttonEliminarFoto)

        buttonEditarFoto.setOnClickListener {
            Utils.showImagePickerDialog(this, requireContext(), "Editar Foto de Perfil") { bitmap, uri ->
                imageBitmap = bitmap
                imageUri = uri
                Utils.loadImageIntoView(imageViewPerfil, bitmap, uri, true)
            }
        }

        buttonEliminarFoto.setOnClickListener {
            eliminarFoto()
        }

        buttonGuardarCambios.setOnClickListener {
            guardarCambios()
        }

        cargarDatosUsuario()

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Utils.handleActivityResult(requestCode, resultCode, data) { bitmap, uri ->
            imageBitmap = bitmap
            imageUri = uri
            Utils.loadImageIntoView(imageViewPerfil, bitmap, uri, true)
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

        val currentUser = firebaseAuth.currentUser
        val userId = currentUser?.uid ?: return
        val updates = hashMapOf<String, Any>(
            "username" to username
        )

        if (imageBitmap != null || imageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("profileImages/$userId.jpg")

            val uploadTask = if (imageBitmap != null) {
                val baos = ByteArrayOutputStream()
                imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val imageData = baos.toByteArray()
                imageRef.putBytes(imageData)
            } else {
                imageRef.putFile(imageUri!!)
            }

            uploadTask.addOnSuccessListener {
                it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                    updates["imageUrl"] = uri.toString()
                    actualizarDatosUsuario(userId, updates)
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Error al subir imagen: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            actualizarDatosUsuario(userId, updates)
        }
    }

    private fun actualizarDatosUsuario(userId: String, updates: Map<String, Any>) {
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al actualizar el perfil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarFoto() {
        val userId = currentUser?.uid ?: return

        // Elimina la imagen de Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("profileImages/$userId.jpg")

        imageRef.delete().addOnSuccessListener {
            // Elimina la referencia de la imagen en Firestore
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("imageUrl", FieldValue.delete())
                .addOnSuccessListener {
                    // Resetea la vista a la imagen por defecto
                    imageBitmap = null
                    imageUri = null
                    imageViewPerfil.setImageResource(R.drawable.person)
                    Toast.makeText(context, "Foto de perfil eliminada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al eliminar referencia de imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Error al eliminar imagen: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
