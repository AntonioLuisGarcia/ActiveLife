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
import edu.tfc.activelife.dao.Routine
import edu.tfc.activelife.adapters.RoutineAdapter
import edu.tfc.activelife.dao.Exercise

class FragmentTwo : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RoutineAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var userUuid: String
    private lateinit var btnGoToFragmentOne: Button
    private var routinesListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_two, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RoutineAdapter()
        recyclerView.adapter = adapter
        db = FirebaseFirestore.getInstance()
        userUuid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        loadRoutines()

        // Referencia al botón y asignación del listener para navegar al FragmentOne
        btnGoToFragmentOne = view.findViewById(R.id.btn_go_to_fragment_one)
        btnGoToFragmentOne.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentTwo_to_fragmentOne)
        }

        return view
    }

    private fun loadRoutines() {
        // Escuchar cambios en tiempo real en la colección de rutinas del usuario actual
        routinesListener = db.collection("rutinas")
            .whereEqualTo("userUuid", userUuid)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    // Manejar errores al cargar las rutinas
                    return@addSnapshotListener
                }

                val routineList = mutableListOf<Routine>()
                if (snapshot != null) {
                    for (document in snapshot) {
                        val routineId = document.id
                        val title = document.getString("title") ?: ""
                        val exercisesList = mutableListOf<Exercise>()

                        // Obtener la lista de ejercicios como una lista de HashMaps
                        val exercisesData = document.get("exercises") as? List<HashMap<String, Any>> ?: emptyList()

                        // Convertir cada HashMap en un objeto Exercise y agregarlo a la lista
                        for (exerciseData in exercisesData) {
                            val exerciseName = exerciseData["exerciseName"] as? String ?: ""
                            val series = exerciseData["series"] as? String ?: ""
                            val repetitions = exerciseData["repetitions"] as? String ?: ""
                            val exercise = Exercise(exerciseName, series, repetitions)
                            exercisesList.add(exercise)
                        }

                        // Crear el objeto Routine con la lista de ejercicios y agregarlo a la lista de rutinas
                        val routine = Routine(routineId, title, exercisesList)
                        routineList.add(routine)
                    }
                }
                adapter.setRoutineList(routineList)
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // Detener la escucha de cambios en la colección de rutinas al destruir la vista
        routinesListener?.remove()
    }
}

