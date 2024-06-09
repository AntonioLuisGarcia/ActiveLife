package edu.tfc.activelife.ui.fragments.home

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.tfc.activelife.R
import edu.tfc.activelife.adapters.ExerciseSwiperAdapter
import edu.tfc.activelife.dao.PublicExercise
import edu.tfc.activelife.dao.Routine
import edu.tfc.activelife.utils.Utils
import java.util.Calendar
import java.util.Date

/**
 * HomeFragment is responsible for displaying the home screen of the application.
 * It handles user authentication, fetching the nearest appointment and routine,
 * and setting up the UI elements accordingly.
 */
class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private var publicExercises: MutableList<PublicExercise> = mutableListOf()
    private var routineUuid: String = ""
    private var citaUuid: String = ""
    private var publicRoutineData: Map<String, Any>? = null
    private lateinit var textViewFragmentOne: TextView

    /**
     * Initializes the fragment and retrieves the Firebase authentication instance.
     * If the user is authenticated, fetches the nearest appointment and routine.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        currentUser?.let {
            fetchNearestAppointment(it.uid)
            fetchRoutine(it.uid)
        } ?: println("User not logged in")
    }


    /**
     * Inflates the fragment's layout.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     * @return The inflated view of the fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        applyBackgroundColor(view)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    /**
     * Applies the background color to the provided view based on the user's saved preferences.
     *
     * @param view The view to which the background color should be applied.
     */
    private fun applyBackgroundColor(view: View) {
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val colorResId = sharedPreferences.getInt("background_color", R.color.white)
        view.setBackgroundResource(colorResId)
    }

    /**
    * Called immediately after the view has been created.
    * Initializes the views and sets up listeners for interactive elements.
    *
    * @param view The view created by onCreateView.
    * @param savedInstanceState If non-null, this is the previously saved state of the fragment.
    */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyBackgroundColor(view)
        textViewFragmentOne = view.findViewById<TextView>(R.id.text_view_fragment_one)
        val textViewCrearCita = view.findViewById<TextView>(R.id.text_view_crear_cita)

        textViewFragmentOne.text = "Copiar rutina"

        textViewFragmentOne.setOnClickListener {
            if (publicRoutineData != null) {
                showCopyConfirmationDialog()
            } else {
                Toast.makeText(context, "No hay rutina para copiar", Toast.LENGTH_SHORT).show()
            }
        }

        textViewCrearCita.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToFragmentCrearCita(citaUuid)
            findNavController().navigate(action)
        }
    }

    /**
     * Sets up the ViewPager2 with the public exercises adapter.
     */
     fun setupViewPager() {
        val viewPager: ViewPager2 = view?.findViewById(R.id.viewPagerExercises) ?: return
        viewPager.adapter = ExerciseSwiperAdapter(this, publicExercises)
    }

    /**
     * Fetches the nearest appointment for the specified user from Firestore.
     *
     * @param userId The ID of the user for whom to fetch the nearest appointment.
     */
    private fun fetchNearestAppointment(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val currentDate = com.google.firebase.Timestamp.now()

        db.collection("citas")
            .whereEqualTo("userUUID", userId)
            .whereGreaterThanOrEqualTo("fechaCita", currentDate)
            .orderBy("fechaCita", Query.Direction.ASCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    updateCitaUI(null)
                } else {
                    val document = documents.first()
                    citaUuid = document.id
                    val cita = document.data
                    val encargadoUuid = document.getString("encargadoUuid") ?: ""
                    getEncargadoUsername(encargadoUuid) { nombre ->
                        cita["encargadoUuid"] = nombre
                        updateCitaUI(cita)
                    }
                }
            }
            .addOnFailureListener {
                updateCitaUI(null)
            }
    }

    /**
     * Fetches the active routine for the user for the current day of the week.
     * If no routine is found, attempts to fetch a public routine.
     *
     * This function queries the "rutinas" collection in Firestore to find the user's active routine.
     * If an active routine is not found, it fetches a public routine as a fallback.
     *
     * @param userId The ID of the user for whom to fetch the routine.
     */
    private fun fetchRoutine(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val dayOfWeekMap = mapOf(
            Calendar.MONDAY to "Lunes",
            Calendar.TUESDAY to "Martes",
            Calendar.WEDNESDAY to "Miércoles",
            Calendar.THURSDAY to "Jueves",
            Calendar.FRIDAY to "Viernes",
            Calendar.SATURDAY to "Sábado",
            Calendar.SUNDAY to "Domingo"
        )

        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val todayString = dayOfWeekMap[today] ?: ""

        db.collection("rutinas")
            .whereEqualTo("userUUID", userId)
            .whereEqualTo("activo", true)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    fetchPublicRoutine()
                } else {
                    val routines = documents.map { it.data }
                    routineUuid = documents.first().id
                    val nearestRoutine = getNearestRoutine(routines, dayOfWeekMap, today)
                    if (nearestRoutine != null) {
                        loadRoutineData(nearestRoutine)
                    } else {
                        fetchPublicRoutine()
                    }
                }
            }
            .addOnFailureListener {
                fetchPublicRoutine()
            }
    }

    /**
     * Fetches a public routine if the user does not have an active routine.
     * The public routine data is loaded and the UI is updated accordingly.
     */
    private fun fetchPublicRoutine() {
        val db = FirebaseFirestore.getInstance()
        db.collection("rutinas")
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    publicRoutineData = documents.first().data
                    showNoRoutineDialog()
                    textViewFragmentOne.text = "Copiar rutina"
                    loadRoutineData(publicRoutineData!!)
                }
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    /**
     * Shows a confirmation dialog to the user asking if they want to copy the public routine.
     * If the user confirms, the routine is copied to their account.
     */
    private fun showCopyConfirmationDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmación")
        builder.setMessage("¿Estás seguro que deseas copiar esta rutina?")
        builder.setPositiveButton("Sí") { _, _ ->
            copyRoutineToUser()
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    /**
     * Copies the public routine data to the current user's account in Firestore.
     * Updates the user interface to reflect the newly copied routine.
     */
    private fun copyRoutineToUser() {
        val userUuid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val currentDayOfWeek = Utils.getCurrentDayOfWeek()

        val exercisesList = publicRoutineData?.get("exercises") as? List<Map<String, Any>> ?: emptyList()

        val routineData = hashMapOf(
            "title" to (publicRoutineData?.get("title") ?: ""),
            "day" to currentDayOfWeek,
            "exercises" to exercisesList,
            "userUUID" to userUuid,
            "activo" to false
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("rutinas").add(routineData)
            .addOnSuccessListener {
                Toast.makeText(context, "Rutina copiada exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al copiar rutina", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Shows a dialog indicating that there is no active routine available for the user.
     */
    private fun showNoRoutineDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.no_active_routine)
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    /**
     * Updates the user interface with the appointment data.
     *
     * @param cita The appointment data as a Map, or null if there is no appointment.
     */
    private fun updateCitaUI(cita: Map<String, Any>?) {
        view?.let { view ->
            val tituloTextView = view.findViewById<TextView>(R.id.text_view_titulo)
            val descripcionTextView = view.findViewById<TextView>(R.id.text_view_descripcion)
            val fechaTextView = view.findViewById<TextView>(R.id.text_fecha_cita)
            val encargadoTextView = view.findViewById<TextView>(R.id.text_view_encargado)
            val imageView = view.findViewById<ImageView>(R.id.image_view_cita)

            if (cita == null) {
                tituloTextView.text = "Crear Cita"
                descripcionTextView.text = ""
                fechaTextView.text = ""
                encargadoTextView.text = ""
                imageView.visibility = View.GONE
                citaUuid = ""
            } else {
                tituloTextView.text = cita["titulo"] as String
                descripcionTextView.text = cita["descripcion"] as String
                encargadoTextView.text = cita["encargadoUuid"] as? String ?: "Sin encargado"

                val timestamp = cita["fechaCita"] as? com.google.firebase.Timestamp
                val formattedDate = timestamp?.let { ts ->
                    Utils.formatFirebaseTimestamp(ts.seconds, ts.nanoseconds.toInt())
                } ?: "Fecha no disponible"
                fechaTextView.text = formattedDate

                val imageUrl = cita["imagen"] as? String
                if (imageUrl.isNullOrEmpty()) {
                    imageView.visibility = View.GONE
                } else {
                    Glide.with(this).load(imageUrl).into(imageView)
                }
                view.findViewById<TextView>(R.id.text_view_crear_cita)?.text = "Editar cita"
            }
        }
    }

    /**
     * Updates the user interface with the routine data.
     *
     * @param routineData The routine data as a Map, or null if there is no routine.
     */
    private fun updateRoutineUI(routineData: Map<String, Any>?) {
        view?.let { view ->
            val routineTitle = view.findViewById<TextView>(R.id.text_view_titulo_rutina)
            val viewPagerExercises = view.findViewById<ViewPager2>(R.id.viewPagerExercises)

            if (routineData == null) {
                routineTitle.text = "Crear Rutina"
                viewPagerExercises.visibility = View.GONE
                routineUuid = ""
            } else {
                routineTitle.text = routineData["title"] as? String ?: "Título no disponible"
                viewPagerExercises.visibility = View.VISIBLE
                publicExercises = mapExercisesToPublicExercises(routineData)
                setupViewPager()
                view.findViewById<TextView>(R.id.text_view_fragment_one)?.text = "Editar rutina"
            }
        }
    }

    /**
     * Finds the nearest routine based on the current day of the week.
     *
     * @param routines List of routines.
     * @param dayOfWeekMap Map of day of week integers to day names.
     * @param today The current day of the week.
     * @return The nearest routine data or null if no routine matches.
     */
    private fun getNearestRoutine(routines: List<Map<String, Any>>, dayOfWeekMap: Map<Int, String>, today: Int): Map<String, Any>? {
        return routines.minByOrNull { routine ->
            val day = routine["day"] as? String ?: ""
            dayOfWeekMap.entries.find { it.value == day }?.key?.let { routineDay ->
                if (routineDay >= today) routineDay - today else 7 - (today - routineDay)
            } ?: 7
        }
    }

    /**
     * Loads the routine data into the user interface.
     *
     * @param routineData The routine data to load.
     */
    private fun loadRoutineData(routineData: Map<String, Any>) {
        updateRoutineUI(routineData)
    }

    /**
     * Maps the exercise data from the routine to a list of PublicExercise objects.
     *
     * @param routineData The routine data containing the exercises.
     * @return A mutable list of PublicExercise objects.
     */
    private fun mapExercisesToPublicExercises(routineData: Map<String, Any>): MutableList<PublicExercise> {
        val exercisesData = routineData["exercises"] as? List<HashMap<String, Any>> ?: return mutableListOf()
        return exercisesData.mapNotNull { exerciseData ->
            try {
                PublicExercise(
                    uuid = exerciseData["id"] as? String ?: "",
                    exerciseName = exerciseData["exerciseName"] as? String ?: "",
                    series = exerciseData["serie"] as? String ?: "",
                    repetitions = exerciseData["repeticiones"] as? String ?: "",
                    description = exerciseData["description"] as? String ?: "",
                    bodyPart = exerciseData["bodyPart"] as? String ?: "",
                    equipment = exerciseData["equipment"] as? String ?: "",
                    gifUrl = exerciseData["gifUrl"] as? String ?: "",
                    instructions = (exerciseData["instructions"] as? List<String> ?: listOf()),
                    target = exerciseData["target"] as? String ?: "",
                    secondaryMuscles = (exerciseData["secondaryMuscles"] as? List<String> ?: listOf()),
                    public = exerciseData["public"] as? Boolean ?: false,
                    title = exerciseData["title"] as? String ?: "",
                    userUUID = exerciseData["userUUID"] as? String ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }.toMutableList()
    }

    /**
     * Retrieves the username of the person in charge based on their UUID.
     *
     * @param encargadoUuid The UUID of the person in charge.
     * @param callback A callback function to handle the retrieved username.
     */
    private fun getEncargadoUsername(encargadoUuid: String, callback: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        if (encargadoUuid.isNotEmpty()) {
            db.collection("users")
                .whereEqualTo("uuid", encargadoUuid)
                .get()
                .addOnSuccessListener { encargadoDocs ->
                    if (!encargadoDocs.isEmpty) {
                        val encargadoDoc = encargadoDocs.documents[0]
                        val nombre = encargadoDoc.getString("username") ?: "Nombre no disponible"
                        callback(nombre)
                    } else {
                        callback("Sin encargado")
                    }
                }
                .addOnFailureListener {
                    callback("Sin encargado")
                }
        } else {
            callback("Sin encargado")
        }
    }
}