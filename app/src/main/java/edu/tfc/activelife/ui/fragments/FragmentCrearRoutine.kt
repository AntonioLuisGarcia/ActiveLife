package edu.tfc.activelife.ui.fragments

import ExerciseFragment
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
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
    private lateinit var daySpinner: Spinner
    private lateinit var db: FirebaseFirestore
    private lateinit var exerciseContainer: ViewGroup
    private var exerciseFragmentCount = 0
    private var exerciseList = mutableListOf<HashMap<String, Any>>()
    private var isActive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        arguments?.let {
            isActive = it.getBoolean("active", false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crear_routine, container, false)

        editTextTitle = view.findViewById(R.id.editTextTitle)
        buttonSendRoutine = view.findViewById(R.id.buttonSendRoutine)
        daySpinner = view.findViewById(R.id.daySpinner)
        exerciseContainer = view.findViewById(R.id.exercise_fragment_container)

        val daysOfWeek = resources.getStringArray(R.array.days_of_week)
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, daysOfWeek)
        daySpinner.adapter = spinnerAdapter

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
        val day = routineData["day"] as? String
        day?.let {
            val position = resources.getStringArray(R.array.days_of_week).indexOf(it)
            daySpinner.setSelection(position)
        }
        routineData["exercises"]?.let {
            (it as List<HashMap<String, Any>>).forEach { exerciseData ->
                addExerciseFragment(exerciseData) // Cargar ejercicios existentes en la edición de una rutina
            }
        }
        // Asignar el valor de active
        isActive = routineData["active"] as? Boolean ?: false
    }

    private fun sendRoutineToFirebase() {
        val title = editTextTitle.text.toString()
        val selectedDay = daySpinner.selectedItem.toString()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userUUID = currentUser?.uid

        if (userUUID == null) {
            Toast.makeText(requireContext(), "No se ha podido obtener el usuario actual", Toast.LENGTH_SHORT).show()
            return
        }

        if (title.isBlank()) {
            Toast.makeText(requireContext(), "Por favor, ingresa un título válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (exerciseFragmentCount == 0) {
            Toast.makeText(requireContext(), "Por favor, añade al menos un ejercicio", Toast.LENGTH_SHORT).show()
            return
        }

        exerciseList.clear()

        for (i in 0 until exerciseFragmentCount) {
            val exerciseFragment = childFragmentManager.findFragmentByTag("exercise_$i") as? ExerciseFragment
            exerciseFragment?.let {
                val exerciseName = it.editTextExerciseName.text.toString()
                val series = it.editTextSeries.text.toString()
                val repetitions = it.editTextRepetitions.text.toString()
                val gifUrl = it.gifUrl.toString()

                if (exerciseName.isBlank() || series.isBlank() || repetitions.isBlank()) {
                    Toast.makeText(requireContext(), "Por favor, ingresa valores válidos para el ejercicio", Toast.LENGTH_SHORT).show()
                    return
                }

                if (!series.isDigitsOnly() || !repetitions.isDigitsOnly() || series.toInt() <= 0 || repetitions.toInt() <= 0) {
                    Toast.makeText(requireContext(), "Por favor, ingresa valores numéricos y positivos para series y repeticiones", Toast.LENGTH_SHORT).show()
                    return
                }

                val exerciseData: HashMap<String, Any> = hashMapOf(
                    "name" to exerciseName,
                    "serie" to series,
                    "repeticiones" to repetitions,
                    "gifUrl" to gifUrl
                )
                exerciseList.add(exerciseData)
            } ?: run {
                Toast.makeText(requireContext(), "Error al obtener datos del ejercicio", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val routineData = hashMapOf(
            "title" to title,
            "day" to selectedDay,
            "exercises" to exerciseList,
            "userUuid" to userUUID,
            "active" to isActive
        )

        val routineId = arguments?.getString("routineId")
        if (routineId.isNullOrEmpty()) {
            db.collection("rutinas").add(routineData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Rutina creada exitosamente", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al crear rutina: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
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

    private fun addExerciseFragment(exerciseData: HashMap<String, Any>? = null) {
        val newExerciseFragment = ExerciseFragment.newInstance()
        val args = Bundle()
        if (exerciseData != null) {
            args.putString("name", exerciseData["name"] as? String)
            args.putString("serie", exerciseData["serie"] as? String)
            args.putString("repeticiones", exerciseData["repeticiones"] as? String)
            args.putString("gifUrl", exerciseData["gifUrl"] as? String)
            newExerciseFragment.arguments = args
        }
        newExerciseFragment.arguments = args
        childFragmentManager.beginTransaction()
            .add(R.id.exercise_fragment_container, newExerciseFragment, "exercise_$exerciseFragmentCount")
            .commit()
        exerciseFragmentCount++
    }
}

interface ExerciseDataListener {
    fun onExerciseDataReceived(exerciseName: String, series: String, repetitions: String, gifUrl: String)
}
