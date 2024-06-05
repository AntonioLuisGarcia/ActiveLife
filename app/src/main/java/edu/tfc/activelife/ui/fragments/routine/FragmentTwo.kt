package edu.tfc.activelife.ui.fragments.routine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import edu.tfc.activelife.R
import edu.tfc.activelife.adapters.RoutineAdapter
import edu.tfc.activelife.dao.Routine
import edu.tfc.activelife.api.ExerciseRepository
import edu.tfc.activelife.dao.PublicExercise
import edu.tfc.activelife.utils.Utils.isNetworkAvailable

class FragmentTwo : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RoutineAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var userUuid: String
    private lateinit var btnCreateRoutine: Button
    private lateinit var btnToggleRoutines: Button
    private lateinit var spinnerSort: Spinner
    private lateinit var btnFilterActive: Button
    private var routinesListener: ListenerRegistration? = null
    private var showPublicRoutines: Boolean = false
    private var showOnlyActive: Boolean = false
    private lateinit var repository: ExerciseRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_two, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RoutineAdapter(mutableListOf(), requireContext(), showPublicRoutines)
        recyclerView.adapter = adapter

        db = FirebaseFirestore.getInstance()
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        userUuid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        btnToggleRoutines = view.findViewById(R.id.btn_toggle_routines)
        btnToggleRoutines.setOnClickListener { toggleRoutines() }

        spinnerSort = view.findViewById(R.id.spinner_sort)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sort_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSort.adapter = adapter
        }

        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                loadRoutines(showPublicRoutines)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        btnFilterActive = view.findViewById(R.id.btn_filter_active)
        btnFilterActive.setOnClickListener { toggleActiveFilter() }

        btnCreateRoutine = view.findViewById(R.id.btn_go_to_fragment_one)
        btnCreateRoutine.setOnClickListener {
            if (!isNetworkAvailable(requireContext())) {
                Toast.makeText(requireContext(), "En estos momentos no se permite crear rutinas sin conexión.", Toast.LENGTH_SHORT).show()
            } else {
                val action = FragmentTwoDirections.actionFragmentTwoToFragmentOne("")
                findNavController().navigate(action)
            }
        }

        repository = ExerciseRepository.getInstance()

        loadRoutines(showPublicRoutines)

        val databaseReference = FirebaseDatabase.getInstance().getReference("rutinas")
        databaseReference.keepSynced(true)

        return view
    }

    private fun toggleRoutines() {
        showPublicRoutines = !showPublicRoutines
        if (showPublicRoutines) {
            showOnlyActive = false
            updateButtonState(btnFilterActive, showOnlyActive)
        }
        updateButtonState(btnToggleRoutines, showPublicRoutines)
        loadRoutines(showPublicRoutines)
    }

    private fun toggleActiveFilter() {
        showOnlyActive = !showOnlyActive
        if (showOnlyActive) {
            showPublicRoutines = false
            updateButtonState(btnToggleRoutines, showPublicRoutines)
        }
        updateButtonState(btnFilterActive, showOnlyActive)
        loadRoutines(showPublicRoutines)
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

    private fun loadRoutines(loadPublic: Boolean = false) {
        routinesListener?.remove()
        val query = if (loadPublic) {
            db.collection("rutinas").whereEqualTo("public", true)
        } else {
            db.collection("rutinas").whereEqualTo("userUUID", userUuid)
        }

        routinesListener = query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                return@addSnapshotListener
            }

            val routineList = mutableListOf<Routine>()
            snapshot?.documents?.forEach { document ->
                val routineId = document.id
                val title = document.getString("title") ?: ""
                val exercisesData = document.get("exercises") as? List<HashMap<String, Any>> ?: emptyList()
                val exercisesList = exercisesData.map { exerciseData ->
                    PublicExercise(
                        uuid = exerciseData["id"] as? String ?: "",
                        exerciseName = exerciseData["name"] as? String ?: "",
                        description = exerciseData["description"] as? String ?: "",
                        bodyPart = exerciseData["bodyPart"] as? String ?: "",
                        equipment = exerciseData["equipment"] as? String ?: "",
                        gifUrl = exerciseData["gifUrl"] as? String ?: "",
                        instructions = (exerciseData["instructions"] as? List<String> ?: listOf()),
                        series = exerciseData["serie"] as? String ?: "",
                        repetitions = exerciseData["repeticiones"] as? String ?: "",
                        target = exerciseData["target"] as? String ?: "",
                        secondaryMuscles = (exerciseData["secondaryMuscles"] as? List<String> ?: listOf()),
                        public = exerciseData["public"] as? Boolean ?: false,
                        title = exerciseData["title"] as? String ?: "",
                        userUUID = exerciseData["userUUID"] as? String ?: ""
                    )
                }
                val active = document.getBoolean("activo") ?: false
                val day = document.getString("day") ?: ""
                val routine = Routine(routineId, title, exercisesList, "", active, day)
                routineList.add(routine)
            }

            // Filtrar por activo si es necesario
            val filteredList = if (showOnlyActive) {
                routineList.filter { it.activo }
            } else {
                routineList
            }

            // Ordenar la lista de lunes a domingo
            val sortedList = when (spinnerSort.selectedItem.toString()) {
                "Día (Ascendente)" -> filteredList.sortedWith(compareBy {
                    when (it.day) {
                        "Lunes" -> 1
                        "Martes" -> 2
                        "Miércoles" -> 3
                        "Jueves" -> 4
                        "Viernes" -> 5
                        "Sábado" -> 6
                        "Domingo" -> 7
                        else -> 8
                    }
                })
                "Día (Descendente)" -> filteredList.sortedWith(compareByDescending {
                    when (it.day) {
                        "Lunes" -> 1
                        "Martes" -> 2
                        "Miércoles" -> 3
                        "Jueves" -> 4
                        "Viernes" -> 5
                        "Sábado" -> 6
                        "Domingo" -> 7
                        else -> 8
                    }
                })
                else -> filteredList
            }

            adapter.setRoutineList(sortedList)
            adapter.showPublicRoutines = loadPublic // Actualizar el estado de showPublicRoutines en el adaptador
            adapter.notifyDataSetChanged() // Notificar al adaptador que los datos han cambiado
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        routinesListener?.remove()
    }
}
