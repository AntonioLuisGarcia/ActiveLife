package edu.tfc.activelife.ui.fragments.cita

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import edu.tfc.activelife.R
import edu.tfc.activelife.utils.Utils
import java.io.ByteArrayOutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for creating or editing an appointment.
 * This fragment provides functionalities for selecting a date, taking or selecting a photo,
 * and saving the appointment details to Firestore.
 */
class FragmentCrearCita : Fragment() {

    private val storage = Firebase.storage
    private lateinit var spinnerEncargados: Spinner
    private var encargadosList = arrayListOf<String>()
    private var encargadosMap = hashMapOf<String, String>()
    private var imageUrl: String = ""
    private lateinit var imageViewFoto: ImageView
    private var btnGuardarCita: Button? = null
    private lateinit var btnEliminarFoto: Button
    private var isImageUploading = false
    private var imageBitmap: Bitmap? = null
    private var imageUri: Uri? = null

    /**
     * Initializes the view for the fragment and sets up the UI elements and event listeners.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crear_cita, container, false)
        applyBackgroundColor(view)
        val editTituloCita: EditText = view.findViewById(R.id.edit_titulo_cita)
        val editDescripcionCita: EditText = view.findViewById(R.id.edit_descripcion_cita)
        val tvDate: TextView = view.findViewById(R.id.date_picker_cita)
        btnGuardarCita = view.findViewById(R.id.btn_guardar_cita)
        val btnTomarFoto: Button = view.findViewById(R.id.btn_tomar_foto)
        imageViewFoto = view.findViewById(R.id.image_view_foto)
        btnEliminarFoto = view.findViewById(R.id.btn_eliminar_foto)

        var hasPhoto = false

        tvDate.setOnClickListener { showDatePickerDialog(tvDate) }

        val db = FirebaseFirestore.getInstance()
        val userUuid = FirebaseAuth.getInstance().currentUser?.uid

        btnTomarFoto.setOnClickListener {
            context?.let { it1 ->
                Utils.showImagePickerDialog(this, requireContext(), it1.getString(R.string.take_photo), hasPhoto) { bitmap, uri ->
                    if (bitmap == null && uri == null) {
                        imageViewFoto.setImageBitmap(null)
                        imageViewFoto.visibility = View.GONE
                        btnEliminarFoto.visibility = View.GONE
                        imageUrl = ""
                        hasPhoto = false
                    } else {
                        imageBitmap = bitmap
                        imageUri = uri
                        Utils.loadImageIntoView(imageViewFoto, bitmap, uri, false)
                        imageViewFoto.visibility = View.VISIBLE
                        btnEliminarFoto.visibility = View.VISIBLE
                        hasPhoto = true
                        if (bitmap != null) {
                            uploadImageToFirebaseStorage(bitmap) { url ->
                                if (url != null) {
                                    imageUrl = url
                                } else {
                                    Toast.makeText(requireContext(), "Error uploading image", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else if (uri != null) {
                            uploadMediaToFirebase(uri)
                        }
                    }
                }
            }
        }

        btnEliminarFoto.setOnClickListener {
            imageViewFoto.setImageBitmap(null)
            imageViewFoto.visibility = View.GONE
            btnEliminarFoto.visibility = View.GONE
            imageUrl = ""
            hasPhoto = false
        }

        val citaId = arguments?.getString("citaId")
        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        if (citaId != null && citaId.isNotEmpty()) {
            db.collection("citas").document(citaId)
                .get()
                .addOnSuccessListener { document ->
                    val titulo = document.getString("titulo")
                    val descripcion = document.getString("descripcion")
                    val fecha = document.getDate("fechaCita")
                    val encargadoUuid = document.getString("encargadoUuid")
                    val imageUrl = document.getString("imagen")

                    editTituloCita.setText(titulo)
                    editDescripcionCita.setText(descripcion)
                    if (fecha != null) {
                        tvDate.text = df.format(fecha)
                    }
                    if (imageUrl != null && imageUrl.isNotEmpty()) {
                        this.imageUrl = imageUrl
                        Utils.loadImageIntoView(imageViewFoto, null, Uri.parse(imageUrl), false)
                        imageViewFoto.visibility = View.VISIBLE
                        btnEliminarFoto.visibility = View.VISIBLE
                        hasPhoto = true
                    }

                    btnGuardarCita?.text = "Editar"
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Error getting appointment details",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            val calendar = Calendar.getInstance()
        }

        btnGuardarCita?.setOnClickListener {
            if (isImageUploading) {
                Toast.makeText(requireContext(), "Please wait for the image to upload", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tituloCita = editTituloCita.text.toString().trim()
            val descripcionCita = editDescripcionCita.text.toString().trim()
            val fechaCita = tvDate.text.toString().trim()

            if (!validateForm(tituloCita, descripcionCita, fechaCita)) {
                return@setOnClickListener
            }

            val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date: Date?
            try {
                date = df.parse(fechaCita)
            } catch (e: ParseException) {
                Toast.makeText(requireContext(), "Invalid date format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
                "userUUID" to userUuid,
                "encargadoUuid" to selectedEncargadoUuid,
                "imagen" to imageUrl
            )

            if (citaId != null && citaId.isNotEmpty()) {
                db.collection("citas").document(citaId)
                    .update(nuevaCita as Map<String, Any>)
                    .addOnSuccessListener {
                        findNavController().navigate(R.id.action_fragmentCrearCita_to_fragmentThree)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Error editing appointment",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                db.collection("citas")
                    .add(nuevaCita)
                    .addOnSuccessListener { documentReference ->
                        editTituloCita.setText("")
                        editDescripcionCita.setText("")
                        findNavController().navigate(R.id.action_fragmentCrearCita_to_fragmentThree)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Error creating appointment",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }

        return view
    }

                /**
     * Applies background color to the view based on the stored preferences.
     *
     * @param view The view to which the background color will be applied.
     */
    private fun applyBackgroundColor(view: View) {
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val colorResId = sharedPreferences.getInt("background_color", R.color.white)
        view.setBackgroundResource(colorResId)
    }

    /**
     * Handles the result from an activity that was started for a result, such as image picking or capturing.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
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

    /**
     * This method is called when the view is created.
     *
     * @param view The created view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyBackgroundColor(view)
        spinnerEncargados = view.findViewById(R.id.spinner_encargados)

        val citaId = arguments?.getString("citaId")
        if (citaId != null && citaId.isNotEmpty()) {
            loadCitaDetails(citaId)
        } else {
            fetchEncargados()
        }
    }

    /**
     * Loads the details of a specific "cita" from Firestore.
     *
     * @param citaId The ID of the "cita" to load.
     */
    private fun loadCitaDetails(citaId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("citas").document(citaId).get().addOnSuccessListener { document ->
            fetchEncargados(document.getString("encargadoUuid"))
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to load details", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Fetches the list of "encargados" from Firestore.
     *
     * @param selectedEncargadoUuid The UUID of the selected "encargado", if any. Defaults to null.
     */
    private fun fetchEncargados(selectedEncargadoUuid: String? = null) {
        encargadosList.add("Ninguno")
        encargadosMap["Ninguno"] = ""

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("admin", true)
            .whereEqualTo("aceptado", true)
            .get()
            .addOnSuccessListener { documents ->
                var selectedIndex = 0
                var currentIndex = 1 // Starts at 1 because "Ninguno" is at position 0

                for (document in documents) {
                    val username = document.getString("username") ?: "Unknown"
                    val userId = document.id
                    encargadosList.add(username)
                    encargadosMap[username] = userId

                    if (selectedEncargadoUuid == userId) {
                        selectedIndex = currentIndex
                    }
                    currentIndex++
                }

                val adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.spinner_item,
                    encargadosList
                )
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                spinnerEncargados.adapter = adapter

                if (selectedIndex >= 0) {
                    spinnerEncargados.setSelection(selectedIndex)
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

    /**
     * Uploads an image to Firebase Storage.
     *
     * @param imageBitmap The bitmap of the image to be uploaded.
     * @param callback A callback function that is called with the URL of the uploaded image, or null if the upload failed.
     */
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

    /**
     * Uploads media to Firebase Storage.
     *
     * @param uri The URI of the media to be uploaded.
     */
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

    /**
     * Shows a date picker dialog.
     *
     * @param textView The TextView to display the selected date.
     */
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

    /**
     * Validates the title of the "cita".
     *
     * @param title The title to validate.
     * @return True if the title is not empty, false otherwise.
     */
    private fun validateTitle(title: String): Boolean {
        return if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor ingresa un título para la cita", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    /**
     * Validates the description of the "cita".
     *
     * @param description The description to validate.
     * @return True if the description is not empty, false otherwise.
     */
    private fun validateDescription(description: String): Boolean {
        return if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor ingresa una descripción para la cita", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    /**
     * Validates the date of the "cita".
     *
     * @param date The date to validate.
     * @return True if the date is not empty and is not the default "Selecciona una fecha", false otherwise.
     */
    private fun validateDate(date: String): Boolean {
        return if (date == "Selecciona una fecha" || date.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor selecciona una fecha para la cita", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    /**
     * Validates the entire form.
     *
     * @param title The title to validate.
     * @param description The description to validate.
     * @param date The date to validate.
     * @return True if all fields are valid, false otherwise.
     */
    private fun validateForm(title: String, description: String, date: String): Boolean {
        return validateTitle(title) && validateDescription(description) && validateDate(date)
    }
}
