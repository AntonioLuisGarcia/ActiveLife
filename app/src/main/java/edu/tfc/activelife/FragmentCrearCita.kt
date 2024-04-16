package edu.tfc.activelife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class FragmentCrearCita : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_crear_cita, container, false)

        // Referencias a los elementos de la interfaz
        val editDescripcionCita: EditText = view.findViewById(R.id.edit_descripcion_cita)
        val datePickerCita: DatePicker = view.findViewById(R.id.date_picker_cita)
        val btnGuardarCita: Button = view.findViewById(R.id.btn_guardar_cita)

        // Obtener la instancia de Firestore
        val db = FirebaseFirestore.getInstance()

        val userUuid = FirebaseAuth.getInstance().currentUser?.uid

        // Manejador del clic del botón
        btnGuardarCita.setOnClickListener {
            // Obtener la descripción de la cita
            val descripcionCita = editDescripcionCita.text.toString().trim()

            // Obtener la fecha de la cita del DatePicker
            val year = datePickerCita.year
            val month = datePickerCita.month
            val day = datePickerCita.dayOfMonth

            // Crear un objeto Calendar para la fecha seleccionada
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)

            // Crear un mapa con los datos de la nueva cita
            val nuevaCita = hashMapOf(
                "descripcion" to descripcionCita,
                "fecha" to calendar.time, // Convertir el objeto Calendar a Date
                "userId" to userUuid // Agregar el ID único del usuario
            )

            // Subir los datos de la nueva cita a Firestore
            db.collection("citas")
                .add(nuevaCita)
                .addOnSuccessListener { documentReference ->
                    // La cita se agregó correctamente
                    // Limpiar el EditText después de guardar la cita
                    editDescripcionCita.setText("")
                    // Navegar de regreso al FragmentThree
                    findNavController().navigateUp()
                }
                .addOnFailureListener { e ->
                    // Manejar el error
                    Toast.makeText(requireContext(), "Fallo en la creación", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }
}
