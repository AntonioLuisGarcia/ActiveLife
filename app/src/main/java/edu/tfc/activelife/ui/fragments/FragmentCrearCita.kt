package edu.tfc.activelife.ui.fragments

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
import edu.tfc.activelife.R
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
        val editCitaId: EditText = view.findViewById(R.id.edit_cita_id)

        // Obtener la instancia de Firestore
        val db = FirebaseFirestore.getInstance()

        val userUuid = FirebaseAuth.getInstance().currentUser?.uid

        // Obtener el ID de la cita a editar (si existe)
        val citaId = arguments?.getString("citaId")
        if (citaId != null) {
            // Se está editando una cita existente
            // Obtener los detalles de la cita existente y establecerlos en los campos correspondientes
            db.collection("citas").document(citaId)
                .get()
                .addOnSuccessListener { document ->
                    val descripcion = document.getString("descripcion")
                    val fecha = document.getDate("fecha")

                    editDescripcionCita.setText(descripcion)
                    // Configurar la fecha del DatePicker
                    val calendar = Calendar.getInstance()
                    calendar.time = fecha
                    datePickerCita.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null)

                    // Establecer el ID de la cita en el campo oculto
                    editCitaId.setText(citaId)
                    // Cambiar el texto del botón a "Editar"
                    btnGuardarCita.text = "Editar"
                }
                .addOnFailureListener { exception ->
                    // Manejar el error
                    Toast.makeText(requireContext(), "Error al obtener los detalles de la cita", Toast.LENGTH_SHORT).show()
                }
        }

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
            if (citaId != null) {
                // Se está editando una cita existente
                // Actualizar los detalles de la cita en Firestore
                db.collection("citas").document(citaId)
                    .update(nuevaCita as Map<String, Any>)
                    .addOnSuccessListener {
                        // La cita se actualizó correctamente
                        // Navegar de regreso al FragmentThree
                        findNavController().navigateUp()
                    }
                    .addOnFailureListener { e ->
                        // Manejar el error
                        Toast.makeText(requireContext(), "Error al editar la cita", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Se está creando una nueva cita
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
                        Toast.makeText(requireContext(), "Error al crear la cita", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        return view
    }
}
