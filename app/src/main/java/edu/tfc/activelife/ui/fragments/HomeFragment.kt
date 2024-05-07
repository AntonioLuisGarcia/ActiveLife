import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.tfc.activelife.R
import edu.tfc.activelife.utils.DateUtils
import java.util.Date

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()  // Initialize Firebase Auth
        val currentUser = auth.currentUser

        currentUser?.let {
            fetchNearestAppointment(it.uid)  // Use current user's UUID to fetch appointments
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
                        updateUI(cita)
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
            val imageView = view.findViewById<ImageView>(R.id.image_view_cita)

            tituloTextView.text = cita["titulo"] as String
            descripcionTextView.text = cita["descripcion"] as String

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

}