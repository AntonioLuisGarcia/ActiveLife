package edu.tfc.activelife.ui.fragments.routine.exercise

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
import com.bumptech.glide.Glide
import edu.tfc.activelife.R
import edu.tfc.activelife.utils.Utils

class ExerciseFragment : Fragment() {

    companion object {
        fun newInstance(): ExerciseFragment {
            return ExerciseFragment()
        }
    }

    var exerciseDataListener: ExerciseDataListener? = null
    lateinit var editTextExerciseName: EditText
    lateinit var editTextSeries: EditText
    lateinit var editTextRepetitions: EditText
    lateinit var imageViewExerciseMedia: ImageView
    lateinit var buttonAddMedia: Button
    lateinit var buttonRemoveExercise: Button
    var gifUri: Uri? = null
    var gifUrl: String? = null

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

        // Ocultar ImageView inicialmente
        imageViewExerciseMedia.visibility = View.GONE

        arguments?.let {
            editTextExerciseName.setText(it.getString("name"))
            editTextSeries.setText(it.getString("serie"))
            editTextRepetitions.setText(it.getString("repeticiones"))
            val mediaUrl = it.getString("gifUrl")
            if (mediaUrl != null) {
                gifUri = Uri.parse(mediaUrl)
                if (isAdded) {
                    Glide.with(this).load(mediaUrl).into(imageViewExerciseMedia)
                }
                imageViewExerciseMedia.visibility = View.VISIBLE // Mostrar el ImageView si hay imagen
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
        Utils.showImagePickerDialog(this, requireContext(), "Imagen de Ejercicio") { bitmap, uri ->
            gifUri = uri
            gifUrl = uri?.toString()
            Utils.loadImageIntoView(imageViewExerciseMedia, bitmap, uri)
            imageViewExerciseMedia.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Utils.handleActivityResult(requestCode, resultCode, data) { bitmap, uri ->
            gifUri = uri
            gifUrl = uri?.toString()
            Utils.loadImageIntoView(imageViewExerciseMedia, bitmap, uri)
            imageViewExerciseMedia.visibility = View.VISIBLE
        }
    }

    fun sendExerciseDataToFragmentOne() {
        val exerciseName = editTextExerciseName.text.toString()
        val series = editTextSeries.text.toString()
        val repetitions = editTextRepetitions.text.toString()
        val mediaUri = gifUri?.toString() ?: ""
        exerciseDataListener?.onExerciseDataReceived(exerciseName, series, repetitions, mediaUri)
    }
}

interface ExerciseDataListener {
    fun onExerciseDataReceived(exerciseName: String, series: String, repetitions: String, mediaUri: String)
    fun onRemoveExercise(fragment: ExerciseFragment)
}