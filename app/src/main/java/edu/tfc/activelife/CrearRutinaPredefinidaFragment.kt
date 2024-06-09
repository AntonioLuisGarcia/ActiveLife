package edu.tfc.activelife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.tfc.activelife.ui.fragments.routine.exercise.AddExerciseDialogFragment

/**
 * Fragment for creating a predefined routine with a list of exercises.
 * Users can add exercises, specify routine details, and save the routine to Firestore.
 */
class CrearRutinaPredefinidaFragment : Fragment() {

    private val selectedExercises = mutableListOf<ExerciseDetail>()
    private lateinit var exercisesListView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var routineTitleInput: EditText
    private lateinit var daySpinner: Spinner
    private lateinit var auth: FirebaseAuth

    /**
     * Inflates the layout for this fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_crear_rutina_predefinida, container, false)

        exercisesListView = view.findViewById(R.id.exercisesListView)
        routineTitleInput = view.findViewById(R.id.routineTitleInput)
        daySpinner = view.findViewById(R.id.daySpinner)
        auth = FirebaseAuth.getInstance()

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, selectedExercises.map { it.exercise.name })
        exercisesListView.adapter = adapter

        val daysOfWeek = resources.getStringArray(R.array.days_of_week)
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, daysOfWeek)
        daySpinner.adapter = spinnerAdapter

        val createRoutineButton: Button = view.findViewById(R.id.createRoutineButton)
        createRoutineButton.setOnClickListener {
            val addExerciseDialog = AddExerciseDialogFragment { exerciseDetail ->
                selectedExercises.add(exerciseDetail)
                adapter.add(exerciseDetail.exercise.name)
                adapter.notifyDataSetChanged()
            }
            addExerciseDialog.show(parentFragmentManager, "AddExerciseDialogFragment")
        }

        val finalizeRoutineButton: Button = view.findViewById(R.id.finalizeRoutineButton)
        finalizeRoutineButton.setOnClickListener {
            createRoutine()
        }

        return view
    }

    /**
     * Creates a new routine with the entered details and exercises, then saves it to Firestore.
     * Displays a success or error message based on the operation result.
     */
    private fun createRoutine() {
        val routineTitle = routineTitleInput.text.toString()
        val selectedDay = daySpinner.selectedItem.toString()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(context, "No se ha podido obtener el usuario actual", Toast.LENGTH_SHORT).show()
            return
        }

        if (routineTitle.isEmpty()) {
            Toast.makeText(context, "Por favor, introduce un título para la rutina", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedExercises.isEmpty()) {
            Toast.makeText(context, "No has añadido ningún ejercicio", Toast.LENGTH_SHORT).show()
            return
        }

        val routine = hashMapOf(
            "title" to routineTitle,
            "description" to "Para hacer cositas",
            "day" to selectedDay,
            "exercises" to selectedExercises.map { exerciseDetail ->
                mapOf(
                    "bodyPart" to exerciseDetail.exercise.bodyPart,
                    "equipment" to exerciseDetail.exercise.equipment,
                    "gifUrl" to exerciseDetail.exercise.gifUrl,
                    "id" to exerciseDetail.exercise.id,
                    "instructions" to exerciseDetail.exercise.instructions,
                    "name" to exerciseDetail.exercise.name,
                    "repeticiones" to exerciseDetail.repetitions.toString(),
                    "secondaryMuscles" to exerciseDetail.exercise.secondaryMuscles,
                    "serie" to exerciseDetail.series.toString(),
                    "target" to exerciseDetail.exercise.target,
                    "activo" to false
                )
            },
            "public" to false,
            "userUuid" to currentUser.uid
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("rutinas")
            .add(routine)
            .addOnSuccessListener {
                Toast.makeText(context, "Rutina creada exitosamente", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_crearRutinaPredefinidaFragment_to_fragmentTwo) // Navigate to FragmentTwo
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al crear rutina: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}