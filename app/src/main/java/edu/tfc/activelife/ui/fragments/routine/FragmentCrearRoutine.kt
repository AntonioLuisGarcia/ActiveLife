package edu.tfc.activelife.ui.fragments.routine

import android.os.Bundle
import android.util.Log
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
    private var exerciseFragmentList = mutableListOf<ExerciseFragment>()
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
        val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, daysOfWeek)
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        daySpinner.adapter = spinnerAdapter

        buttonSendRoutine.setOnClickListener {
            uploadMediaAndSendRoutine()
        }

        val addExerciseButton: Button = view.findViewById(R.id.addExerciseText)
        addExerciseButton.setOnClickListener {
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
                    Toast.makeText(requireContext(), "No se encontró la rutina", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Error al buscar la rutina: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
                addExerciseFragment(exerciseData)
            }
        }
        isActive = routineData["activo"] as? Boolean ?: false
    }

    private fun uploadMediaAndSendRoutine() {
        Log.d("FragmentOne", "uploadMediaAndSendRoutine called")
        buttonSendRoutine.isEnabled = false
        exerciseList.clear()
        uploadNextMedia(0)
    }

    private fun uploadNextMedia(index: Int) {
        Log.d("FragmentOne", "uploadNextMedia called with index: $index")
        if (index >= exerciseFragmentList.size) {
            Log.d("FragmentOne", "All media uploaded, proceeding to sendRoutineToFirebase")
            sendRoutineToFirebase()
            return
        }

        val exerciseFragment = exerciseFragmentList[index]
        val exerciseName = exerciseFragment.editTextExerciseName.text.toString()
        val series = exerciseFragment.editTextSeries.text.toString()
        val repetitions = exerciseFragment.editTextRepetitions.text.toString()
        val mediaUri = exerciseFragment.gifUri
        val mediaUrl = exerciseFragment.gifUrl

        Log.d(
            "FragmentOne",
            "Exercise details - Name: $exerciseName, Series: $series, Repetitions: $repetitions, MediaUri: $mediaUri, MediaUrl: $mediaUrl"
        )

        if (!validateExercise(exerciseName, series, repetitions)) {
            Log.e("FragmentOne", "Validation failed for exercise at index: $index")
            buttonSendRoutine.isEnabled = true
            return
        }

        val exerciseData: HashMap<String, Any> = hashMapOf(
            "name" to exerciseName,
            "serie" to series,
            "repeticiones" to repetitions
        )

        if (mediaUri != null && (mediaUri.toString().startsWith("content://") || mediaUri.toString()
                .startsWith("file://"))
        ) {
            Log.d("FragmentOne", "Uploading media from URI: $mediaUri")
            val storageRef =
                FirebaseStorage.getInstance().reference.child("exercise_media/${mediaUri.lastPathSegment}")
            storageRef.putFile(mediaUri).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    Log.d("FragmentOne", "Media uploaded successfully, URL: $downloadUrl")
                    exerciseData["gifUrl"] = downloadUrl.toString()
                    exerciseList.add(exerciseData)
                    uploadNextMedia(index + 1)
                }.addOnFailureListener {
                    Log.e("FragmentOne", "Error getting download URL", it)
                    Toast.makeText(
                        requireContext(),
                        "Error al obtener la URL de descarga",
                        Toast.LENGTH_SHORT
                    ).show()
                    buttonSendRoutine.isEnabled = true
                }
            }.addOnFailureListener {
                Log.e("FragmentOne", "Error uploading file", it)
                Toast.makeText(requireContext(), "Error al subir el archivo", Toast.LENGTH_SHORT)
                    .show()
                buttonSendRoutine.isEnabled = true
            }
        } else if (mediaUri != null && mediaUri.toString().startsWith("http")) {
            Log.d("FragmentOne", "Using existing media URL: $mediaUri")
            exerciseData["gifUrl"] = mediaUri
            exerciseList.add(exerciseData)
            uploadNextMedia(index + 1)
        } else {
            Log.d("FragmentOne", "No media to upload for this exercise")
            exerciseList.add(exerciseData)
            uploadNextMedia(index + 1)
        }
    }

    private fun sendRoutineToFirebase() {
        Log.d("FragmentOne", "sendRoutineToFirebase called")
        val title = editTextTitle.text.toString()
        val selectedDay = daySpinner.selectedItem.toString()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userUUID = currentUser?.uid

        if (userUUID == null) {
            Log.e("FragmentOne", "No se ha podido obtener el usuario actual")
            Toast.makeText(requireContext(), "No se ha podido obtener el usuario actual", Toast.LENGTH_SHORT).show()
            buttonSendRoutine.isEnabled = true
            return
        }

        if (!validateForm(title, selectedDay)) {
            Log.e("FragmentOne", "Form validation failed")
            buttonSendRoutine.isEnabled = true
            return
        }

        if (exerciseFragmentList.isEmpty()) {
            Log.e("FragmentOne", "No hay ejercicios agregados")
            Toast.makeText(requireContext(), "Por favor, añade al menos un ejercicio", Toast.LENGTH_SHORT).show()
            buttonSendRoutine.isEnabled = true
            return
        }

        val routineData = hashMapOf(
            "title" to title,
            "day" to selectedDay,
            "exercises" to exerciseList,
            "userUUID" to userUUID,
            "activo" to isActive
        )

        val routineId = arguments?.getString("routineId")
        if (routineId.isNullOrEmpty()) {
            Log.d("FragmentOne", "Adding new routine to Firestore")
            db.collection("rutinas").add(routineData)
                .addOnSuccessListener {
                    Log.d("FragmentOne", "Rutina creada exitosamente")
                    Toast.makeText(requireContext(), "Rutina creada exitosamente", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener { e ->
                    Log.e("FragmentOne", "Error al crear rutina", e)
                    Toast.makeText(requireContext(), "Error al crear rutina: ${e.message}", Toast.LENGTH_SHORT).show()
                    buttonSendRoutine.isEnabled = true
                }
        } else {
            Log.d("FragmentOne", "Updating existing routine in Firestore")
            db.collection("rutinas").document(routineId).set(routineData)
                .addOnSuccessListener {
                    Log.d("FragmentOne", "Rutina actualizada exitosamente")
                    Toast.makeText(requireContext(), "Rutina actualizada exitosamente", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener { e ->
                    Log.e("FragmentOne", "Error al actualizar rutina", e)
                    Toast.makeText(requireContext(), "Error al actualizar rutina: ${e.message}", Toast.LENGTH_SHORT).show()
                    buttonSendRoutine.isEnabled = true
                }
        }
    }

    private fun addExerciseFragment(exerciseData: HashMap<String, Any>? = null) {
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
        exerciseFragmentList.add(newExerciseFragment)
        childFragmentManager.beginTransaction()
            .add(R.id.exercise_fragment_container, newExerciseFragment, "exercise_${exerciseFragmentList.size - 1}")
            .commit()
    }

    private fun showAddPredefinedExerciseDialog() {
        val dialog = AddExerciseDialogFragment { selectedExercise ->
            val exerciseData: HashMap<String, Any> = hashMapOf(
                "name" to selectedExercise.exercise.name,
                "serie" to selectedExercise.series.toString(),
                "repeticiones" to selectedExercise.repetitions.toString(),
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
        exerciseFragmentList.remove(fragment)

        // Actualizar la lista de ejercicios global
        val remainingExercises = exerciseFragmentList.map { exerciseFragment ->
            hashMapOf<String, Any>(
                "name" to (exerciseFragment.editTextExerciseName?.text?.toString() ?: ""),
                "serie" to (exerciseFragment.editTextSeries?.text?.toString() ?: ""),
                "repeticiones" to (exerciseFragment.editTextRepetitions?.text?.toString() ?: ""),
                "gifUrl" to (exerciseFragment.gifUrl ?: "")
            )
        }
        exerciseList = remainingExercises.toMutableList()
        Log.d("FragmentOne", "Exercise list after removal: $exerciseList")
    }

    private fun validateTitle(title: String): Boolean {
        return if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor ingresa un título para la rutina", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun validateDay(selectedDay: String): Boolean {
        return if (selectedDay.isEmpty() || selectedDay == "Selecciona un día") {
            Toast.makeText(requireContext(), "Por favor selecciona un día para la rutina", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun validateExercise(exerciseName: String, series: String, repetitions: String): Boolean {
        return when {
            exerciseName.isBlank() -> {
                Toast.makeText(requireContext(), "Por favor, ingresa un nombre para el ejercicio", Toast.LENGTH_SHORT).show()
                false
            }
            series.isBlank() || !series.isDigitsOnly() || series.toInt() <= 0 -> {
                Toast.makeText(requireContext(), "Por favor, ingresa un valor numérico positivo para las series", Toast.LENGTH_SHORT).show()
                false
            }
            repetitions.isBlank() || !repetitions.isDigitsOnly() || repetitions.toInt() <= 0 -> {
                Toast.makeText(requireContext(), "Por favor, ingresa un valor numérico positivo para las repeticiones", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun validateForm(title: String, selectedDay: String): Boolean {
        return validateTitle(title) && validateDay(selectedDay)
    }
}
