package edu.tfc.activelife.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import edu.tfc.activelife.R
import edu.tfc.activelife.dao.Routine
import edu.tfc.activelife.ui.fragments.FragmentTwoDirections

class RoutineAdapter(private var routineList: MutableList<Routine>, private val context: Context) : RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_routine, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val currentRoutine = routineList[position]
        holder.titleTextView.text = currentRoutine.title
        holder.dayTextView.text = currentRoutine.day

        holder.switchActive.setOnCheckedChangeListener(null)  // Remove the listener before setting the checked state to prevent unwanted callbacks
        holder.switchActive.isChecked = currentRoutine.active
        holder.switchActive.setOnCheckedChangeListener { _, isChecked ->
            holder.switchActive.isEnabled = false
            updateRoutineStatus(currentRoutine.id, isChecked) {
                currentRoutine.active = isChecked
                holder.switchActive.isEnabled = true
                holder.switchActive.isChecked = isChecked // Ensures the UI reflects the actual state
            }
        }

        // Configurar el RecyclerView de ejercicios dentro de cada rutina
        holder.exerciseAdapter.setExerciseList(currentRoutine.exercises)

        holder.btnEditRoutine.setOnClickListener {
            // Usar los argumentos para pasar el ID y active de la rutina al fragmento
            val action = FragmentTwoDirections.actionFragmentTwoToFragmentOne(currentRoutine.id, currentRoutine.active)
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
        db.collection("rutinas").document(routineId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Rutina eliminada", Toast.LENGTH_SHORT).show()
                //routineList.removeAt(position)
                //notifyItemRemoved(position)
                //notifyItemRangeChanged(position, routineList.size)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al eliminar rutina", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateRoutineStatus(routineId: String, isActive: Boolean, onComplete: (Boolean) -> Unit) {
        db.collection("rutinas").document(routineId)
            .update("activo", isActive)
            .addOnSuccessListener {
                Toast.makeText(context, "Estado de la rutina actualizado", Toast.LENGTH_SHORT).show()
                onComplete(true)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al actualizar estado", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
    }

    override fun getItemCount(): Int = routineList.size

    fun setRoutineList(list: List<Routine>) {
        routineList.clear()
        routineList.addAll(list)
        notifyDataSetChanged()
    }

    class RoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewRoutineTitle)
        val dayTextView: TextView = itemView.findViewById(R.id.textViewRoutineDay)
        val exercisesRecyclerView: RecyclerView = itemView.findViewById(R.id.recyclerViewExercises)
        val btnEditRoutine: Button = itemView.findViewById(R.id.btn_edit_routine)
        val btnDeleteRoutine: Button = itemView.findViewById(R.id.btn_delete_routine)
        val switchActive: Switch = itemView.findViewById(R.id.switch_active)
        val exerciseAdapter: ExerciseAdapter = ExerciseAdapter(emptyList())


        init {
            exercisesRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            exercisesRecyclerView.adapter = exerciseAdapter
        }
    }
}
