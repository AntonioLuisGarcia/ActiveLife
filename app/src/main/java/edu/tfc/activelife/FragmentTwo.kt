package edu.tfc.activelife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.tfc.activelife.dao.Routine
import edu.tfc.activelife.routine.RoutineAdapter

class FragmentTwo : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RoutineAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String

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
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        loadRoutines()
        return view
    }

    private fun loadRoutines() {
        // Consultar las rutinas del usuario actual desde Firebase
        db.collection("rutinas")
            .whereEqualTo("userUuid", userId)
            .get()
            .addOnSuccessListener { documents ->
                val routineList = mutableListOf<Routine>()
                for (document in documents) {
                    val routine = document.toObject(Routine::class.java)
                    routineList.add(routine)
                }
                adapter.setRoutineList(routineList)
            }
            .addOnFailureListener { exception ->
                // Manejar errores al cargar las rutinas
            }
    }
}
