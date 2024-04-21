package edu.tfc.activelife.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import edu.tfc.activelife.R
import edu.tfc.activelife.dao.Routine

class RoutineAdapter : RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder>() {

    private var routineList = emptyList<Routine>()

    fun setRoutineList(list: List<Routine>) {
        routineList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_routine, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val currentRoutine = routineList[position]
        holder.bind(currentRoutine)

        holder.btnDeleteRoutine.setOnClickListener {
            // Mostrar la ventana emergente de confirmación
            showDeleteConfirmationDialog(holder.itemView.context, currentRoutine.id)
        }
    }

    override fun getItemCount(): Int {
        return routineList.size
    }

    inner class RoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewRoutineTitle)
        private val exercisesRecyclerView: RecyclerView = itemView.findViewById(R.id.recyclerViewExercises)
        val btnEditRoutine: Button = itemView.findViewById(R.id.btn_edit_routine)
        val btnDeleteRoutine: Button = itemView.findViewById(R.id.btn_delete_routine)

        private val exerciseAdapter = ExerciseAdapter(emptyList())

        init {
            exercisesRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            exercisesRecyclerView.adapter = exerciseAdapter
        }

        fun bind(routine: Routine) {
            titleTextView.text = routine.title
            exerciseAdapter.setExercises(routine.exercises)
        }
    }

    private fun showDeleteConfirmationDialog(context: Context, routineId: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmación")
        builder.setMessage("¿Estás seguro que deseas borrar esta rutina?")

        builder.setPositiveButton("Sí") { _, _ ->
            // Eliminar la rutina de la base de datos
            deleteFromFirestore(context, routineId)
        }

        builder.setNegativeButton("Cancelar") { _, _ -> }

        builder.show()
    }

    // Método para eliminar la rutina de la base de datos
    private fun deleteFromFirestore(context: Context, routineId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("rutinas").document(routineId)
            .delete()
            .addOnSuccessListener {
                // La rutina se eliminó correctamente
                // Puedes actualizar la lista de rutinas si es necesario
                Toast.makeText(context, "Eliminada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Manejar el error
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            }
    }

}