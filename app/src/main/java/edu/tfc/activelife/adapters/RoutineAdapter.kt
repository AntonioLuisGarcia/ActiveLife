package edu.tfc.activelife.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import edu.tfc.activelife.R
import edu.tfc.activelife.dao.Routine
import edu.tfc.activelife.ui.fragments.FragmentTwoDirections

class RoutineAdapter(private var routineList: List<Routine>, private val context: Context) : RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_routine, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val currentRoutine = routineList[position]
        holder.titleTextView.text = currentRoutine.title
        // Configurar el RecyclerView de ejercicios dentro de cada rutina
        holder.exerciseAdapter.setExerciseList(currentRoutine.exercises)

        holder.btnEditRoutine.setOnClickListener {
            // Usar los argumentos para pasar el ID de la rutina al fragmento
            val action = FragmentTwoDirections.actionFragmentTwoToFragmentOne(currentRoutine.id)
            holder.itemView.findNavController().navigate(action)
        }

        holder.btnDeleteRoutine.setOnClickListener {
            showDeleteConfirmationDialog(currentRoutine.id, position)
        }
    }



    private fun showDeleteConfirmationDialog(routineId: String, position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmación")
        builder.setMessage("¿Estás seguro que deseas borrar esta rutina?")
        builder.setPositiveButton("Sí") { _, _ ->
            deleteFromFirestore(routineId, position)
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun deleteFromFirestore(routineId: String, position: Int) {
        val db = FirebaseFirestore.getInstance()
        db.collection("rutinas").document(routineId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Rutina eliminada", Toast.LENGTH_SHORT).show()
                // Eliminar la rutina de la lista y notificar al adaptador
                routineList = routineList.toMutableList().apply { removeAt(position) }
                notifyItemRemoved(position)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al eliminar rutina", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int = routineList.size

    fun setRoutineList(list: List<Routine>) {
        routineList = list
        notifyDataSetChanged()
    }

    class RoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewRoutineTitle)
        val exercisesRecyclerView: RecyclerView = itemView.findViewById(R.id.recyclerViewExercises)
        val btnEditRoutine: Button = itemView.findViewById(R.id.btn_edit_routine)
        val btnDeleteRoutine: Button = itemView.findViewById(R.id.btn_delete_routine)
        val exerciseAdapter: ExerciseAdapter = ExerciseAdapter(emptyList())

        init {
            exercisesRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            exercisesRecyclerView.adapter = exerciseAdapter
        }
    }
}