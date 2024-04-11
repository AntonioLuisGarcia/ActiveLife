package edu.tfc.activelife.routine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.tfc.activelife.ExerciseAdapter
import edu.tfc.activelife.R

class RoutineFragment : Fragment() {

    private lateinit var titleTextView: TextView
    private lateinit var exercisesRecyclerView: RecyclerView
    private lateinit var exercisesAdapter: ExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_routine, container, false)

        // Inicializar vistas
        titleTextView = view.findViewById(R.id.textViewRoutineTitle)
        exercisesRecyclerView = view.findViewById(R.id.recyclerViewExercises)

        // Configurar RecyclerView para la lista de ejercicios
        exercisesAdapter = ExerciseAdapter(emptyList())
        exercisesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        exercisesRecyclerView.adapter = exercisesAdapter

        return view
    }
}

