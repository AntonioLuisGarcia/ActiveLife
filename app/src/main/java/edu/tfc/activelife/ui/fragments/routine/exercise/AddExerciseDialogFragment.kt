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

class AddExerciseDialogFragment(private val onExerciseSelected: (ExerciseDetail) -> Unit) : DialogFragment() {

    private lateinit var exerciseRepository: ExerciseRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
        val view = inflater.inflate(R.layout.fragment_add_exercise_dialog, container, false)

        val bodyPartsListView: ListView = view.findViewById(R.id.exercisesListView)

        exerciseRepository = ExerciseRepository.getInstance()
        exerciseRepository.fetchBodyParts()

        exerciseRepository.bodyParts.observe(viewLifecycleOwner, Observer { bodyParts ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, bodyParts)
            bodyPartsListView.adapter = adapter
        })

        bodyPartsListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedBodyPart = bodyPartsListView.getItemAtPosition(position) as String
            val exercisesFragment =
                ExercisesByBodyPartFragment.newInstance(selectedBodyPart, onExerciseSelected)
            exercisesFragment.show(parentFragmentManager, "ExercisesByBodyPartFragment")
            dismiss() // Cierra el diálogo actual
        }

        return view
    }
}
