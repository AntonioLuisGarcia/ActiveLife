import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.tfc.activelife.R
import edu.tfc.activelife.adapters.ExerciseSwiperAdapter
import edu.tfc.activelife.dao.PublicExercise
import edu.tfc.activelife.utils.DateUtils
import java.util.Date

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private var publicExercises: MutableList<PublicExercise> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()  // Initialize Firebase Auth
        val currentUser = auth.currentUser

        currentUser?.let {
            fetchNearestAppointment(it.uid)  // Use current user's UUID to fetch appointments
            fetchRoutine("2sQjt576ywoChF6nqVtX ")  // Carga la rutina específica por UUID
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
    }

    private fun setupViewPager() {
        val viewPager: ViewPager2 = view?.findViewById(R.id.viewPagerExercises) ?: return
        viewPager.adapter = ExerciseSwiperAdapter(this, publicExercises)
    }

    private fun fetchNearestAppointment(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val currentDate = Date()

        db.collection("citas")
            .whereEqualTo("userUuid", userId)
            //.whereGreaterThanOrEqualTo("fechaCita", currentDate)
            //.orderBy("fechaCita", Query.Direction.ASCENDING)
            //.limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    println("No upcoming appointments found.")
                } else {
                    for (document in documents) {
                        val cita = document.data
                        val encargadoUuid = document.getString("encargadoUuid") ?: ""
                        getEncargadoUsername(encargadoUuid) { nombre ->
                            cita["encargadoUuid"] = nombre
                            updateUI(cita)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
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
            encargadoTextView.text = cita["encargadoNombre"] as? String ?: "Sin encargado"

            // Extraer Timestamp y formatearlo
            val timestamp = cita["fechaCita"] as? com.google.firebase.Timestamp
            val formattedDate = timestamp?.let { ts ->
                DateUtils.formatFirebaseTimestamp(ts.seconds, ts.nanoseconds.toInt())
            } ?: "Fecha no disponible"

            fechaTextView.text = formattedDate

            // Si tienes un URL de imagen en `cita["image"]`, úsalo aquí
            val imageUrl = "https://firebasestorage.googleapis.com/v0/b/activelife-74fc2.appspot.com/o/images%2FdcU88zySTheCcEi6eKnxUrUvCyq2%2F95738596-644e-4946-b2e3-89709e5fe256.jpg?alt=media&token=c857523f-1660-4798-8488-f1fa764e056e"
            if (imageUrl.isNullOrEmpty()) {
                imageView.visibility = View.GONE  // Oculta el ImageView si no hay imagen
            } else {
                Glide.with(this).load(imageUrl).into(imageView)
            }
        }
    }

    private fun fetchRoutine(routineUuid: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("rutinas").document(routineUuid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val routineData = document.data ?: return@addOnSuccessListener
                    // Mapea los datos de ejercicios a objetos PublicExercise
                    mapExercisesToPublicExercises(routineData)
                    updateRoutineUI(routineData)
                } else {
                    println("No se encontró la rutina.")
                }
            }
            .addOnFailureListener { exception ->
                println("Error al cargar la rutina: $exception")
            }
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

