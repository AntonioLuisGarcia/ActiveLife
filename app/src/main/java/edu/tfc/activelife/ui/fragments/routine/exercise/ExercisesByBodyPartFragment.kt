package edu.tfc.activelife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import edu.tfc.activelife.api.ExerciseRepository
import edu.tfc.activelife.api.ExerciseResponse

/**
 * Data class to hold exercise details, including exercise information, series, and repetitions.
 */
data class ExerciseDetail(
    val exercise: ExerciseResponse,
    val series: Int,
    val repetitions: Int,
)

/**
 * Fragment to display a list of exercises filtered by body part.
 * When an exercise is selected, detailed information is shown and can be added to a routine.
 *
 * @param onExerciseSelected Callback to handle the selected exercise details.
 */
class ExercisesByBodyPartFragment(private val onExerciseSelected: (ExerciseDetail) -> Unit) : DialogFragment() {

    private lateinit var exerciseRepository: ExerciseRepository
    private var bodyPart: String? = null
    private lateinit var exercisesListView: ListView
    private lateinit var exerciseDetailView: View
    private lateinit var selectedExercise: ExerciseResponse

    /**
     * Called to do initial creation of the fragment.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bodyPart = arguments?.getString(ARG_BODY_PART)
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercises_by_body_part, container, false)
        exercisesListView = view.findViewById(R.id.exercisesListView)
        exerciseDetailView = view.findViewById(R.id.exerciseDetailView)

        exerciseRepository = ExerciseRepository.getInstance()
        bodyPart?.let {
            exerciseRepository.fetchExercisesByBodyPart(it)
        }

        exerciseRepository.exercises.observe(viewLifecycleOwner, Observer { exercises ->
            val exerciseNames = exercises.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, exerciseNames)
            exercisesListView.adapter = adapter
            exercisesListView.setOnItemClickListener { _, _, position, _ ->
                selectedExercise = exercises[position]
                showExerciseDetail(selectedExercise)
            }
        })

        val addButton: Button = view.findViewById(R.id.addButton)
        addButton.setOnClickListener {
            val seriesInput: EditText = exerciseDetailView.findViewById(R.id.seriesInput)
            val repetitionsInput: EditText = exerciseDetailView.findViewById(R.id.repetitionsInput)

            val exerciseDetail = ExerciseDetail(
                exercise = selectedExercise,
                series = seriesInput.text.toString().toInt(),
                repetitions = repetitionsInput.text.toString().toInt()
            )
            onExerciseSelected(exerciseDetail)
            dismiss() // Close the current dialog
        }

        return view
    }

    /**
     * Displays detailed information of the selected exercise.
     * @param exercise The selected exercise.
     */
    private fun showExerciseDetail(exercise: ExerciseResponse) {
        exercisesListView.visibility = View.GONE
        exerciseDetailView.visibility = View.VISIBLE

        val exerciseName: TextView = exerciseDetailView.findViewById(R.id.exerciseName)
        val exerciseDescription: TextView = exerciseDetailView.findViewById(R.id.exerciseDescription)
        val exerciseImage: ImageView = exerciseDetailView.findViewById(R.id.exerciseImage)
        val seriesInput: EditText = exerciseDetailView.findViewById(R.id.seriesInput)
        val repetitionsInput: EditText = exerciseDetailView.findViewById(R.id.repetitionsInput)

        exerciseName.text = exercise.name
        exerciseDescription.text = exercise.instructions[0] ?: "No description available"
        Glide.with(this).load(exercise.gifUrl).into(exerciseImage)
    }

    companion object {
        private const val ARG_BODY_PART = "body_part"

        /**
         * Creates a new instance of ExercisesByBodyPartFragment.
         * @param bodyPart The body part to filter exercises by.
         * @param onExerciseSelected Callback to handle the selected exercise details.
         * @return A new instance of ExercisesByBodyPartFragment.
         */
        fun newInstance(bodyPart: String, onExerciseSelected: (ExerciseDetail) -> Unit): ExercisesByBodyPartFragment {
            val fragment = ExercisesByBodyPartFragment(onExerciseSelected)
            val args = Bundle()
            args.putString(ARG_BODY_PART, bodyPart)
            fragment.arguments = args
            return fragment
        }
    }
}