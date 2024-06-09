package edu.tfc.activelife.ui.fragments.routine.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import edu.tfc.activelife.ExerciseDetail
import edu.tfc.activelife.ExercisesByBodyPartFragment
import edu.tfc.activelife.R
import edu.tfc.activelife.api.ExerciseRepository

/**
 * AddExerciseDialogFragment is a dialog fragment that allows users to add an exercise to their routine.
 * Users can select a body part from a list, and then see a list of exercises targeting that body part.
 * When an exercise is selected, it is passed back to the parent fragment or activity via a callback.
 *
 * @param onExerciseSelected A callback function to handle the selected exercise.
 */
class AddExerciseDialogFragment(private val onExerciseSelected: (ExerciseDetail) -> Unit) : DialogFragment() {

    private lateinit var exerciseRepository: ExerciseRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_exercise_dialog, container, false)

        val bodyPartsListView: ListView = view.findViewById(R.id.exercisesListView)

        exerciseRepository = ExerciseRepository.getInstance()
        exerciseRepository.fetchBodyParts()

        // Observe the body parts LiveData and update the ListView when the data changes
        exerciseRepository.bodyParts.observe(viewLifecycleOwner, Observer { bodyParts ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, bodyParts)
            bodyPartsListView.adapter = adapter
        })

        // Set up item click listener for the ListView
        bodyPartsListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedBodyPart = bodyPartsListView.getItemAtPosition(position) as String
            val exercisesFragment =
                ExercisesByBodyPartFragment.newInstance(selectedBodyPart, onExerciseSelected)
            exercisesFragment.show(parentFragmentManager, "ExercisesByBodyPartFragment")
            dismiss() // Close the current dialog
        }

        return view
    }
}
