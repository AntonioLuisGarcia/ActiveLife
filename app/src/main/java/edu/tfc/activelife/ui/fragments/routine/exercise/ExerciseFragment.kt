package edu.tfc.activelife.ui.fragments.routine.exercise

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import com.bumptech.glide.Glide
import edu.tfc.activelife.R
import edu.tfc.activelife.utils.Utils
import java.io.File
import java.io.FileOutputStream

/**
 * ExerciseFragment allows users to add or edit exercise details including name, series, repetitions, and media.
 * Users can add images or GIFs to the exercise, which can be removed or updated as needed.
 * The fragment interacts with the parent activity to send the updated exercise data.
 */
class ExerciseFragment : Fragment() {

    companion object {
        /**
         * Creates a new instance of ExerciseFragment.
         */
        fun newInstance(): ExerciseFragment {
            return ExerciseFragment()
        }
    }

    // Listener for exercise data
    var exerciseDataListener: ExerciseDataListener? = null

    // UI elements
    lateinit var editTextExerciseName: EditText
    lateinit var editTextSeries: EditText
    lateinit var editTextRepetitions: EditText
    lateinit var imageViewExerciseMedia: ImageView
    lateinit var buttonAddMedia: Button
    lateinit var buttonRemoveMedia: Button
    lateinit var buttonRemoveExercise: Button
    lateinit var spinnerBodyPart: Spinner

    // Variables for media data
    var gifUri: Uri? = null
    var gifUrl: String? = null

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercise, container, false)
        try {
            // Initialize UI elements
            editTextExerciseName = view.findViewById(R.id.editTextExerciseName)
            editTextSeries = view.findViewById(R.id.editTextSeries)
            editTextRepetitions = view.findViewById(R.id.editTextRepetitions)
            imageViewExerciseMedia = view.findViewById(R.id.imageViewExerciseMedia)
            buttonAddMedia = view.findViewById(R.id.buttonAddMedia)
            buttonRemoveMedia = view.findViewById(R.id.buttonRemoveMedia)
            buttonRemoveExercise = view.findViewById(R.id.buttonRemoveExercise)
            spinnerBodyPart = view.findViewById(R.id.spinnerBodyPart)

            // Initially hide the ImageView and remove media button
            imageViewExerciseMedia.visibility = View.GONE
            buttonRemoveMedia.visibility = View.GONE

            // Load arguments if available
            arguments?.let {
                editTextExerciseName.setText(it.getString("name"))
                editTextSeries.setText(it.getString("serie"))
                editTextRepetitions.setText(it.getString("repeticiones"))
                val mediaUrl = it.getString("gifUrl")
                if (!mediaUrl.isNullOrEmpty()) {
                    gifUri = Uri.parse(mediaUrl)
                    if (isAdded) {
                        Glide.with(this).load(mediaUrl).into(imageViewExerciseMedia)
                    }
                    imageViewExerciseMedia.visibility = View.VISIBLE // Show ImageView if there is an image
                    //buttonRemoveMedia.visibility = View.VISIBLE // Show remove media button if there is an image
                } else {
                    imageViewExerciseMedia.visibility = View.GONE // Hide ImageView if there is no image
                    buttonRemoveMedia.visibility = View.GONE // Hide remove media button if there is no image
                }
                val bodyPart = it.getString("bodyPart")
                bodyPart?.let {
                    val bodyPartsArray = resources.getStringArray(R.array.body_parts)
                    val position = bodyPartsArray.indexOf(it)
                    if (position >= 0) {
                        spinnerBodyPart.setSelection(position)
                    }
                }
            }

            // Set up button listeners
            buttonAddMedia.setOnClickListener {
                showMediaPickerDialog()
            }

            buttonRemoveMedia.setOnClickListener {
                removeMedia()
            }

            buttonRemoveExercise.setOnClickListener {
                exerciseDataListener?.onRemoveExercise(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error initializing fragment: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    /**
     * Shows a dialog for picking an image or GIF for the exercise.
     */
    private fun showMediaPickerDialog() {
        context?.let {
            Utils.showImagePickerDialog(this, requireContext(), it.getString(R.string.selected_image), gifUri != null) { bitmap, uri ->
                if (bitmap == null && uri == null) {
                    gifUri = null
                    gifUrl = null
                    imageViewExerciseMedia.setImageBitmap(null)
                    imageViewExerciseMedia.visibility = View.GONE
                    buttonRemoveMedia.visibility = View.GONE
                } else {
                    gifUri = uri
                    gifUrl = uri?.toString()
                    Utils.loadImageIntoView(imageViewExerciseMedia, bitmap, uri)
                    imageViewExerciseMedia.visibility = View.VISIBLE
                    //buttonRemoveMedia.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * Removes the currently selected media from the exercise.
     */
    private fun removeMedia() {
        gifUri = null
        gifUrl = null
        imageViewExerciseMedia.setImageDrawable(null)
        imageViewExerciseMedia.visibility = View.GONE
        buttonRemoveMedia.visibility = View.GONE
    }

    /**
     * Handles the result from the media picker dialog.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Utils.handleActivityResult(requestCode, resultCode, data) { bitmap, uri ->
            if (bitmap != null) {
                gifUri = saveBitmapToFile(requireContext(), bitmap)
                gifUrl = gifUri?.toString()
            } else {
                gifUri = uri
                gifUrl = uri?.toString()
            }
            Utils.loadImageIntoView(imageViewExerciseMedia, bitmap, gifUri)
            imageViewExerciseMedia.visibility = View.VISIBLE
            //buttonRemoveMedia.visibility = View.VISIBLE // Show remove media button
        }
    }

    /**
     * Saves a Bitmap to a file in the cache directory and returns the Uri of the saved file.
     *
     * @param context The context used to access the cache directory.
     * @param bitmap The Bitmap to save.
     * @return The Uri of the saved file.
     */
    fun saveBitmapToFile(context: Context, bitmap: Bitmap): Uri? {
        val imagesFolder = File(context.cacheDir, "images")
        imagesFolder.mkdirs()
        val file = File(imagesFolder, "${System.currentTimeMillis()}.jpg")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.flush()
        stream.close()
        return Uri.fromFile(file)
    }

    /**
     * Sends the exercise data to the parent activity or fragment.
     */
    fun sendExerciseDataToFragmentOne() {
        val exerciseName = editTextExerciseName.text.toString()
        val series = editTextSeries.text.toString()
        val repetitions = editTextRepetitions.text.toString()
        val mediaUri = gifUri?.toString() ?: ""
        val bodyPart = spinnerBodyPart.selectedItem.toString()
        exerciseDataListener?.onExerciseDataReceived(exerciseName, series, repetitions, mediaUri, bodyPart)
    }
}

/**
 * Interface for handling exercise data interactions.
 */
interface ExerciseDataListener {
    fun onExerciseDataReceived(exerciseName: String, series: String, repetitions: String, mediaUri: String, bodyPart: String)
    fun onRemoveExercise(fragment: ExerciseFragment)
}
