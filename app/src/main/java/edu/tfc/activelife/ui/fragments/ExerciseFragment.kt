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
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import edu.tfc.activelife.R
import edu.tfc.activelife.ui.fragments.ExerciseDataListener

class ExerciseFragment : Fragment() {

    companion object {
        fun newInstance(): ExerciseFragment {
            return ExerciseFragment()
        }
        private const val PICK_MEDIA_REQUEST = 1
    }

    var exerciseDataListener: ExerciseDataListener? = null
    lateinit var editTextExerciseName: EditText
    lateinit var editTextSeries: EditText
    lateinit var editTextRepetitions: EditText
    lateinit var imageViewExerciseMedia: ImageView
    lateinit var buttonAddMedia: Button
    var gifUrl: Uri? = null

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

        arguments?.let {
            editTextExerciseName.setText(it.getString("exerciseName"))
            editTextSeries.setText(it.getString("series"))
            editTextRepetitions.setText(it.getString("repetitions"))
            val mediaUrl = it.getString("gifUrl")
            if (mediaUrl != null) {
                Glide.with(this).load(mediaUrl).into(imageViewExerciseMedia)
                imageViewExerciseMedia.visibility = View.VISIBLE
            }
        }

        buttonAddMedia.setOnClickListener {
            openMediaPicker()
        }

        return view
    }

    private fun openMediaPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/* video/*"
        startActivityForResult(intent, PICK_MEDIA_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_MEDIA_REQUEST && resultCode == Activity.RESULT_OK) {
            gifUrl = data?.data
            gifUrl?.let { uri ->
                val storageRef = FirebaseStorage.getInstance().reference.child("exercise_media/${uri.lastPathSegment}")
                storageRef.putFile(uri).addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        gifUrl = downloadUrl
                        Glide.with(this).load(gifUrl).into(imageViewExerciseMedia)
                        Toast.makeText(requireContext(), "Media uploaded: $gifUrl", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to get download URL", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Media upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun sendExerciseDataToFragmentOne() {
        val exerciseName = editTextExerciseName.text.toString()
        val series = editTextSeries.text.toString()
        val repetitions = editTextRepetitions.text.toString()
        val mediaUrl = gifUrl?.toString() ?: "" // Asegúrate de enviar la URL del medio
        exerciseDataListener?.onExerciseDataReceived(exerciseName, series, repetitions,mediaUrl)
    }
}
