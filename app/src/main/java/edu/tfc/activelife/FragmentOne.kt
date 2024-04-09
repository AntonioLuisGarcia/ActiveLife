package edu.tfc.activelife

// FragmentOne.kt
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import com.google.firebase.firestore.FirebaseFirestore


class FragmentOne : Fragment(), ExerciseDataListener {

    private lateinit var editTextTitle: EditText
    private lateinit var buttonSendRoutine: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_one, container, false)
        db = FirebaseFirestore.getInstance()
        editTextTitle = view.findViewById(R.id.editTextTitle)
        buttonSendRoutine = view.findViewById(R.id.buttonSendRoutine)
        val exerciseFragment = childFragmentManager.findFragmentById(R.id.exercise_fragment) as? ExerciseFragment
        exerciseFragment?.exerciseDataListener = this
        buttonSendRoutine.setOnClickListener {
            sendRoutineToFirebase()
        }
        return view
    }

    private fun sendRoutineToFirebase() {
        val title = editTextTitle.text.toString()
        val exerciseFragment = childFragmentManager.findFragmentById(R.id.exercise_fragment) as? ExerciseFragment

        // Verificar que el título no esté vacío
        if (title.isBlank()) {
            Toast.makeText(requireContext(), "Por favor, ingresa un título válido", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar que el fragmento de ejercicio no sea nulo y que los campos no estén vacíos
        exerciseFragment?.let {
            val exerciseName = it.editTextExerciseName.text.toString()
            val series = it.editTextSeries.text.toString()
            val repetitions = it.editTextRepetitions.text.toString()

            // Verificar que el nombre del ejercicio, series y repeticiones no estén vacíos
            if (exerciseName.isBlank() || series.isBlank() || repetitions.isBlank()) {
                Toast.makeText(requireContext(), "Por favor, ingresa valores válidos para el ejercicio", Toast.LENGTH_SHORT).show()
                return
            }

            // Verificar que series y repeticiones sean números enteros y positivos
            if (!series.isDigitsOnly() || !repetitions.isDigitsOnly()) {
                Toast.makeText(requireContext(), "Por favor, ingresa valores numéricos válidos para series y repeticiones", Toast.LENGTH_SHORT).show()
                return
            }

            // Convertir series y repeticiones a enteros
            val seriesInt = series.toInt()
            val repetitionsInt = repetitions.toInt()

            // Verificar que series y repeticiones sean positivos
            if (seriesInt <= 0 || repetitionsInt <= 0) {
                Toast.makeText(requireContext(), "Por favor, ingresa valores positivos para series y repeticiones", Toast.LENGTH_SHORT).show()
                return
            }

            // Si pasa todas las validaciones, enviar los datos a FragmentOne
            exerciseFragment.sendExerciseDataToFragmentOne()
        } ?: run {
            Toast.makeText(requireContext(), "Error al obtener datos del ejercicio", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onExerciseDataReceived(exerciseName: String, series: String, repetitions: String) {
        val routineData = hashMapOf(
            "title" to editTextTitle.text.toString(),
            "exerciseName" to exerciseName,
            "series" to series,
            "repetitions" to repetitions
        )
        db.collection("rutinas")
            .add(routineData)
            .addOnSuccessListener { documentReference ->
                // Rutina enviable con éxito
                Toast.makeText(requireContext(), "Creada", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener { e ->
                // Error al enviar la rutina
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()

            }
    }
}

interface ExerciseDataListener {
    fun onExerciseDataReceived(exerciseName: String, series: String, repetitions: String)
}