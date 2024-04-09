package edu.tfc.activelife
// ExerciseFragment.kt
// ExerciseFragment.kt
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import edu.tfc.activelife.ExerciseDataListener
import edu.tfc.activelife.R

class ExerciseFragment : Fragment() {

    var exerciseDataListener: ExerciseDataListener? = null
    lateinit var editTextExerciseName: EditText
    lateinit var editTextSeries: EditText
    lateinit var editTextRepetitions: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercise, container, false)
        editTextExerciseName = view.findViewById(R.id.editTextExerciseName)
        editTextSeries = view.findViewById(R.id.editTextSeries)
        editTextRepetitions = view.findViewById(R.id.editTextRepetitions)
        return view
    }

    fun sendExerciseDataToFragmentOne() {
        val exerciseName = editTextExerciseName.text.toString()
        val series = editTextSeries.text.toString()
        val repetitions = editTextRepetitions.text.toString()
        exerciseDataListener?.onExerciseDataReceived(exerciseName, series, repetitions)
    }
}
