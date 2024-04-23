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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import edu.tfc.activelife.R
import edu.tfc.activelife.adapters.RoutineAdapter
import edu.tfc.activelife.dao.Routine
import edu.tfc.activelife.dao.Exercise

class FragmentTwo : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RoutineAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var userUuid: String
    private lateinit var btnCreateRoutine: Button  // Cambiado para reflejar su propósito
    private var routinesListener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_two, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RoutineAdapter(emptyList(), requireContext())
        recyclerView.adapter = adapter

        db = FirebaseFirestore.getInstance()
        userUuid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        loadRoutines()

        btnCreateRoutine = view.findViewById(R.id.btn_go_to_fragment_one)  // Asegúrate de que el ID en el layout está correcto
        btnCreateRoutine.setOnClickListener {
            // Navegación explícita para crear una nueva rutina
            val action = FragmentTwoDirections.actionFragmentTwoToFragmentOne("")
            findNavController().navigate(action)
        }

        return view
    }

    private fun loadRoutines() {
        routinesListener = db.collection("rutinas")
            .whereEqualTo("userUuid", userUuid)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }

                val routineList = mutableListOf<Routine>()
                if (snapshot != null) {
                    for (document in snapshot) {
                        val routineId = document.id
                        val title = document.getString("title") ?: ""
                        val exercisesList = mutableListOf<Exercise>()
                        val exercisesData = document.get("exercises") as? List<HashMap<String, Any>> ?: emptyList()
                        for (exerciseData in exercisesData) {
                            val exercise = Exercise("",
                                exerciseData["exerciseName"] as? String ?: "",
                                exerciseData["series"] as? String ?: "",
                                exerciseData["repetitions"] as? String ?: ""
                            )
                            exercisesList.add(exercise)
                        }
                        val routine = Routine(routineId, title, exercisesList)
                        routineList.add(routine)
                    }
                }
                adapter.setRoutineList(routineList)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        routinesListener?.remove()
    }
}
