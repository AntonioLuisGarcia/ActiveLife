package edu.tfc.activelife.ui.fragments.routine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.tfc.activelife.adapters.ExerciseAdapter
import edu.tfc.activelife.R

/**
 * RoutineFragment is responsible for displaying the details of a specific routine,
 * including the title and the list of exercises. The exercises are displayed in a RecyclerView.
 */
class RoutineFragment : Fragment() {

    private lateinit var titleTextView: TextView
    private lateinit var exercisesRecyclerView: RecyclerView
    private lateinit var exercisesAdapter: ExerciseAdapter

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional and can be null for non-graphical fragments.
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
        val view = inflater.inflate(R.layout.fragment_routine, container, false)

        // Initialize views
        titleTextView = view.findViewById(R.id.textViewRoutineTitle)
        exercisesRecyclerView = view.findViewById(R.id.recyclerViewExercises)

        // Configure RecyclerView for the list of exercises
        exercisesAdapter = ExerciseAdapter(emptyList())
        exercisesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        exercisesRecyclerView.adapter = exercisesAdapter

        return view
    }
}