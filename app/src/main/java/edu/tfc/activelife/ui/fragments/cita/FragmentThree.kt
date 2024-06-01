package edu.tfc.activelife.ui.fragments.cita

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
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.hilt.android.AndroidEntryPoint
import edu.tfc.activelife.R
import edu.tfc.activelife.adapters.CitasAdapter
import edu.tfc.activelife.dao.Cita
import edu.tfc.activelife.ui.fragments.CitasViewModel
import edu.tfc.activelife.utils.Utils.formatFirebaseTimestamp
import edu.tfc.activelife.utils.Utils.isNetworkAvailable
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
            if (!isNetworkAvailable(requireContext())) {
                Toast.makeText(requireContext(), "No se puede crear una cita sin conexión a Internet.", Toast.LENGTH_SHORT).show()
            } else {
                findNavController().navigate(R.id.action_fragmentThree_to_fragmentCrearCita)
            }
        }

        // Configurar Firestore para modo offline
        val firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        val db = FirebaseFirestore.getInstance()
        db.firestoreSettings = firestoreSettings

        // Sincronizar datos de Firebase Database
        val databaseReference = FirebaseDatabase.getInstance().getReference("citas")
        databaseReference.keepSynced(true)

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
                    val tasks = mutableListOf<Task<*>>()

                    for (document in result) {
                        val citaId = document.id
                        val title = document.getString("titulo") ?: ""
                        val descripcion = document.getString("descripcion") ?: ""
                        val fechaTimestamp = document.getTimestamp("fechaCita")
                        val formattedDate = fechaTimestamp?.let { ts ->
                            formatFirebaseTimestamp(ts.seconds, ts.nanoseconds.toInt())
                        } ?: "Fecha no disponible"
                        val imageUrl = document.getString("image") ?: ""
                        val encargadoUuid = document.getString("encargadoUuid") ?: ""
                        val estado = document.getString("estado") ?: "espera"
                        var encargadoNombre = "Cargando..."

                        val cita = Cita(citaId, title, descripcion, formattedDate, imageUrl, encargadoNombre, estado)
                        citasList.add(cita)

                        val task = getEncargadoUsername(encargadoUuid)
                            .addOnSuccessListener { nombre ->
                                encargadoNombre = nombre
                                val index = citasList.indexOfFirst { it.id == citaId }
                                if (index >= 0) {
                                    val updatedCita = cita.copy(encargado = encargadoNombre)
                                    citasList[index] = updatedCita
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        citasAdapter.updateItem(index, updatedCita)
                                    }
                                }
                            }
                        tasks.add(task)
                    }

                    // Esperar a que todas las tareas de actualización del nombre del encargado terminen antes de actualizar la lista
                    com.google.android.gms.tasks.Tasks.whenAllComplete(tasks).addOnCompleteListener {
                        citasAdapter.updateData(citasList)

                        // Sincronizar con SQLite
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                viewModel.syncCitasWithSQLite(citasList)
                            } catch (e: Exception) {
                                Log.e("FragmentThree", "Error syncing with SQLite: ${e.message}", e)
                            }
                        }
                    }
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
                        val tasks = mutableListOf<Task<*>>()

                        for (document in snapshot) {
                            val citaId = document.id
                            val title = document.getString("titulo") ?: ""
                            val descripcion = document.getString("descripcion") ?: ""
                            val fechaTimestamp = document.getTimestamp("fechaCita")
                            val formattedDate = fechaTimestamp?.let { ts ->
                                formatFirebaseTimestamp(ts.seconds, ts.nanoseconds.toInt())
                            } ?: "Fecha no disponible"
                            val imageUrl = document.getString("image") ?: ""
                            val encargadoUuid = document.getString("encargadoUuid") ?: ""
                            val estado = document.getString("estado") ?: "espera"
                            var encargadoNombre = "Cargando..."

                            val cita = Cita(citaId, title, descripcion, formattedDate, imageUrl, encargadoNombre, estado)
                            citasList.add(cita)

                            val task = getEncargadoUsername(encargadoUuid)
                                .addOnSuccessListener { nombre ->
                                    encargadoNombre = nombre
                                    val index = citasList.indexOfFirst { it.id == citaId }
                                    if (index >= 0) {
                                        val updatedCita = cita.copy(encargado = encargadoNombre)
                                        citasList[index] = updatedCita
                                        lifecycleScope.launch(Dispatchers.Main) {
                                            citasAdapter.updateItem(index, updatedCita)
                                        }
                                    }
                                }
                            tasks.add(task)
                        }

                        com.google.android.gms.tasks.Tasks.whenAllComplete(tasks).addOnCompleteListener {
                            citasAdapter.updateData(citasList)

                            // Sincronizar con SQLite
                            lifecycleScope.launch(Dispatchers.IO) {
                                try {
                                    viewModel.syncCitasWithSQLite(citasList)
                                } catch (e: Exception) {
                                    Log.e("FragmentThree", "Error syncing with SQLite: ${e.message}", e)
                                }
                            }
                        }
                    }
                }
        } else {
            Toast.makeText(context, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getEncargadoUsername(encargadoUuid: String): Task<String> {
        val db = FirebaseFirestore.getInstance()
        val taskCompletionSource = TaskCompletionSource<String>()

        if (encargadoUuid.isNotEmpty()) {
            db.collection("users")
                .whereEqualTo("uuid", encargadoUuid)
                .get()
                .addOnSuccessListener { encargadoDocs ->
                    if (!encargadoDocs.isEmpty) {
                        val encargadoDoc = encargadoDocs.documents[0]
                        val nombre = encargadoDoc.getString("username") ?: "Nombre no disponible"
                        taskCompletionSource.setResult(nombre)
                    } else {
                        taskCompletionSource.setResult("Sin encargado")
                    }
                }
                .addOnFailureListener {
                    taskCompletionSource.setResult("Sin encargado")
                }
        } else {
            taskCompletionSource.setResult("Sin encargado")
        }

        return taskCompletionSource.task
    }
}
