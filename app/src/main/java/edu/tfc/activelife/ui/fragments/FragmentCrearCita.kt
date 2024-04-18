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
        val editTituloCita: EditText = view.findViewById(R.id.edit_titulo_cita)
        val editDescripcionCita: EditText = view.findViewById(R.id.edit_descripcion_cita)
        val datePickerCita: DatePicker = view.findViewById(R.id.date_picker_cita)
        val btnGuardarCita: Button = view.findViewById(R.id.btn_guardar_cita)

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
                    val titulo = document.getString("titulo")
                    val descripcion = document.getString("descripcion")
                    val fecha = document.getDate("fechaCita")

                    editTituloCita.setText(titulo)
                    editDescripcionCita.setText(descripcion)
                    // Configurar la fecha del DatePicker
                    val calendar = Calendar.getInstance()
                    calendar.time = fecha
                    datePickerCita.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null)

                    // Cambiar el texto del botón a "Editar"
                    btnGuardarCita.text = "Editar"
                }
                .addOnFailureListener { exception ->
                    // Manejar el error
                    Toast.makeText(requireContext(), "Error al obtener los detalles de la cita", Toast.LENGTH_SHORT).show()
                }
        } else {
            // No hay cita para editar, estamos creando una nueva cita
            // Configurar la fecha del DatePicker con la fecha actual
            val calendar = Calendar.getInstance()
            datePickerCita.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null)
        }

        // Manejador del clic del botón
        btnGuardarCita.setOnClickListener {
            // Obtener el título de la cita
            val tituloCita = editTituloCita.text.toString().trim()

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
                "titulo" to tituloCita,
                "descripcion" to descripcionCita,
                "fechaCita" to calendar.time, // Convertir el objeto Calendar a Date
                "fechaSolicitud" to Date(), // Fecha y hora actuales
                "userUuid" to userUuid // Agregar el ID único del usuario
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
                        // Limpiar los EditText después de guardar la cita
                        editTituloCita.setText("")
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