package edu.tfc.activelife

// FragmentOne.kt
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class FragmentOne : Fragment(), ExerciseDataListener {

    private lateinit var editTextTitle: EditText
    private lateinit var buttonSendRoutine: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var exerciseContainer: ViewGroup // Contenedor para los fragmentos de ejercicio
    private var exerciseFragmentCount = 1 // Contador para asignar IDs únicos a los fragmentos de ejercicio
    private val exerciseList = mutableListOf<HashMap<String, String>>() // Lista de ejercicios

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_one, container, false)
        db = FirebaseFirestore.getInstance()
        editTextTitle = view.findViewById(R.id.editTextTitle)
        buttonSendRoutine = view.findViewById(R.id.buttonSendRoutine)
        exerciseContainer = view.findViewById(R.id.exercise_fragment)
        val exerciseFragment = childFragmentManager.findFragmentById(R.id.exercise_fragment) as? ExerciseFragment
        exerciseFragment?.exerciseDataListener = this
        buttonSendRoutine.setOnClickListener {
            sendRoutineToFirebase()
        }

        // Obtener el TextView para añadir un nuevo ejercicio
        val addExerciseText: TextView = view.findViewById(R.id.addExerciseText)
        addExerciseText.setOnClickListener {
            addExerciseFragment()
        }
        return view
    }

    private fun addExerciseFragment() {
        // Crear un nuevo fragmento de ejercicio
        val newExerciseFragment = ExerciseFragment.newInstance()

        // Agregar el nuevo fragmento al contenedor con una etiqueta única
        val tag = "exercise_$exerciseFragmentCount"
        val transaction = childFragmentManager.beginTransaction()
        transaction.add(exerciseContainer.id, newExerciseFragment, tag)
        transaction.commit()

        exerciseFragmentCount++
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

        // Recorrer cada fragmento de ejercicio para obtener los datos
        for (i in 0 until exerciseFragmentCount) {
            val exerciseFragment: ExerciseFragment? = if (i == 0) {
                // Si es el primer fragmento, obténlo directamente
                childFragmentManager.findFragmentById(R.id.exercise_fragment) as? ExerciseFragment
            } else {
                // Si no es el primer fragmento, obténlo por tag
                childFragmentManager.findFragmentByTag("exercise_$i") as? ExerciseFragment
            }

            exerciseFragment?.let {
                val exerciseName = it.editTextExerciseName.text.toString()
                val series = it.editTextSeries.text.toString()
                val repetitions = it.editTextRepetitions.text.toString()

                // Verificar que los campos no estén vacíos
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

                // Crear un mapa con los datos del ejercicio
                val exerciseData = hashMapOf(
                    "exerciseName" to exerciseName,
                    "series" to series,
                    "repetitions" to repetitions
                )

                // Agregar el mapa a la lista de ejercicios
                exerciseList.add(exerciseData)
            } ?: run {
                Toast.makeText(requireContext(), "Error al obtener datos del ejercicio", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Crear un mapa con los datos de la rutina
        val routineData = hashMapOf(
            "title" to title,
            "exercises" to exerciseList,
            "userUuid" to userUUID
        )

        // Subir los datos a Firebase
        db.collection("rutinas")
            .add(routineData)
            .addOnSuccessListener { documentReference ->
                // Rutina enviada con éxito
                Toast.makeText(requireContext(), "Creada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Error al enviar la rutina
                Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
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