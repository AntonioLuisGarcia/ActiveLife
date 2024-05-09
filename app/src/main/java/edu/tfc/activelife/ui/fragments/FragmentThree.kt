package edu.tfc.activelife.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.tfc.activelife.R
import edu.tfc.activelife.adapters.CitasAdapter
import edu.tfc.activelife.dao.Cita
import edu.tfc.activelife.utils.DateUtils

class FragmentThree : Fragment() {

    private lateinit var recyclerViewCitas: RecyclerView
    private lateinit var citasAdapter: CitasAdapter
    private lateinit var btnAddCita: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_three, container, false)

        // Referencia al RecyclerView
        recyclerViewCitas = view.findViewById(R.id.recycler_view_citas)

        // Configurar el RecyclerView
        recyclerViewCitas.layoutManager = LinearLayoutManager(requireContext())
        citasAdapter = CitasAdapter(emptyList(), requireContext()) // Pasa una lista vacía inicialmente
        recyclerViewCitas.adapter = citasAdapter

        // Obtener datos de Firestore y actualizar el adaptador
        fetchCitasFromFirestore()

        // Referencia al botón
        btnAddCita = view.findViewById(R.id.btn_add_cita)

        // Manejador del clic del botón
        btnAddCita.setOnClickListener {
            // Navegar al fragmento FragmentCrearCita al hacer clic en el botón
            findNavController().navigate(R.id.action_fragmentThree_to_fragmentCrearCita)
        }

        return view
    }

    private fun fetchCitasFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userUuid = currentUser.uid
            db.collection("citas")
                .whereEqualTo("userUuid", userUuid)  // Filtrar por userUuid
                .get()
                .addOnSuccessListener { result ->
                    val citasList = mutableListOf<Cita>()
                    for (document in result) {
                        val citaId = document.id
                        val title = document.getString("titulo") ?: ""
                        val descripcion = document.getString("descripcion") ?: ""
                        val fechaTimestamp = document.getTimestamp("fechaCita")
                        val formattedDate = fechaTimestamp?.let { ts ->
                            DateUtils.formatFirebaseTimestamp(ts.seconds, ts.nanoseconds.toInt())
                        } ?: "Fecha no disponible"
                        val imageUrl = document.getString("image") ?: ""
                        val cita = Cita(citaId, title, descripcion, formattedDate, imageUrl)

                        citasList.add(cita)
                    }
                    citasAdapter.updateData(citasList)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error al cargar citas: ${exception.message}", Toast.LENGTH_SHORT).show()
                }

            db.collection("citas")
                .whereEqualTo("userUuid", userUuid)  // Aplicar el mismo filtro al escuchador de cambios
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val citasList = mutableListOf<Cita>()
                        for (document in snapshot) {
                            val citaId = document.id
                            val title = document.getString("titulo") ?: ""
                            val descripcion = document.getString("descripcion") ?: ""
                            val fechaTimestamp = document.getTimestamp("fechaCita")
                            val fecha = fechaTimestamp.toString()
                            val imageUrl = document.getString("image") ?: ""
                            val cita = Cita(citaId, title, descripcion, fecha, imageUrl)

                            citasList.add(cita)
                        }
                        citasAdapter.updateData(citasList)
                    }
                }
        } else {
            Toast.makeText(context, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
        }
    }

}
