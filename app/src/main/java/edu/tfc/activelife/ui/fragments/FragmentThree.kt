package edu.tfc.activelife.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import edu.tfc.activelife.R
import edu.tfc.activelife.adapters.CitasAdapter
import edu.tfc.activelife.dao.Cita
import edu.tfc.activelife.dao.CitaEntity
import edu.tfc.activelife.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentThree : Fragment() {

    private lateinit var recyclerViewCitas: RecyclerView
    private lateinit var citasAdapter: CitasAdapter
    private lateinit var btnAddCita: Button

    private val viewModel: CitasViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_three, container, false)

        recyclerViewCitas = view.findViewById(R.id.recycler_view_citas)
        recyclerViewCitas.layoutManager = LinearLayoutManager(requireContext())
        citasAdapter = CitasAdapter(emptyList(), requireContext())
        recyclerViewCitas.adapter = citasAdapter

        btnAddCita = view.findViewById(R.id.btn_add_cita)
        btnAddCita.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentThree_to_fragmentCrearCita)
        }

        fetchCitasFromFirestore()

        return view
    }

    private fun fetchCitasFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userUuid = currentUser.uid
            db.collection("citas")
                .whereEqualTo("userUuid", userUuid)
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
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                Log.d("FragmentThree", "Inserting cita: $citaId")
                                viewModel.addCita(CitaEntity(citaId, title, descripcion, formattedDate, imageUrl))
                                Log.d("FragmentThree", "Inserted cita: $citaId")
                            } catch (e: Exception) {
                                Log.e("FragmentThree", "Error inserting cita: $citaId, Exception: ${e.message}", e)
                            }
                        }
                    }
                    citasAdapter.updateData(citasList)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error al cargar citas: ${exception.message}", Toast.LENGTH_SHORT).show()
                }

            db.collection("citas")
                .whereEqualTo("userUuid", userUuid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("FragmentThree", "Snapshot listener error: ${e.message}", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val citasList = mutableListOf<Cita>()
                        for (document in snapshot) {
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
                            lifecycleScope.launch(Dispatchers.IO) {
                                try {
                                    Log.d("FragmentThree", "Inserting cita from snapshot: $citaId")
                                    viewModel.addCita(CitaEntity(citaId, title, descripcion, formattedDate, imageUrl))
                                    Log.d("FragmentThree", "Inserted cita from snapshot: $citaId")
                                } catch (e: Exception) {
                                    Log.e("FragmentThree", "Error inserting cita from snapshot: $citaId, Exception: ${e.message}", e)
                                }
                            }
                        }
                        citasAdapter.updateData(citasList)
                    }
                }
        } else {
            Toast.makeText(context, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
        }
    }

}
