package edu.tfc.activelife.ui.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import edu.tfc.activelife.R
import java.io.ByteArrayOutputStream
import java.util.*

class FragmentCrearCita : Fragment() {

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private val storage = Firebase.storage
    private lateinit var spinnerEncargados: Spinner
    private var encargadosList = arrayListOf<String>()
    private var encargadosMap = hashMapOf<String, String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_crear_cita, container, false)

        // Referencias a los elementos de la interfaz
        val editTituloCita: EditText = view.findViewById(R.id.edit_titulo_cita)
        val editDescripcionCita: EditText = view.findViewById(R.id.edit_descripcion_cita)
        val datePickerCita: DatePicker = view.findViewById(R.id.date_picker_cita)
        val btnGuardarCita: Button = view.findViewById(R.id.btn_guardar_cita)
        val btnTomarFoto: Button = view.findViewById(R.id.btn_tomar_foto)

        // Obtener la instancia de Firestore
        val db = FirebaseFirestore.getInstance()

        val userUuid = FirebaseAuth.getInstance().currentUser?.uid

        var imageUrl = ""

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // La imagen se capturó correctamente
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                // Subir la imagen a Firebase Storage y obtener la URL
                uploadImageToFirebaseStorage(imageBitmap) { url ->
                    // Aquí puedes manejar la URL de la imagen devuelta
                    if (url != null) {
                        // La imagen se subió correctamente, asignar el blob
                        imageUrl = url
                    } else {
                        // Ocurrió un error al subir la imagen
                        Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnTomarFoto.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
                cameraLauncher.launch(takePictureIntent)
            }
        }

        // Obtener el ID de la cita a editar (si existe)
        val citaId = arguments?.getString("citaId")
        if (citaId != null) {
            // Se está editando una cita existente
            // Obtener los detalles de la cita existente y establecerlos en los campos correspondientes
            db.collection("citas").document(citaId)
                .get()
                .addOnSuccessListener { document ->
                    val titulo = document.getString("titulo")
                    val descripcion = document.getString("descripcion")
                    val fecha = document.getDate("fechaCita")

                    editTituloCita.setText(titulo)
                    editDescripcionCita.setText(descripcion)
                    // Configurar la fecha del DatePicker
                    val calendar = Calendar.getInstance()
                    calendar.time = fecha
                    datePickerCita.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null)

                    // Cambiar el texto del botón a "Editar"
                    btnGuardarCita.text = "Editar"
                }
                .addOnFailureListener { exception ->
                    // Manejar el error
                    Toast.makeText(requireContext(), "Error al obtener los detalles de la cita", Toast.LENGTH_SHORT).show()
                }
        } else {
            // No hay cita para editar, estamos creando una nueva cita
            // Configurar la fecha del DatePicker con la fecha actual
            val calendar = Calendar.getInstance()
            datePickerCita.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null)
        }

        // Manejador del clic del botón
        btnGuardarCita.setOnClickListener {
            // Obtener el título de la cita
            val tituloCita = editTituloCita.text.toString().trim()

            // Obtener la descripción de la cita
            val descripcionCita = editDescripcionCita.text.toString().trim()

            // Obtener la fecha de la cita del DatePicker
            val year = datePickerCita.year
            val month = datePickerCita.month
            val day = datePickerCita.dayOfMonth

            // Crear un objeto Calendar para la fecha seleccionada
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            val selectedEncargadoName = spinnerEncargados.selectedItem.toString()
            val selectedEncargadoUuid = encargadosMap[selectedEncargadoName] ?: ""

            val nuevaCita = hashMapOf(
                "titulo" to tituloCita,
                "descripcion" to descripcionCita,
                "fechaCita" to calendar.time, // Convertir el objeto Calendar a Date
                "fechaSolicitud" to Date(), // Fecha y hora actuales
                "userUuid" to userUuid,
                "encargadoUuid" to selectedEncargadoUuid, // UUID del encargado seleccionado
                "image" to imageUrl
            )

            // Subir los datos de la nueva cita a Firestore
            if (citaId != null) {
                // Se está editando una cita existente
                // Actualizar los detalles de la cita en Firestore
                db.collection("citas").document(citaId)
                    .update(nuevaCita as Map<String, Any>)
                    .addOnSuccessListener {
                        // La cita se actualizó correctamente
                        // Navegar de regreso al FragmentThree
                        findNavController().navigateUp()
                    }
                    .addOnFailureListener { e ->
                        // Manejar el error
                        Toast.makeText(requireContext(), "Error al editar la cita", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Se está creando una nueva cita
                db.collection("citas")
                    .add(nuevaCita)
                    .addOnSuccessListener { documentReference ->
                        // La cita se agregó correctamente
                        // Limpiar los EditText después de guardar la cita
                        editTituloCita.setText("")
                        editDescripcionCita.setText("")
                        // Navegar de regreso al FragmentThree
                        findNavController().navigateUp()
                    }
                    .addOnFailureListener { e ->
                        // Manejar el error
                        Toast.makeText(requireContext(), "Error al crear la cita", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinnerEncargados = view.findViewById(R.id.spinner_encargados)

        // Llenar el spinner con los encargados
        fetchEncargados()
    }

    private fun fetchEncargados() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("admin", true)
            .whereEqualTo("aceptado", true)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val username = document.getString("username") ?: "Unknown"
                    val userId = document.id
                    encargadosList.add(username)
                    encargadosMap[username] = userId
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, encargadosList)
                spinnerEncargados.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error al obtener los encargados: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebaseStorage(imageBitmap: Bitmap, callback: (String?) -> Unit) {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val imageRef = storage.reference.child("images/$userUid/${UUID.randomUUID()}.jpg")

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        imageRef.putBytes(imageData)
            .addOnSuccessListener { uploadTask ->
                // La imagen se subió correctamente, obtener la URL de descarga
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Llamar al callback con la URL de la imagen
                    callback(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                // Manejar el error
                Toast.makeText(requireContext(), "Error al subir la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                // Llamar al callback con null en caso de error
                callback(null)
            }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // La imagen se capturó correctamente
            val imageBitmap = data?.extras?.get("data") as Bitmap
            // Aquí puedes hacer lo que quieras con la imagen capturada, como guardarla o mostrarla en un ImageView
            // Por ejemplo, puedes mostrar la imagen en un ImageView
            // imageView.setImageBitmap(imageBitmap)
        }
    }
}
