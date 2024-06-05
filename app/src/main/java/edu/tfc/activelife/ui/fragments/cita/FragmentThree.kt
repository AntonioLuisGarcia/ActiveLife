package edu.tfc.activelife.ui.fragments.cita

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
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

    private lateinit var spinnerSort: Spinner
    private lateinit var btnFilterWaiting: Button
    private lateinit var btnFilterDenied: Button
    private lateinit var btnFilterAccepted: Button

    private val viewModel: CitasViewModel by viewModels()
    private var citasList: List<Cita> = listOf()
    private val activeFilters = mutableSetOf<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_three, container, false)

        recyclerViewCitas = view.findViewById(R.id.recycler_view_citas)
        recyclerViewCitas.layoutManager = LinearLayoutManager(requireContext())
        citasAdapter = CitasAdapter(emptyList(), requireContext())
        recyclerViewCitas.adapter = citasAdapter

        btnAddCita = view.findViewById(R.id.btn_add_cita)
        spinnerSort = view.findViewById(R.id.spinner_sort)
        btnFilterWaiting = view.findViewById(R.id.btn_filter_waiting)
        btnFilterDenied = view.findViewById(R.id.btn_filter_denied)
        btnFilterAccepted = view.findViewById(R.id.btn_filter_accepted)

        btnAddCita.setOnClickListener {
            if (!isNetworkAvailable(requireContext())) {
                Toast.makeText(requireContext(), "No se puede crear una cita sin conexión a Internet.", Toast.LENGTH_SHORT).show()
            } else {
                findNavController().navigate(R.id.action_fragmentThree_to_fragmentCrearCita)
            }
        }

        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> sortCitas(true)
                    1 -> sortCitas(false)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnFilterWaiting.setOnClickListener { toggleFilter("espera", btnFilterWaiting) }
        btnFilterDenied.setOnClickListener { toggleFilter("denegado", btnFilterDenied) }
        btnFilterAccepted.setOnClickListener { toggleFilter("aceptado", btnFilterAccepted) }

        // Activar todos los filtros por defecto
        activateAllFilters()

        // Ordenar de forma ascendente por defecto
        spinnerSort.setSelection(0)

        val firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        val db = FirebaseFirestore.getInstance()
        db.firestoreSettings = firestoreSettings

        val databaseReference = FirebaseDatabase.getInstance().getReference("citas")
        databaseReference.keepSynced(true)

        fetchCitasFromFirestore()

        return view
    }

    private fun activateAllFilters() {
        activeFilters.add("espera")
        activeFilters.add("denegado")
        activeFilters.add("aceptado")

        updateButtonState(btnFilterWaiting, true)
        updateButtonState(btnFilterDenied, true)
        updateButtonState(btnFilterAccepted, true)

        applyFiltersAndSort(true)
    }

    private fun updateButtonState(button: Button, isActive: Boolean) {
        if (isActive) {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBackground))
        } else {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorBackground))
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        }
    }
    private fun fetchCitasFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userUuid = currentUser.uid
            db.collection("citas")
                .whereEqualTo("userUUID", userUuid)
                .get()
                .addOnSuccessListener { result ->
                    citasList = result.documents.mapNotNull { document ->
                        val citaId = document.id
                        val title = document.getString("titulo") ?: ""
                        val descripcion = document.getString("descripcion") ?: ""
                        val fechaTimestamp = document.getTimestamp("fechaCita")
                        val formattedDate = fechaTimestamp?.let { ts ->
                            formatFirebaseTimestamp(ts.seconds, ts.nanoseconds.toInt())
                        } ?: "Fecha no disponible"
                        val imageUrl = document.getString("imagen") ?: ""
                        val encargadoUuid = document.getString("encargadoUuid") ?: ""
                        val estado = document.getString("estado") ?: "espera"
                        val fileUrl = document.getString("fileUrl") ?: ""
                        val respuesta = document.getString("respuesta") ?: ""
                        var encargadoNombre = "Cargando..."

                        fechaTimestamp?.let {
                            Cita(citaId, title, descripcion, formattedDate, it, imageUrl, encargadoNombre, estado, fileUrl, respuesta).also { cita ->
                                getEncargadoUsername(encargadoUuid)
                                    .addOnSuccessListener { nombre ->
                                        cita.encargado = nombre
                                        updateCitaInAdapter(cita)
                                    }
                            }
                        }
                    }
                    applyFiltersAndSort(true)

                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            viewModel.syncCitasWithSQLite(citasList)
                        } catch (e: Exception) {
                            Log.e("FragmentThree", "Error syncing with SQLite: ${e.message}", e)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error al cargar citas: ${exception.message}", Toast.LENGTH_SHORT).show()
                }

            db.collection("citas")
                .whereEqualTo("userUUID", userUuid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("FragmentThree", "Snapshot listener error: ${e.message}", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        citasList = snapshot.documents.mapNotNull { document ->
                            val citaId = document.id
                            val title = document.getString("titulo") ?: ""
                            val descripcion = document.getString("descripcion") ?: ""
                            val fechaTimestamp = document.getTimestamp("fechaCita")
                            val formattedDate = fechaTimestamp?.let { ts ->
                                formatFirebaseTimestamp(ts.seconds, ts.nanoseconds.toInt())
                            } ?: "Fecha no disponible"
                            val imageUrl = document.getString("imagen") ?: ""
                            val encargadoUuid = document.getString("encargadoUuid") ?: ""
                            val estado = document.getString("estado") ?: "espera"
                            val fileUrl = document.getString("fileUrl") ?: ""
                            val respuesta = document.getString("respuesta") ?: ""
                            var encargadoNombre = "Cargando..."

                            fechaTimestamp?.let {
                                Cita(citaId, title, descripcion, formattedDate, it, imageUrl, encargadoNombre, estado, fileUrl, respuesta).also { cita ->
                                    getEncargadoUsername(encargadoUuid)
                                        .addOnSuccessListener { nombre ->
                                            cita.encargado = nombre
                                            updateCitaInAdapter(cita)
                                        }
                                }
                            }
                        }
                        applyFiltersAndSort(true)

                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                viewModel.syncCitasWithSQLite(citasList)
                            } catch (e: Exception) {
                                Log.e("FragmentThree", "Error syncing with SQLite: ${e.message}", e)
                            }
                        }
                    }
                }
        } else {
            Toast.makeText(context, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCitaInAdapter(updatedCita: Cita) {
        val index = citasList.indexOfFirst { it.id == updatedCita.id }
        if (index >= 0) {
            citasList = citasList.toMutableList().apply { set(index, updatedCita) }
            citasAdapter.updateItem(index, updatedCita)
        }
    }

    private fun sortCitas(ascendente: Boolean) {
        val sortedList = if (ascendente) {
            citasList.sortedBy { it.fecha }
        } else {
            citasList.sortedByDescending { it.fecha }
        }
        citasAdapter.updateData(sortedList)
    }

    private fun applyFiltersAndSort(ascendente: Boolean) {
        val filteredList = citasList.filter { it.estado in activeFilters }
        val sortedList = if (ascendente) {
            filteredList.sortedBy { it.fecha }
        } else {
            filteredList.sortedByDescending { it.fecha }
        }
        citasAdapter.updateData(sortedList)
    }


    private fun toggleFilter(estado: String, button: Button) {
        if (activeFilters.contains(estado)) {
            activeFilters.remove(estado)
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorBackground))
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        } else {
            activeFilters.add(estado)
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBackground))
        }
        applyFiltersAndSort(spinnerSort.selectedItemPosition == 0)
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