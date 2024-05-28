package edu.tfc.activelife.ui.fragments.routine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
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
import edu.tfc.activelife.dao.PublicExercise
import android.app.AlertDialog
import android.widget.Toast
import edu.tfc.activelife.api.ExerciseRepository
import edu.tfc.activelife.utils.Utils.isNetworkAvailable

class FragmentTwo : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RoutineAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var userUuid: String
    private lateinit var btnCreateRoutine: Button
    private var routinesListener: ListenerRegistration? = null
    private lateinit var switchToggleRoutines: Switch
    private var showPublicRoutines: Boolean = false
    private lateinit var repository: ExerciseRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_two, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RoutineAdapter(mutableListOf(), requireContext())
        recyclerView.adapter = adapter

        // Configurar Firestore para modo offline
        val firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db = FirebaseFirestore.getInstance()
        db.firestoreSettings = firestoreSettings

        userUuid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        switchToggleRoutines = view.findViewById(R.id.switch_toggle_routines)
        switchToggleRoutines.setOnCheckedChangeListener { _, isChecked ->
            showPublicRoutines = isChecked
            loadRoutines(showPublicRoutines)
        }

        btnCreateRoutine = view.findViewById(R.id.btn_go_to_fragment_one)
        btnCreateRoutine.setOnClickListener {
            if (!isNetworkAvailable(requireContext())) {
                Toast.makeText(requireContext(), "En estos momentos no se permite crear rutinas sin conexión.", Toast.LENGTH_SHORT).show()
            } else {
                showCreateRoutineDialog()
            }
        }

        repository = ExerciseRepository.getInstance()

        loadRoutines(showPublicRoutines)

        // Configurar sincronización offline para la colección "rutinas"
        val databaseReference = FirebaseDatabase.getInstance().getReference("rutinas")
        databaseReference.keepSynced(true)

        return view
    }

    private fun showCreateRoutineDialog() {
        val options = arrayOf("Crear desde cero", "Usar ejercicios predefinidos")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Crear Rutina")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    val action = FragmentTwoDirections.actionFragmentTwoToFragmentOne("")
                    findNavController().navigate(action)
                }
                1 -> {
                    repository.fetchExercises()
                    Toast.makeText(requireContext(), "Funcionalidad aún no implementada", Toast.LENGTH_SHORT).show()
                    val action = FragmentTwoDirections.actionFragmentTwoToCrearRutinaPredefinidaFragment()
                    findNavController().navigate(action)
                }
            }
        }
        builder.show()
    }

    private fun loadRoutines(loadPublic: Boolean = false) {
        routinesListener?.remove()
        val query = if (loadPublic) {
            db.collection("rutinas").whereEqualTo("public", true)
        } else {
            db.collection("rutinas").whereEqualTo("userUuid", userUuid)
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
                        userUUID = exerciseData["userUuid"] as? String ?: ""
                    )
                }
                val activo = document.getBoolean("activo") ?: false
                val day = document.getString("day") ?: ""
                val routine = Routine(routineId, title, exercisesList,"", activo, day)
                routineList.add(routine)
            }
            adapter.setRoutineList(routineList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        routinesListener?.remove()
    }
}
