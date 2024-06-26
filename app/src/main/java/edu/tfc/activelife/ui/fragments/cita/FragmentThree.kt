package edu.tfc.activelife.ui.fragments.cita

import android.content.Context
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

/**
 * Fragment to manage and display user appointments. Allows filtering, sorting, and adding new appointments.
 */
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

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
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

        // Activate all filters by default
        activateAllFilters()

        // Sort ascending by default
        spinnerSort.setSelection(0)

        val firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        val db = FirebaseFirestore.getInstance()
        db.firestoreSettings = firestoreSettings

        val databaseReference = FirebaseDatabase.getInstance().getReference("citas")
        databaseReference.keepSynced(true)

        fetchCitasFromFirestore()
        applyBackgroundColor(view)
        return view
    }

    /**
     * Called immediately after onCreateView has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once they know their view hierarchy has been completely created.
     * @param view The View returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyBackgroundColor(view)
    }

    /**
     * Apply the background color to the view.
     * @param view The View to which the background color will be applied.
     */
    private fun applyBackgroundColor(view: View) {
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val colorResId = sharedPreferences.getInt("background_color", R.color.white)
        view.setBackgroundResource(colorResId)
    }

    /**
     * Activate all filters and update their state.
     */
    private fun activateAllFilters() {
        activeFilters.add("espera")
        activeFilters.add("denegado")
        activeFilters.add("aceptado")

        updateButtonState(btnFilterWaiting, true)
        updateButtonState(btnFilterDenied, true)
        updateButtonState(btnFilterAccepted, true)

        applyFiltersAndSort(true)
    }

    /**
     * Update the state of a button to reflect whether a filter is active.
     * @param button The Button to update.
     * @param isActive Boolean indicating whether the filter is active.
     */
    private fun updateButtonState(button: Button, isActive: Boolean) {
        if (isActive) {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBackground))
        } else {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorBackground))
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        }
    }

    /**
     * Fetch the appointments (citas) from Firestore and update the RecyclerView.
     */
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

    /**
     * Updates the specified appointment (cita) in the adapter.
     *
     * @param updatedCita The updated appointment to be reflected in the adapter.
     */
    private fun updateCitaInAdapter(updatedCita: Cita) {
        val index = citasList.indexOfFirst { it.id == updatedCita.id }
        if (index >= 0) {
            citasList = citasList.toMutableList().apply { set(index, updatedCita) }
            citasAdapter.updateItem(index, updatedCita)
        }
    }

    /**
     * Sorts the appointments by date in ascending or descending order.
     *
     * @param ascendente Boolean indicating whether to sort in ascending order (true) or descending order (false).
     */
    private fun sortCitas(ascendente: Boolean) {
        val sortedList = if (ascendente) {
            citasList.sortedBy { it.fecha }
        } else {
            citasList.sortedByDescending { it.fecha }
        }
        citasAdapter.updateData(sortedList)
    }

    /**
     * Applies the active filters and sorts the appointments accordingly.
     *
     * @param ascendente Boolean indicating whether to sort in ascending order (true) or descending order (false).
     */
    private fun applyFiltersAndSort(ascendente: Boolean) {
        val filteredList = citasList.filter { it.estado in activeFilters }
        val sortedList = if (ascendente) {
            filteredList.sortedBy { it.fecha }
        } else {
            filteredList.sortedByDescending { it.fecha }
        }
        citasAdapter.updateData(sortedList)
    }

    /**
     * Toggles the filter for the specified state and updates the button state accordingly.
     *
     * @param estado The state to be filtered.
     * @param button The Button corresponding to the filter state.
     */
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

    /**
     * Retrieves the username of the specified manager (encargado) from Firestore.
     *
     * @param encargadoUuid The UUID of the manager whose username is to be retrieved.
     * @return A Task that resolves to the manager's username.
     */
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