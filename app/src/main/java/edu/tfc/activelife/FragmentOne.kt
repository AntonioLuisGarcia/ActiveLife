import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.firebase.firestore.FirebaseFirestore
import edu.tfc.activelife.R

class FragmentOne : Fragment() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var buttonSendRoutine: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_one, container, false)

        // Inicializar Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Obtener referencias a los elementos del diseño
        editTextTitle = view.findViewById(R.id.editTextTitle)
        editTextDescription = view.findViewById(R.id.editTextDescription)
        buttonSendRoutine = view.findViewById(R.id.buttonSendRoutine)

        // Configurar el clic del botón para enviar la rutina
        buttonSendRoutine.setOnClickListener {
            sendRoutineToFirebase()
        }

        return view
    }

    private fun sendRoutineToFirebase() {
        // Obtener los valores ingresados por el usuario
        val title = editTextTitle.text.toString()
        val description = editTextDescription.text.toString()

        // Crear un nuevo documento en la colección "rutinas" con los valores ingresados
        val routine = hashMapOf(
            "title" to title,
            "description" to description
        )

        // Agregar el documento a la colección "rutinas" en Firebase Firestore
        db.collection("rutinas")
            .add(routine)
            .addOnSuccessListener { documentReference ->
                // Rutina enviada con éxito
                // Puedes agregar aquí cualquier lógica adicional después de enviar la rutina
            }
            .addOnFailureListener { e ->
                // Error al enviar la rutina
                // Puedes manejar aquí el error y mostrar un mensaje al usuario
            }
    }
}
