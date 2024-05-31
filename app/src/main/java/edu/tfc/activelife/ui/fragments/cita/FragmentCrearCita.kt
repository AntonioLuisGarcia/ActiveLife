package edu.tfc.activelife.ui.fragments.cita

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import edu.tfc.activelife.R
import edu.tfc.activelife.utils.Utils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FragmentCrearCita : Fragment() {

    companion object {
        private const val PICK_MEDIA_REQUEST = 1
        private const val REQUEST_IMAGE_CAPTURE = 2
    }

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private val storage = Firebase.storage
    private lateinit var spinnerEncargados: Spinner
    private var encargadosList = arrayListOf<String>()
    private var encargadosMap = hashMapOf<String, String>()
    private var imageUrl: String = ""
    private var photoUri: Uri? = null
    private var currentPhotoPath: String? = null
    private lateinit var imageViewFoto: ImageView
    val btnGuardarCita: Button? = view?.findViewById(R.id.btn_guardar_cita)
    lateinit var btnEliminarFoto: Button


    //variable para saber si se esta cargando la imagen al storage
    private var isImageUploading = false
    private var imageBitmap: Bitmap? = null
    private var imageUri: Uri? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crear_cita, container, false)

        val editTituloCita: EditText = view.findViewById(R.id.edit_titulo_cita)
        val editDescripcionCita: EditText = view.findViewById(R.id.edit_descripcion_cita)
        val tvDate: TextView = view.findViewById(R.id.date_picker_cita)
        val btnGuardarCita: Button = view.findViewById(R.id.btn_guardar_cita)
        val btnTomarFoto: Button = view.findViewById(R.id.btn_tomar_foto)
        imageViewFoto = view.findViewById(R.id.image_view_foto)
        btnEliminarFoto = view.findViewById(R.id.btn_eliminar_foto)

        tvDate.setOnClickListener { showDatePickerDialog(tvDate) }

        val db = FirebaseFirestore.getInstance()
        val userUuid = FirebaseAuth.getInstance().currentUser?.uid

        btnTomarFoto.setOnClickListener {
            Utils.showImagePickerDialog(this, requireContext(), "Seleccionar Medio") { bitmap, uri ->
                imageBitmap = bitmap
                imageUri = uri
                Utils.loadImageIntoView(imageViewFoto, bitmap, uri, false)
                imageViewFoto.visibility = View.VISIBLE
                btnEliminarFoto.visibility = View.VISIBLE
                if (bitmap != null) {
                    uploadImageToFirebaseStorage(bitmap) { url ->
                        if (url != null) {
                            imageUrl = url
                        } else {
                            Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else if (uri != null) {
                    uploadMediaToFirebase(uri)
                }
            }
        }

        btnEliminarFoto.setOnClickListener {
            imageViewFoto.setImageBitmap(null)
            imageViewFoto.visibility = View.GONE
            btnEliminarFoto.visibility = View.GONE
            imageUrl = ""
        }

        val citaId = arguments?.getString("citaId")
        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        if (citaId != null) {
            db.collection("citas").document(citaId)
                .get()
                .addOnSuccessListener { document ->
                    val titulo = document.getString("titulo")
                    val descripcion = document.getString("descripcion")
                    val fecha = document.getDate("fechaCita")
                    val encargadoUuid = document.getString("encargadoUuid")

                    editTituloCita.setText(titulo)
                    editDescripcionCita.setText(descripcion)
                    if (fecha != null) {
                        tvDate.text = df.format(fecha)
                    }

                    btnGuardarCita.text = "Editar"
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Error al obtener los detalles de la cita",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            val calendar = Calendar.getInstance()
        }

        btnGuardarCita.setOnClickListener {
            if (isImageUploading) {
                Toast.makeText(requireContext(), "Por favor, espera a que se cargue la imagen", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tituloCita = editTituloCita.text.toString().trim()
            val descripcionCita = editDescripcionCita.text.toString().trim()
            val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = df.parse(tvDate.text.toString())
            var calendar = Calendar.getInstance()
            if (date != null) {
                calendar.time = date
            }

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            val selectedEncargadoName = spinnerEncargados.selectedItem.toString()
            val selectedEncargadoUuid = encargadosMap[selectedEncargadoName] ?: ""

            val nuevaCita = hashMapOf(
                "titulo" to tituloCita,
                "descripcion" to descripcionCita,
                "fechaCita" to calendar.time,
                "fechaSolicitud" to Date(),
                "userUuid" to userUuid,
                "encargadoUuid" to selectedEncargadoUuid,
                "image" to imageUrl
            )

            if (citaId != null) {
                db.collection("citas").document(citaId)
                    .update(nuevaCita as Map<String, Any>)
                    .addOnSuccessListener {
                        findNavController().navigateUp()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Error al editar la cita",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                db.collection("citas")
                    .add(nuevaCita)
                    .addOnSuccessListener { documentReference ->
                        editTituloCita.setText("")
                        editDescripcionCita.setText("")
                        findNavController().navigateUp()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Error al crear la cita",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Utils.handleActivityResult(requestCode, resultCode, data) { bitmap, uri ->
            imageBitmap = bitmap
            imageUri = uri
            Utils.loadImageIntoView(imageViewFoto, bitmap, uri, false)
            imageViewFoto.visibility = View.VISIBLE
            btnEliminarFoto?.visibility = View.VISIBLE
            if (bitmap != null) {
                uploadImageToFirebaseStorage(bitmap) { url ->
                    if (url != null) {
                        imageUrl = url
                    } else {
                        Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (uri != null) {
                uploadMediaToFirebase(uri)
            }
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinnerEncargados = view.findViewById(R.id.spinner_encargados)

        val citaId = arguments?.getString("citaId")
        if (citaId != null) {
            loadCitaDetails(citaId)
        } else {
            fetchEncargados()
        }
    }

    private fun loadCitaDetails(citaId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("citas").document(citaId).get().addOnSuccessListener { document ->
            fetchEncargados(document.getString("encargadoUuid"))
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to load details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchEncargados(selectedEncargadoUuid: String? = null) {
        encargadosList.add("Ninguno")
        encargadosMap["Ninguno"] = ""

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
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    encargadosList
                )
                spinnerEncargados.adapter = adapter

                selectedEncargadoUuid?.let { uuid ->
                    val selectedIndex = encargadosMap.values.toList().indexOf(uuid)
                    if (selectedIndex >= 0) {
                        spinnerEncargados.setSelection(selectedIndex + 1)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Error al obtener los encargados: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showMediaPickerDialog() {
        Utils.showImagePickerDialog(this, requireContext(), "Escoge una opción") { bitmap, uri ->
            imageBitmap = bitmap
            imageUri = uri
            Utils.loadImageIntoView(imageViewFoto, bitmap, uri, false)
            if (bitmap != null) {
                uploadImageToFirebaseStorage(bitmap) { url ->
                    if (url != null) {
                        imageUrl = url
                    } else {
                        Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (uri != null) {
                uploadMediaToFirebase(uri)
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            photoFile?.also {
                photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "edu.tfc.activelife.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }



    private fun uploadImageToFirebaseStorage(imageBitmap: Bitmap, callback: (String?) -> Unit) {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val imageRef = storage.reference.child("images/$userUid/${UUID.randomUUID()}.jpg")

        isImageUploading = true
        btnGuardarCita?.isEnabled = false

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        imageRef.putBytes(imageData)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    isImageUploading = false
                    btnGuardarCita?.isEnabled = true
                    callback(uri.toString())
                }.addOnFailureListener {
                    isImageUploading = false
                    btnGuardarCita?.isEnabled = true
                    Toast.makeText(requireContext(), "Failed to get download URL", Toast.LENGTH_SHORT).show()
                    callback(null)
                }
            }
            .addOnFailureListener {
                isImageUploading = false
                btnGuardarCita?.isEnabled = true
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                callback(null)
            }
    }

    private fun uploadMediaToFirebase(uri: Uri) {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val mediaRef = storage.reference.child("images/$userUid/${uri.lastPathSegment}")

        isImageUploading = true
        btnGuardarCita?.isEnabled = false

        mediaRef.putFile(uri)
            .addOnSuccessListener {
                mediaRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    isImageUploading = false
                    btnGuardarCita?.isEnabled = true
                    imageUrl = downloadUrl.toString()
                    Toast.makeText(requireContext(), "Media uploaded: $imageUrl", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    isImageUploading = false
                    btnGuardarCita?.isEnabled = true
                    Toast.makeText(requireContext(), "Failed to get download URL", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                isImageUploading = false
                btnGuardarCita?.isEnabled = true
                Toast.makeText(requireContext(), "Media upload failed", Toast.LENGTH_SHORT).show()
            }
    }



    private fun showDatePickerDialog(textView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDateCalendar = Calendar.getInstance()
            selectedDateCalendar.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0)
            selectedDateCalendar.set(Calendar.MILLISECOND, 0)

            val currentDateCalendar = Calendar.getInstance()
            currentDateCalendar.set(Calendar.HOUR_OF_DAY, 0)
            currentDateCalendar.set(Calendar.MINUTE, 0)
            currentDateCalendar.set(Calendar.SECOND, 0)
            currentDateCalendar.set(Calendar.MILLISECOND, 0)

            if (selectedDateCalendar.before(currentDateCalendar)) {
                Toast.makeText(requireContext(), "La fecha no puede ser anterior a la actual.", Toast.LENGTH_LONG).show()
            } else {
                val formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                textView.text = formattedDate
            }
        }, year, month, day)

        datePickerDialog.show()
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }


    private fun openMediaPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/* video/*"
        galleryLauncher.launch(intent)
    }


}
