package edu.tfc.activelife.ui.fragments.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import edu.tfc.activelife.utils.Utils
import java.util.Calendar
import java.util.Date

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private var publicExercises: MutableList<PublicExercise> = mutableListOf()
    private lateinit var routineUuid: String
    private lateinit var citaUuid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()  // Initialize Firebase Auth
        val currentUser = auth.currentUser

        currentUser?.let {
            fetchNearestAppointment(it.uid)  // Use current user's UUID to fetch appointments
            fetchRoutine(it.uid)
        } ?: println("User not logged in")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textViewFragmentOne = view?.findViewById<TextView>(R.id.text_view_fragment_one)
        val textViewCrearCita = view?.findViewById<TextView>(R.id.text_view_crear_cita)

        textViewFragmentOne?.setOnClickListener {
            if (routineUuid.isNotEmpty()) {
                Toast.makeText(context, "Navigating to Fragment One", Toast.LENGTH_SHORT).show()
                val action = HomeFragmentDirections.actionHomeFragmentToFragmentOne(routineUuid)
                findNavController().navigate(action)
            } else {
                Toast.makeText(context, "Routine UUID is not available", Toast.LENGTH_SHORT).show()
            }
        }

        textViewCrearCita?.setOnClickListener {
            if (citaUuid.isNotEmpty()) {
                Toast.makeText(context, "Navigating to Crear Cita", Toast.LENGTH_SHORT).show()
                val action = HomeFragmentDirections.actionHomeFragmentToFragmentCrearCita(citaUuid)
                findNavController().navigate(action)
            } else {
                Toast.makeText(context, "Cita UUID is not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupViewPager() {
        val viewPager: ViewPager2 = view?.findViewById(R.id.viewPagerExercises) ?: return
        viewPager.adapter = ExerciseSwiperAdapter(this, publicExercises)
    }

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
                    Log.d("HomeFragment", "No upcoming appointments found.")
                } else {
                    val document = documents.first()
                    citaUuid = document.id
                    val cita = document.data
                    Log.d("HomeFragment", "Nearest appointment found: $cita")
                    val encargadoUuid = document.getString("encargadoUuid") ?: ""
                    getEncargadoUsername(encargadoUuid) { nombre ->
                        cita["encargadoUuid"] = nombre
                        updateUI(cita)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Error getting documents: $exception")
            }
    }

    private fun updateUI(cita: Map<String, Any>) {
        view?.let { view ->
            val tituloTextView = view.findViewById<TextView>(R.id.text_view_titulo)
            val descripcionTextView = view.findViewById<TextView>(R.id.text_view_descripcion)
            val fechaTextView = view.findViewById<TextView>(R.id.text_fecha_cita)
            val encargadoTextView = view.findViewById<TextView>(R.id.text_view_encargado)
            val imageView = view.findViewById<ImageView>(R.id.image_view_cita)

            tituloTextView.text = cita["titulo"] as String
            descripcionTextView.text = cita["descripcion"] as String
            encargadoTextView.text = cita["encargadoUuid"] as? String ?: "Sin encargado"

            // Extraer Timestamp y formatearlo
            val timestamp = cita["fechaCita"] as? com.google.firebase.Timestamp
            val formattedDate = timestamp?.let { ts ->
                Utils.formatFirebaseTimestamp(ts.seconds, ts.nanoseconds.toInt())
            } ?: "Fecha no disponible"

            fechaTextView.text = formattedDate

            // Si tienes un URL de imagen en `cita["image"]`, úsalo aquí
            val imageUrl = cita["imagen"]
            if (imageUrl?.equals("") == true) {
                imageView.visibility = View.GONE  // Oculta el ImageView si no hay imagen
            } else {
                Glide.with(this).load(imageUrl).into(imageView)
            }
        }
    }

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
        Log.d("edu.tfc.activelife.ui.fragments.home.HomeFragment", "Today is $todayString")
        Toast.makeText(context, "Today is $todayString", Toast.LENGTH_SHORT).show()

        db.collection("rutinas")
            .whereEqualTo("userUUID", userId)
            .whereEqualTo("activo", true)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("edu.tfc.activelife.ui.fragments.home.HomeFragment", "No active routines found.")
                    Toast.makeText(context, "No active routines found.", Toast.LENGTH_SHORT).show()
                } else {
                    val routines = documents.map { it.data }
                    routineUuid = documents.first().id
                    val nearestRoutine = getNearestRoutine(routines, dayOfWeekMap, today)
                    if (nearestRoutine != null) {
                        loadRoutineData(nearestRoutine)
                    } else {
                        Log.d("edu.tfc.activelife.ui.fragments.home.HomeFragment", "No active routines found for this week.")
                        Toast.makeText(context, "No active routines found for this week.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("edu.tfc.activelife.ui.fragments.home.HomeFragment", "Error fetching routines: $exception")
                Toast.makeText(context, "Error fetching routines: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getNearestRoutine(routines: List<Map<String, Any>>, dayOfWeekMap: Map<Int, String>, today: Int): Map<String, Any>? {
        return routines.minByOrNull { routine ->
            val day = routine["day"] as? String ?: ""
            dayOfWeekMap.entries.find { it.value == day }?.key?.let { routineDay ->
                if (routineDay >= today) routineDay - today else 7 - (today - routineDay)
            } ?: 7 // default to last priority if day not found
        }
    }

    private fun loadRoutineData(routineData: Map<String, Any>) {
        Log.d("edu.tfc.activelife.ui.fragments.home.HomeFragment", "Routine data: $routineData")
        Toast.makeText(context, "Routine loaded successfully", Toast.LENGTH_SHORT).show()
        mapExercisesToPublicExercises(routineData)
        updateRoutineUI(routineData)
    }

    private fun mapExercisesToPublicExercises(routineData: Map<String, Any>) {
        val exercisesData = routineData["exercises"] as? List<HashMap<String, Any>> ?: return
        publicExercises = exercisesData.mapNotNull { exerciseData ->
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
                null  // Si ocurre un error al convertir, retorna null para este ejercicio
            }
        }.toMutableList()
        setupViewPager()

        // Ahora publicExercises contiene todos los ejercicios mapeados correctamente
        println("Ejercicios cargados: ${publicExercises.size}")
    }

    private fun updateRoutineUI(routineData: Map<String, Any>) {
        val routineTitle = routineData["title"] as? String ?: "Título no disponible"
        view?.findViewById<TextView>(R.id.text_view_titulo_rutina)?.text = routineTitle
    }

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