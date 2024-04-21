package edu.tfc.activelife.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import edu.tfc.activelife.R
import edu.tfc.activelife.adapters.CitasAdapter
import edu.tfc.activelife.dao.Cita

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
        db.collection("citas")
            .get()
            .addOnSuccessListener { result ->
                val citasList = mutableListOf<Cita>()
                for (document in result) {
                    // Obtener el UUID de la cita del documento Firestore
                    val citaId = document.id
                    val title = document.getString("titulo")?: ""
                    val descripcion = document.getString("descripcion") ?: ""
                    val fechaTimestamp = document.getTimestamp("fechaCita")
                    val fecha = fechaTimestamp.toString()
                    val imageUrl = document.getString("image") ?: ""
                    val cita = Cita(citaId, title, descripcion, fecha, imageUrl)


                    // Agregar la cita a la lista
                    citasList.add(cita)
                }
                // Actualizar el adaptador con la lista de citas
                citasAdapter.updateData(citasList)
            }
            .addOnFailureListener { exception ->
                // Manejar el error
            }

        // Escuchar los cambios en la colección de citas
        db.collection("citas")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Manejar el error
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // La lista de citas en Firebase ha cambiado
                    // Actualizar la lista en el adaptador
                    val citasList = mutableListOf<Cita>()
                    for (document in snapshot) {
                        // Obtener los detalles de cada cita y agregarla a la lista
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
    }

}