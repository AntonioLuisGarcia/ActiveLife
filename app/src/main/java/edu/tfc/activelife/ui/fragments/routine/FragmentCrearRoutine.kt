package edu.tfc.activelife.ui.fragments.routine

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
import com.google.firebase.storage.FirebaseStorage
import edu.tfc.activelife.ui.fragments.routine.exercise.AddExerciseDialogFragment
import edu.tfc.activelife.R
import edu.tfc.activelife.ui.fragments.routine.exercise.ExerciseDataListener
import edu.tfc.activelife.ui.fragments.routine.exercise.ExerciseFragment

class FragmentOne : Fragment(), ExerciseDataListener {

    private lateinit var editTextTitle: EditText
    private lateinit var buttonSendRoutine: Button
    private lateinit var buttonAddPredefinedExercise: Button
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
            isActive = it.getBoolean("activo", false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crear_routine, container, false)

        editTextTitle = view.findViewById(R.id.editTextTitle)
        buttonSendRoutine = view.findViewById(R.id.buttonSendRoutine)
        buttonAddPredefinedExercise = view.findViewById(R.id.button_add_predefined_exercise)
        daySpinner = view.findViewById(R.id.daySpinner)
        exerciseContainer = view.findViewById(R.id.exercise_fragment_container)

        val daysOfWeek = resources.getStringArray(R.array.days_of_week)
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, daysOfWeek)
        daySpinner.adapter = spinnerAdapter

        buttonSendRoutine.setOnClickListener {
            uploadMediaAndSendRoutine()
        }

        val addExerciseText: TextView = view.findViewById(R.id.addExerciseText)
        addExerciseText.setOnClickListener {
            addExerciseFragment()
        }

        buttonAddPredefinedExercise.setOnClickListener {
            showAddPredefinedExerciseDialog()
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
            (it as List<HashMap<String, Any>>).forEachIndexed { index, exerciseData ->
                addExerciseFragment(exerciseData, index)
            }
        }
        isActive = routineData["activo"] as? Boolean ?: false
    }

    private fun uploadMediaAndSendRoutine() {
        buttonSendRoutine.isEnabled = false
        exerciseList.clear()
        uploadNextMedia(0)
    }

    private fun uploadNextMedia(index: Int) {
        if (index >= exerciseContainer.childCount) {
            sendRoutineToFirebase()
            return
        }

        val exerciseFragment = childFragmentManager.findFragmentByTag("exercise_$index") as? ExerciseFragment
        exerciseFragment?.let {
            val exerciseName = it.editTextExerciseName.text.toString()
            val series = it.editTextSeries.text.toString()
            val repetitions = it.editTextRepetitions.text.toString()
            val mediaUri = it.gifUri
            val mediaUrl = it.gifUrl

            if (exerciseName.isBlank() || series.isBlank() || repetitions.isBlank()) {
                Toast.makeText(requireContext(), "Por favor, ingresa valores válidos para el ejercicio", Toast.LENGTH_SHORT).show()
                buttonSendRoutine.isEnabled = true
                return
            }

            if (!series.isDigitsOnly() || !repetitions.isDigitsOnly() || series.toInt() <= 0 || repetitions.toInt() <= 0) {
                Toast.makeText(requireContext(), "Por favor, ingresa valores numéricos y positivos para series y repeticiones", Toast.LENGTH_SHORT).show()
                buttonSendRoutine.isEnabled = true
                return
            }

            val exerciseData: HashMap<String, Any> = hashMapOf(
                "name" to exerciseName,
                "serie" to series,
                "repeticiones" to repetitions
            )

            if (mediaUri != null && (mediaUri.toString().startsWith("content://") || mediaUri.toString().startsWith("file://"))) {
                val storageRef = FirebaseStorage.getInstance().reference.child("exercise_media/${mediaUri.lastPathSegment}")
                storageRef.putFile(mediaUri).addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        exerciseData["gifUrl"] = downloadUrl.toString()
                        exerciseList.add(exerciseData)
                        uploadNextMedia(index + 1)
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al obtener la URL de descarga", Toast.LENGTH_SHORT).show()
                        buttonSendRoutine.isEnabled = true
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al subir el archivo", Toast.LENGTH_SHORT).show()
                    buttonSendRoutine.isEnabled = true
                }
            } else if (mediaUri != null && mediaUri.toString().startsWith("http")) {
                exerciseData["gifUrl"] = mediaUri
                exerciseList.add(exerciseData)
                uploadNextMedia(index + 1)
            } else {
                exerciseList.add(exerciseData)
                uploadNextMedia(index + 1)
            }
        }
    }

    private fun sendRoutineToFirebase() {
        val title = editTextTitle.text.toString()
        val selectedDay = daySpinner.selectedItem.toString()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userUUID = currentUser?.uid

        if (userUUID == null) {
            Toast.makeText(requireContext(), "No se ha podido obtener el usuario actual", Toast.LENGTH_SHORT).show()
            buttonSendRoutine.isEnabled = true
            return
        }

        if (title.isBlank()) {
            Toast.makeText(requireContext(), "Por favor, ingresa un título válido", Toast.LENGTH_SHORT).show()
            buttonSendRoutine.isEnabled = true
            return
        }

        if (exerciseContainer.childCount == 0) {
            Toast.makeText(requireContext(), "Por favor, añade al menos un ejercicio", Toast.LENGTH_SHORT).show()
            buttonSendRoutine.isEnabled = true
            return
        }

        val routineData = hashMapOf(
            "title" to title,
            "day" to selectedDay,
            "exercises" to exerciseList,
            "userUuid" to userUUID,
            "activo" to isActive
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
                    buttonSendRoutine.isEnabled = true
                }
        } else {
            db.collection("rutinas").document(routineId).set(routineData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Rutina actualizada exitosamente", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al actualizar rutina: ${e.message}", Toast.LENGTH_SHORT).show()
                    buttonSendRoutine.isEnabled = true
                }
        }
    }

    private fun addExerciseFragment(exerciseData: HashMap<String, Any>? = null, index: Int = exerciseFragmentCount) {
        val newExerciseFragment = ExerciseFragment.newInstance()
        newExerciseFragment.exerciseDataListener = this
        val args = Bundle()
        if (exerciseData != null) {
            args.putString("name", exerciseData["name"] as? String)
            args.putString("serie", exerciseData["serie"] as? String)
            args.putString("repeticiones", exerciseData["repeticiones"] as? String)
            args.putString("gifUrl", exerciseData["gifUrl"] as? String)
            newExerciseFragment.arguments = args
        }
        childFragmentManager.beginTransaction()
            .add(R.id.exercise_fragment_container, newExerciseFragment, "exercise_$index")
            .commit()
        exerciseFragmentCount++
    }

    private fun showAddPredefinedExerciseDialog() {
        val dialog = AddExerciseDialogFragment { selectedExercise ->
            val exerciseData: HashMap<String, Any> = hashMapOf(
                "name" to selectedExercise.exercise.name,
                "serie" to selectedExercise.series,
                "repeticiones" to selectedExercise.repetitions,
                "gifUrl" to selectedExercise.exercise.gifUrl
            )
            addExerciseFragment(exerciseData)
        }
        dialog.show(parentFragmentManager, "AddExerciseDialogFragment")
    }

    override fun onExerciseDataReceived(exerciseName: String, series: String, repetitions: String, gifUrl: String) {
        // Aquí puedes manejar la recepción de los datos del ejercicio, si es necesario.
    }

    override fun onRemoveExercise(fragment: ExerciseFragment) {
        childFragmentManager.beginTransaction().remove(fragment).commit()
        exerciseContainer.removeView(fragment.view)
        exerciseFragmentCount--

        // Reajusta las etiquetas de los fragmentos restantes
        for (i in 0 until exerciseContainer.childCount) {
            val exerciseFragment = childFragmentManager.findFragmentByTag("exercise_$i") as? ExerciseFragment
            exerciseFragment?.let {
                val transaction = childFragmentManager.beginTransaction()
                transaction.remove(it)
                transaction.add(R.id.exercise_fragment_container, it, "exercise_$i")
                transaction.commit()
            }
        }
    }


}
