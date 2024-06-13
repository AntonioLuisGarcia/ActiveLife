package edu.tfc.activelife.ui.fragments.routine

import android.content.Context
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

/**
 * FragmentOne is a Fragment that allows users to create or edit a workout routine.
 * Users can add exercises, set the routine's title and day, and upload media files related to the exercises.
 */
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

    /**
     * Initializes Firestore instance and retrieves the 'activo' argument if available.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        arguments?.let {
            isActive = it.getBoolean("activo", false)
        }
    }

    /**
     * Inflates the layout for this fragment and sets up the UI elements.
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
        val view = inflater.inflate(R.layout.fragment_crear_routine, container, false)
        applyBackgroundColor(view)

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


    /**
     * Called after onCreateView and applies the background color to the view.
     *
     * @param view The view returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyBackgroundColor(view)
    }

    /**
     * Applies the background color from shared preferences to the given view.
     *
     * @param view The view to which the background color is applied.
     */
    private fun applyBackgroundColor(view: View) {
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val colorResId = sharedPreferences.getInt("background_color", R.color.white)
        view.setBackgroundResource(colorResId)
    }

    /**
     * Loads routine data from Firestore using the provided routine ID.
     *
     * @param routineId The ID of the routine to load.
     */
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

    /**
     * Fills the fragment with data from the loaded routine.
     *
     * @param routineData A map containing the routine data.
     */
    private fun fillFragmentWithData(routineData: Map<String, Any>) {
        editTextTitle.setText(routineData["title"] as? String)

        val day = routineData["day"] as? String
        val daysOfWeek = resources.getStringArray(R.array.days_of_week)

        if (day != null) {
            val position = daysOfWeek.indexOf(day)
            daySpinner.setSelection(position)
        } else {
            // Set default day to "Lunes"
            val defaultDay = "Lunes"
            val position = daysOfWeek.indexOf(defaultDay)
            daySpinner.setSelection(position)
        }

        routineData["exercises"]?.let {
            (it as List<HashMap<String, Any>>).forEachIndexed { index, exerciseData ->
                addExerciseFragment(exerciseData)
            }
        }
        isActive = routineData["activo"] as? Boolean ?: false
    }

    /**
     * Uploads media files and sends routine data to Firestore.
     */
    private fun uploadMediaAndSendRoutine() {
        Log.d("FragmentOne", "uploadMediaAndSendRoutine called")
        buttonSendRoutine.isEnabled = false
        exerciseList.clear()
        uploadNextMedia(0)
    }

    /**
     * Uploads media files one by one.
     *
     * @param index The index of the exercise fragment to upload media for.
     */
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
        val bodyPart = exerciseFragment.spinnerBodyPart.selectedItem.toString()
        val mediaUri = exerciseFragment.gifUri
        val mediaUrl = exerciseFragment.gifUrl

        Log.d(
            "FragmentOne",
            "Exercise details - Name: $exerciseName, Series: $series, Repetitions: $repetitions, MediaUri: $mediaUri, MediaUrl: $mediaUrl, BodyPart: $bodyPart"
        )

        if (!validateExercise(exerciseName, series, repetitions)) {
            Log.e("FragmentOne", "Validation failed for exercise at index: $index")
            buttonSendRoutine.isEnabled = true
            return
        }

        val exerciseData: HashMap<String, Any> = hashMapOf(
            "name" to exerciseName,
            "serie" to series,
            "repeticiones" to repetitions,
            "bodyPart" to bodyPart
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

    /**
     * Sends the routine data to Firebase Firestore.
     *
     * This method gathers the routine data, including title, selected day, exercises, and user UUID.
     * It validates the input fields and uploads the routine to Firestore.
     * If an existing routine ID is provided, it updates the routine; otherwise, it creates a new one.
     */
    private fun sendRoutineToFirebase() {
        Log.d("FragmentOne", "sendRoutineToFirebase called")
        val title = editTextTitle.text.toString()
        val selectedDay = daySpinner.selectedItem.toString()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userUUID = currentUser?.uid

        if (userUUID == null) {
            Log.e("FragmentOne", "Unable to get the current user")
            Toast.makeText(requireContext(), "Unable to get the current user", Toast.LENGTH_SHORT).show()
            buttonSendRoutine.isEnabled = true
            return
        }

        if (!validateForm(title, selectedDay)) {
            Log.e("FragmentOne", "Form validation failed")
            buttonSendRoutine.isEnabled = true
            return
        }

        if (exerciseFragmentList.isEmpty()) {
            Log.e("FragmentOne", "No exercises added")
            Toast.makeText(requireContext(), "Please add at least one exercise", Toast.LENGTH_SHORT).show()
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
                    Log.d("FragmentOne", "Routine created successfully")
                    Toast.makeText(requireContext(), "Routine created successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_fragmentOne_to_fragmentTwo)
                }
                .addOnFailureListener { e ->
                    Log.e("FragmentOne", "Error creating routine", e)
                    Toast.makeText(requireContext(), "Error creating routine: ${e.message}", Toast.LENGTH_SHORT).show()
                    buttonSendRoutine.isEnabled = true
                }
        } else {
            Log.d("FragmentOne", "Updating existing routine in Firestore")
            db.collection("rutinas").document(routineId).set(routineData)
                .addOnSuccessListener {
                    Log.d("FragmentOne", "Routine updated successfully")
                    Toast.makeText(requireContext(), "Routine updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_fragmentOne_to_fragmentTwo)
                }
                .addOnFailureListener { e ->
                    Log.e("FragmentOne", "Error updating routine", e)
                    Toast.makeText(requireContext(), "Error updating routine: ${e.message}", Toast.LENGTH_SHORT).show()
                    buttonSendRoutine.isEnabled = true
                }
        }
    }

    /**
     * Adds a new ExerciseFragment to the list and the UI.
     *
     * This method creates a new ExerciseFragment instance, sets its listener, and optionally passes initial exercise data.
     * The fragment is then added to the child fragment manager and displayed in the UI.
     *
     * @param exerciseData Optional initial data for the exercise.
     */
    private fun addExerciseFragment(exerciseData: HashMap<String, Any>? = null) {
        val newExerciseFragment = ExerciseFragment.newInstance()
        newExerciseFragment.exerciseDataListener = this
        val args = Bundle()
        if (exerciseData != null) {
            args.putString("name", exerciseData["name"] as? String)
            args.putString("serie", exerciseData["serie"] as? String)
            args.putString("repeticiones", exerciseData["repeticiones"] as? String)
            args.putString("gifUrl", exerciseData["gifUrl"] as? String)
            args.putString("bodyPart", exerciseData["bodyPart"] as? String)
            newExerciseFragment.arguments = args
        }
        exerciseFragmentList.add(newExerciseFragment)
        childFragmentManager.beginTransaction()
            .add(R.id.exercise_fragment_container, newExerciseFragment, "exercise_${exerciseFragmentList.size - 1}")
            .commit()
    }

    /**
     * Displays a dialog to add predefined exercises.
     *
     * This method shows a dialog fragment that allows the user to select a predefined exercise.
     * The selected exercise's data is then used to add a new ExerciseFragment.
     */
    private fun showAddPredefinedExerciseDialog() {
        val dialog = AddExerciseDialogFragment { selectedExercise ->
            val exerciseData: HashMap<String, Any> = hashMapOf(
                "name" to selectedExercise.exercise.name,
                "serie" to selectedExercise.series.toString(),
                "repeticiones" to selectedExercise.repetitions.toString(),
                "gifUrl" to selectedExercise.exercise.gifUrl,
                "bodyPart" to selectedExercise.exercise.bodyPart // Pass the bodyPart here
            )
            addExerciseFragment(exerciseData)
        }
        dialog.show(parentFragmentManager, "AddExerciseDialogFragment")
    }

    /**
     * Handles the receipt of exercise data from an ExerciseFragment.
     *
     * This method is called when an ExerciseFragment sends updated exercise data.
     *
     * @param exerciseName The name of the exercise.
     * @param series The number of series.
     * @param repetitions The number of repetitions.
     * @param gifUrl The URL of the exercise's media.
     */
    override fun onExerciseDataReceived(exerciseName: String, series: String, repetitions: String, gifUrl: String, bodyPart: String) {
        // Handle the receipt of exercise data if necessary.
    }

    /**
     * Removes an ExerciseFragment from the list and updates the UI.
     *
     * This method removes the specified ExerciseFragment from the child fragment manager and updates the list of exercises.
     *
     * @param fragment The ExerciseFragment to remove.
     */
    override fun onRemoveExercise(fragment: ExerciseFragment) {
        childFragmentManager.beginTransaction().remove(fragment).commit()
        exerciseContainer.removeView(fragment.view)
        exerciseFragmentList.remove(fragment)

        // Update the global exercise list
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

    /**
     * Validates the routine title.
     *
     * This method checks if the title is empty. If it is, a Toast message is shown to the user
     * prompting them to enter a title, and the method returns false. Otherwise, it returns true.
     *
     * @param title The title of the routine.
     * @return True if the title is not empty, false otherwise.
     */
    private fun validateTitle(title: String): Boolean {
        return if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a title for the routine", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    /**
     * Validates the selected day.
     *
     * This method checks if the selected day is empty or if it is the default selection prompt.
     * If so, a Toast message is shown to the user prompting them to select a day, and the method returns false.
     * Otherwise, it returns true.
     *
     * @param selectedDay The selected day for the routine.
     * @return True if a valid day is selected, false otherwise.
     */
    private fun validateDay(selectedDay: String): Boolean {
        return if (selectedDay.isEmpty() || selectedDay == "Select a day") {
            Toast.makeText(requireContext(), "Please select a day for the routine", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    /**
     * Validates the exercise details.
     *
     * This method checks if the exercise name is blank, if the series and repetitions are numeric and positive.
     * If any of these conditions fail, a Toast message is shown to the user indicating the error,
     * and the method returns false. Otherwise, it returns true.
     *
     * @param exerciseName The name of the exercise.
     * @param series The number of series for the exercise.
     * @param repetitions The number of repetitions for the exercise.
     * @return True if all exercise details are valid, false otherwise.
     */
    private fun validateExercise(exerciseName: String, series: String, repetitions: String): Boolean {
        return when {
            exerciseName.isBlank() -> {
                Toast.makeText(requireContext(), "Please enter a name for the exercise", Toast.LENGTH_SHORT).show()
                false
            }
            series.isBlank() || !series.isDigitsOnly() || series.toInt() <= 0 -> {
                Toast.makeText(requireContext(), "Please enter a positive numeric value for the series", Toast.LENGTH_SHORT).show()
                false
            }
            repetitions.isBlank() || !repetitions.isDigitsOnly() || repetitions.toInt() <= 0 -> {
                Toast.makeText(requireContext(), "Please enter a positive numeric value for the repetitions", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    /**
     * Validates the routine form.
     *
     * This method validates the routine title and the selected day by calling their respective validation methods.
     *
     * @param title The title of the routine.
     * @param selectedDay The selected day for the routine.
     * @return True if both the title and the selected day are valid, false otherwise.
     */
    private fun validateForm(title: String, selectedDay: String): Boolean {
        return validateTitle(title) && validateDay(selectedDay)
    }
}
