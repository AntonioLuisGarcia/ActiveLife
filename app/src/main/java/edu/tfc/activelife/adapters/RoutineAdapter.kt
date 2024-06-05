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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.tfc.activelife.R
import edu.tfc.activelife.dao.Routine
import edu.tfc.activelife.ui.fragments.routine.FragmentTwoDirections
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RoutineAdapter(private var routineList: MutableList<Routine>, private val context: Context, var showPublicRoutines: Boolean) : RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_routine, parent, false)
        return RoutineViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val currentRoutine = routineList[position]
        holder.bind(currentRoutine, position)
    }

    override fun getItemCount(): Int = routineList.size

    fun setRoutineList(list: List<Routine>) {
        routineList.clear()
        routineList.addAll(list)
        notifyDataSetChanged()
    }

    inner class RoutineViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewRoutineTitle)
        val dayTextView: TextView = itemView.findViewById(R.id.textViewRoutineDay)
        val exercisesRecyclerView: RecyclerView = itemView.findViewById(R.id.recyclerViewExercises)
        val btnEditRoutine: Button = itemView.findViewById(R.id.btn_edit_routine)
        val btnDeleteRoutine: Button = itemView.findViewById(R.id.btn_delete_routine)
        val btnCopyRoutine: Button = itemView.findViewById(R.id.btn_copy_routine)
        val switchActive: Switch = itemView.findViewById(R.id.switch_active)
        val exerciseAdapter: ExerciseAdapter = ExerciseAdapter(emptyList())

        init {
            exercisesRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            exercisesRecyclerView.adapter = exerciseAdapter
        }

        fun bind(routine: Routine, position: Int) {
            titleTextView.text = routine.title
            dayTextView.text = routine.day

            switchActive.setOnCheckedChangeListener(null)
            switchActive.isChecked = routine.activo
            switchActive.setOnCheckedChangeListener { _, isChecked ->
                updateRoutineStatus(routine.id, isChecked) { success ->
                    if (success) {
                        routine.activo = isChecked
                        notifyItemChanged(position)
                    } else {
                        switchActive.isChecked = !isChecked
                    }
                }
            }

            exerciseAdapter.setExerciseList(routine.exercises)

            if (showPublicRoutines) {
                btnEditRoutine.visibility = View.GONE
                btnDeleteRoutine.visibility = View.GONE
                switchActive.visibility = View.GONE
                btnCopyRoutine.visibility = View.VISIBLE
                btnCopyRoutine.setOnClickListener {
                    showCopyConfirmationDialog(routine)
                }
            } else {
                btnEditRoutine.visibility = View.VISIBLE
                btnDeleteRoutine.visibility = View.VISIBLE
                switchActive.visibility = View.VISIBLE
                btnCopyRoutine.visibility = View.GONE
            }

            btnEditRoutine.setOnClickListener {
                val action = FragmentTwoDirections.actionFragmentTwoToFragmentOne(routine.id, routine.activo)
                itemView.findNavController().navigate(action)
            }

            btnDeleteRoutine.setOnClickListener {
                showDeleteConfirmationDialog(routine.id, position)
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

        private fun showCopyConfirmationDialog(routine: Routine) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Confirmación")
            builder.setMessage("¿Estás seguro que deseas copiar esta rutina?")
            builder.setPositiveButton("Sí") { _, _ ->
                copyRoutineToUser(routine)
            }
            builder.setNegativeButton("Cancelar", null)
            builder.show()
        }

        private fun copyRoutineToUser(routine: Routine) {
            val userUuid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val currentDayOfWeek = getCurrentDayOfWeek()

            val exercisesList = routine.exercises.map { exercise ->
                hashMapOf(
                    "id" to exercise.uuid,
                    "name" to exercise.exerciseName,
                    "description" to exercise.description,
                    "gifUrl" to exercise.gifUrl,
                    "serie" to exercise.series,
                    "repeticiones" to exercise.repetitions,
                )
            }

            val routineData = hashMapOf(
                "title" to routine.title,
                "day" to currentDayOfWeek, // Asignar día de la semana actual
                "exercises" to exercisesList,
                "userUUID" to userUuid,
                "activo" to false // Establecer activo a false por defecto
            )

            db.collection("rutinas").add(routineData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Rutina copiada exitosamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al copiar rutina", Toast.LENGTH_SHORT).show()
                }
        }

        private fun getCurrentDayOfWeek(): String {
            val calendar = Calendar.getInstance()
            return when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "Lunes"
                Calendar.TUESDAY -> "Martes"
                Calendar.WEDNESDAY -> "Miércoles"
                Calendar.THURSDAY -> "Jueves"
                Calendar.FRIDAY -> "Viernes"
                Calendar.SATURDAY -> "Sábado"
                Calendar.SUNDAY -> "Domingo"
                else -> "Lunes" // Por defecto si ocurre algún error
            }
        }
    }
}