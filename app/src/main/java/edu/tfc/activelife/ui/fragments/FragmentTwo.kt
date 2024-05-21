package edu.tfc.activelife.ui.fragments

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import edu.tfc.activelife.R
import edu.tfc.activelife.adapters.RoutineAdapter
import edu.tfc.activelife.dao.Routine
import edu.tfc.activelife.dao.Exercise
import edu.tfc.activelife.dao.PublicExercise

class FragmentTwo : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RoutineAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var userUuid: String
    private lateinit var btnCreateRoutine: Button
    private var routinesListener: ListenerRegistration? = null
    private lateinit var switchToggleRoutines: Switch
    private var showPublicRoutines: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_two, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RoutineAdapter(mutableListOf(), requireContext())
        recyclerView.adapter = adapter

        db = FirebaseFirestore.getInstance()
        userUuid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        switchToggleRoutines = view.findViewById(R.id.switch_toggle_routines)
        switchToggleRoutines.setOnCheckedChangeListener { _, isChecked ->
            showPublicRoutines = isChecked
            loadRoutines(showPublicRoutines)
        }

        btnCreateRoutine = view.findViewById(R.id.btn_go_to_fragment_one)
        btnCreateRoutine.setOnClickListener {
            val action = FragmentTwoDirections.actionFragmentTwoToFragmentOne("")
            findNavController().navigate(action)
        }

        loadRoutines(showPublicRoutines)

        return view
    }

    private fun loadRoutines(loadPublic: Boolean = false) {
        routinesListener?.remove()  // Detiene la escucha de cualquier consulta anterior
        val query = if (loadPublic) {
            db.collection("rutinas").whereEqualTo("public", true)
        } else {
            db.collection("rutinas").whereEqualTo("userUuid", userUuid)
        }

        routinesListener = query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Considera mostrar un mensaje de error o realizar alguna acción de recuperación
                return@addSnapshotListener
            }

            val routineList = mutableListOf<Routine>()
            snapshot?.documents?.forEach { document ->
                val routineId = document.id
                val title = document.getString("title") ?: ""
                val exercisesData = document.get("exercises") as? List<HashMap<String, Any>> ?: emptyList()
                val exercisesList = if (loadPublic) {
                    exercisesData.map { exerciseData ->
                        PublicExercise(
                            uuid = exerciseData["id"] as? String ?: "",
                            exerciseName = exerciseData["exerciseName"] as? String ?: "",
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
                    }.toMutableList()
                } else {
                    exercisesData.map { exerciseData ->
                        Exercise(
                            uuid = exerciseData["id"] as? String ?: "",
                            exerciseName = exerciseData["exerciseName"] as? String ?: "",
                            series = exerciseData["series"] as? String ?: "",
                            repetitions = exerciseData["repetitions"] as? String ?: "",
                            gifUrl = exerciseData["gifUrl"] as? String ?: ""
                        )
                    }.toMutableList()
                }
                val routine = Routine(routineId, title, exercisesList)
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
