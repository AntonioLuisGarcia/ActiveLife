package edu.tfc.activelife.ui.fragments.perfil

import android.content.Context
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

/**
 * EditarPerfilFragment allows users to view and edit their profile information, including username and profile picture.
 * Users can also choose different background colors for the fragment.
 */
class EditarPerfilFragment : Fragment() {

    private lateinit var imageViewPerfil: ImageView
    private lateinit var editTextUsername: EditText
    private lateinit var buttonGuardarCambios: Button
    private lateinit var buttonEditarFoto: Button
    private lateinit var buttonEliminarFoto: ImageButton
    private lateinit var textViewEmail: TextView
    private lateinit var textViewEmailValue: TextView
    private lateinit var buttonPrimary: Button
    private lateinit var buttonSecondary: Button
    private lateinit var buttonTertiary: Button
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var imageBitmap: Bitmap? = null
    private var imageUri: Uri? = null
    private var currentUser = firebaseAuth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_editar_perfil, container, false)

        // Initialize UI components
        imageViewPerfil = view.findViewById(R.id.imageViewPerfil)
        editTextUsername = view.findViewById(R.id.editTextUsername)
        buttonGuardarCambios = view.findViewById(R.id.buttonGuardarCambios)
        buttonEditarFoto = view.findViewById(R.id.buttonEditarFoto)
        buttonEliminarFoto = view.findViewById(R.id.buttonEliminarFoto)
        textViewEmail = view.findViewById(R.id.textViewEmail)
        textViewEmailValue = view.findViewById(R.id.textViewEmailValue)
        buttonPrimary = view.findViewById(R.id.buttonPrimary)
        buttonSecondary = view.findViewById(R.id.buttonSecondary)
        buttonTertiary = view.findViewById(R.id.buttonTertiary)

        // Set up button click listeners
        buttonEditarFoto.setOnClickListener {
            context?.getString(R.string.edit_image)?.let { it1 ->
                Utils.showImagePickerDialog(this, requireContext(),

                    it1, imageUri != null) { bitmap, uri ->
                    if (bitmap == null && uri == null) {
                        imageViewPerfil.setImageBitmap(null)
                        imageViewPerfil.visibility = View.GONE
                        buttonEliminarFoto.visibility = View.GONE
                        imageUri = null
                        imageBitmap = null
                    } else {
                        imageBitmap = bitmap
                        imageUri = uri
                        Utils.loadImageIntoView(imageViewPerfil, bitmap, uri, true)
                    }
                }
            }
        }

        applyBackgroundColor(view)

        buttonEliminarFoto.setOnClickListener {
            eliminarFoto()
        }

        buttonGuardarCambios.setOnClickListener {
            guardarCambios()
        }

        buttonPrimary.setOnClickListener {
            view.setBackgroundResource(R.drawable.gradient_primary)
            saveBackgroundColor(R.drawable.gradient_primary)
            updateBackgroundColor(R.drawable.gradient_primary)
        }

        buttonSecondary.setOnClickListener {
            view.setBackgroundResource(R.drawable.gradient_secondary)
            saveBackgroundColor(R.drawable.gradient_secondary)
            updateBackgroundColor(R.drawable.gradient_secondary)
        }

        buttonTertiary.setOnClickListener {
            view.setBackgroundResource(R.drawable.gradient_tertiary)
            saveBackgroundColor(R.drawable.gradient_tertiary)
            updateBackgroundColor(R.drawable.gradient_tertiary)
        }

        cargarDatosUsuario()

        return view
    }

    /**
     * Apply the saved background color to the given view.
     *
     * @param view The view to which the background color will be applied.
     */
    private fun applyBackgroundColor(view: View) {
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val colorResId = sharedPreferences.getInt("background_color", R.color.white)
        view.setBackgroundResource(colorResId)
    }

    /**
     * Update the background color of the activity's window decor view.
     *
     * @param colorResId The resource ID of the new background color.
     */
    private fun updateBackgroundColor(colorResId: Int) {
        activity?.window?.decorView?.setBackgroundResource(colorResId)
    }

    /**
     * Save the selected background color to shared preferences.
     *
     * @param colorResId The resource ID of the selected background color.
     */
    private fun saveBackgroundColor(colorResId: Int) {
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("background_color", colorResId).apply()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Utils.handleActivityResult(requestCode, resultCode, data) { bitmap, uri ->
            imageBitmap = bitmap
            imageUri = uri
            Utils.loadImageIntoView(imageViewPerfil, bitmap, uri, true)
        }
    }

    /**
     * Load the current user's profile data from Firestore and display it in the UI.
     */
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
                textViewEmailValue.text = currentUser?.email
            }
        }?.addOnFailureListener { exception ->
            Toast.makeText(context, "Error al cargar datos: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Save changes to the user's profile, including username and profile picture.
     */
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

    /**
     * Update the user's data in Firestore.
     *
     * @param userId The ID of the user whose data is being updated.
     * @param updates A map containing the fields to update and their new values.
     */
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

    /**
     * Delete the user's profile picture from Firebase Storage and remove the reference from Firestore.
     */
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
