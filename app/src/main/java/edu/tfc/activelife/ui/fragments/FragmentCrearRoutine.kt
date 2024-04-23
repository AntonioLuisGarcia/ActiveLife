package edu.tfc.activelife.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.tfc.activelife.R

class FragmentOne : Fragment() {

    private lateinit var editTextTitle: EditText
    private lateinit var buttonSendRoutine: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var exerciseContainer: ViewGroup
    private var exerciseFragmentCount = 0
    private var exerciseList = mutableListOf<HashMap<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance() // Asegúrate que db esté inicializado aquí.
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crear_routine, container, false)

        editTextTitle = view.findViewById(R.id.editTextTitle)
        buttonSendRoutine = view.findViewById(R.id.buttonSendRoutine)
        exerciseContainer = view.findViewById(R.id.exercise_fragment_container)

        buttonSendRoutine.setOnClickListener {
            sendRoutineToFirebase()
        }

        val addExerciseText: TextView = view.findViewById(R.id.addExerciseText)
        addExerciseText.setOnClickListener {
            addExerciseFragment() // Agregar un nuevo ExerciseFragment solo en clic
        }

        val routineId = arguments?.getString("routineId")
        if (!routineId.isNullOrEmpty()) {
            loadRoutineData(routineId)
        }

        return view
    }

    private fun loadRoutineData(routineId: String) {
        db.collection("rutinas").document(routineId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    fillFragmentWithData(document.data as Map<String, Any>)
                } else {
                    Toast.makeText(requireContext(), "No se encontró la rutina", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error al buscar la rutina: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fillFragmentWithData(routineData: Map<String, Any>) {
        editTextTitle.setText(routineData["title"] as? String)
        routineData["exercises"]?.let {
            (it as List<HashMap<String, String>>).forEach { exerciseData ->
                addExerciseFragment(exerciseData) // Cargar ejercicios existentes en la edición de una rutina
            }
        }
    }

    private fun sendRoutineToFirebase() {
        val title = editTextTitle.text.toString()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userUUID = currentUser?.uid

        // Verificar que el título no esté vacío
        if (title.isBlank()) {
            Toast.makeText(requireContext(), "Por favor, ingresa un título válido", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar que haya al menos un fragmento de ejercicio
        if (exerciseFragmentCount == 0) {
            Toast.makeText(requireContext(), "Por favor, añade al menos un ejercicio", Toast.LENGTH_SHORT).show()
            return
        }

        // Limpiar la lista de ejercicios
        exerciseList.clear()

        // Recoger datos de cada fragmento de ejercicio
        for (i in 0 until exerciseFragmentCount) {
            val exerciseFragment = childFragmentManager.findFragmentByTag("exercise_$i") as? ExerciseFragment
            exerciseFragment?.let {
                val exerciseName = it.editTextExerciseName.text.toString()
                val series = it.editTextSeries.text.toString()
                val repetitions = it.editTextRepetitions.text.toString()

                // Validar campos del ejercicio
                if (exerciseName.isBlank() || series.isBlank() || repetitions.isBlank()) {
                    Toast.makeText(requireContext(), "Por favor, ingresa valores válidos para el ejercicio", Toast.LENGTH_SHORT).show()
                    return
                }

                // Verificar que series y repeticiones sean números enteros y positivos
                if (!series.isDigitsOnly() || !repetitions.isDigitsOnly() || series.toInt() <= 0 || repetitions.toInt() <= 0) {
                    Toast.makeText(requireContext(), "Por favor, ingresa valores numéricos y positivos para series y repeticiones", Toast.LENGTH_SHORT).show()
                    return
                }

                // Agregar datos validados a la lista de ejercicios
                val exerciseData: HashMap<String, Any> = hashMapOf(
                    "exerciseName" to exerciseName,
                    "series" to series,
                    "repetitions" to repetitions
                )
                exerciseList.add(exerciseData)
            } ?: run {
                Toast.makeText(requireContext(), "Error al obtener datos del ejercicio", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Crear mapa con los datos de la rutina
        val routineData = hashMapOf(
            "title" to title,
            "exercises" to exerciseList,
            "userUuid" to userUUID
        )

        val routineId = arguments?.getString("routineId")
        if (routineId.isNullOrEmpty()) {
            // Crear nueva rutina
            db.collection("rutinas").add(routineData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Rutina creada exitosamente", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al crear rutina: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Actualizar rutina existente
            db.collection("rutinas").document(routineId).set(routineData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Rutina actualizada exitosamente", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al actualizar rutina: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun addExerciseFragment(exerciseData: HashMap<String, String>? = null) {
        val newExerciseFragment = ExerciseFragment.newInstance()
        val args = Bundle()
        if (exerciseData != null) {
            args.putString("exerciseName", exerciseData["exerciseName"])
            args.putString("series", exerciseData["series"])
            args.putString("repetitions", exerciseData["repetitions"])
            newExerciseFragment.arguments = args
        }
        childFragmentManager.beginTransaction()
            .add(R.id.exercise_fragment_container, newExerciseFragment, "exercise_$exerciseFragmentCount")
            .commit()
        exerciseFragmentCount++
    }

}

interface ExerciseDataListener {
    fun onExerciseDataReceived(exerciseName: String, series: String, repetitions: String)
}
