package edu.tfc.activelife.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import edu.tfc.activelife.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ExerciseFragment : Fragment() {

    companion object {
        fun newInstance(): ExerciseFragment {
            return ExerciseFragment()
        }
        private const val PICK_MEDIA_REQUEST = 1
        private const val REQUEST_IMAGE_CAPTURE = 2
    }

    var exerciseDataListener: ExerciseDataListener? = null
    lateinit var editTextExerciseName: EditText
    lateinit var editTextSeries: EditText
    lateinit var editTextRepetitions: EditText
    lateinit var imageViewExerciseMedia: ImageView
    lateinit var buttonAddMedia: Button
    lateinit var buttonRemoveExercise: Button
    var gifUrl: Uri? = null
    private var photoUri: Uri? = null
    private var currentPhotoPath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercise, container, false)
        editTextExerciseName = view.findViewById(R.id.editTextExerciseName)
        editTextSeries = view.findViewById(R.id.editTextSeries)
        editTextRepetitions = view.findViewById(R.id.editTextRepetitions)
        imageViewExerciseMedia = view.findViewById(R.id.imageViewExerciseMedia)
        buttonAddMedia = view.findViewById(R.id.buttonAddMedia)
        buttonRemoveExercise = view.findViewById(R.id.buttonRemoveExercise)

        arguments?.let {
            editTextExerciseName.setText(it.getString("name"))
            editTextSeries.setText(it.getString("serie"))
            editTextRepetitions.setText(it.getString("repeticiones"))
            val mediaUrl = it.getString("gifUrl")
            if (mediaUrl != null) {
                gifUrl = Uri.parse(mediaUrl)  // Actualizamos gifUrl aquí para que se pueda coger en el FragmentOne
                if (isAdded) {
                    Glide.with(this).load(mediaUrl).into(imageViewExerciseMedia)
                }
                imageViewExerciseMedia.visibility = View.VISIBLE
            }
        }

        buttonAddMedia.setOnClickListener {
            showMediaPickerDialog()
        }

        buttonRemoveExercise.setOnClickListener {
            exerciseDataListener?.onRemoveExercise(this)
        }

        return view
    }

    private fun showMediaPickerDialog() {
        val options = arrayOf("Tomar Foto", "Seleccionar de Galería")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Escoge una opción")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> dispatchTakePictureIntent()
                1 -> openMediaPicker()
            }
        }
        builder.show()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
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
                takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = requireActivity().getExternalFilesDir(null)
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
        startActivityForResult(intent, PICK_MEDIA_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_MEDIA_REQUEST -> {
                    gifUrl = data?.data
                    if (gifUrl != null) {
                        imageViewExerciseMedia.visibility = View.VISIBLE
                        if (isAdded) {
                            Glide.with(this).load(gifUrl).into(imageViewExerciseMedia)
                        }
                        uploadMediaToFirebase(gifUrl)
                    }
                }
                REQUEST_IMAGE_CAPTURE -> {
                    gifUrl = photoUri
                    if (gifUrl != null) {
                        imageViewExerciseMedia.visibility = View.VISIBLE
                        if (isAdded) {
                            Glide.with(this).load(gifUrl).into(imageViewExerciseMedia)
                        }
                        uploadMediaToFirebase(gifUrl)
                    }
                }
            }
        }
    }

    private fun uploadMediaToFirebase(uri: Uri?) {
        uri?.let {
            val storageRef = FirebaseStorage.getInstance().reference.child("exercise_media/${uri.lastPathSegment}")
            storageRef.putFile(uri).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    gifUrl = downloadUrl
                    if (isAdded) {
                        Glide.with(this).load(gifUrl).into(imageViewExerciseMedia)
                    }
                    Toast.makeText(requireContext(), "Media uploaded: $gifUrl", Toast.LENGTH_SHORT).show()
                    sendExerciseDataToFragmentOne() // Asegúrate de actualizar los datos con la URL de descarga
                }.addOnFailureListener {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Failed to get download URL", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Media upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun sendExerciseDataToFragmentOne() {
        val exerciseName = editTextExerciseName.text.toString()
        val series = editTextSeries.text.toString()
        val repetitions = editTextRepetitions.text.toString()
        val mediaUrl = gifUrl?.toString() ?: ""
        exerciseDataListener?.onExerciseDataReceived(exerciseName, series, repetitions, mediaUrl)
    }
}

interface ExerciseDataListener {
    fun onExerciseDataReceived(exerciseName: String, series: String, repetitions: String, gifUrl: String)
    fun onRemoveExercise(fragment: ExerciseFragment)
}
