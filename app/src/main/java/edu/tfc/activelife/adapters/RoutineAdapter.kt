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
import edu.tfc.activelife.utils.Utils

/**
 * Adapter for displaying a list of routines in a RecyclerView.
 *
 * @param routineList The list of routines to display.
 * @param context The context of the parent activity.
 * @param showPublicRoutines A boolean indicating whether to show public routines or not.
 */
class RoutineAdapter(
    private var routineList: MutableList<Routine>,
    private val context: Context,
    var showPublicRoutines: Boolean
) : RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    /**
     * Creates a new ViewHolder for a routine item.
     *
     * @param parent The parent ViewGroup.
     * @param viewType The type of the new view.
     * @return A new RoutineViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_routine, parent, false)
        return RoutineViewHolder(view, context)
    }

    /**
     * Binds the data to the ViewHolder for the specified position.
     *
     * @param holder The ViewHolder to bind the data to.
     * @param position The position of the item in the data set.
     */
    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val currentRoutine = routineList[position]
        holder.bind(currentRoutine, position)
    }

    /**
     * Returns the total number of items in the data set.
     *
     * @return The size of the routine list.
     */
    override fun getItemCount(): Int = routineList.size

    /**
     * Updates the routine list with new data.
     *
     * @param list The new list of routines.
     */
    fun setRoutineList(list: List<Routine>) {
        routineList.clear()
        routineList.addAll(list)
        notifyDataSetChanged()
    }

    /**
     * ViewHolder for routine items.
     *
     * @param itemView The view of the item.
     * @param context The context of the parent activity.
     */
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

        /**
         * Binds a routine to the ViewHolder.
         *
         * @param routine The routine to bind.
         * @param position The position of the item in the data set.
         */
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

        /**
         * Shows a confirmation dialog to delete a routine.
         *
         * @param routineId The ID of the routine to delete.
         * @param position The position of the routine in the list.
         */
        private fun showDeleteConfirmationDialog(routineId: String, position: Int) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Confirmation")
            builder.setMessage("Are you sure you want to delete this routine?")
            builder.setPositiveButton("Yes") { _, _ ->
                deleteFromFirestore(routineId, position)
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
        }

        /**
         * Deletes a routine from Firestore.
         *
         * @param routineId The ID of the routine to delete.
         * @param position The position of the routine in the list.
         */
        private fun deleteFromFirestore(routineId: String, position: Int) {
            db.collection("rutinas").document(routineId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Routine deleted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error deleting routine", Toast.LENGTH_SHORT).show()
                }
        }

        /**
         * Updates the status of a routine in Firestore.
         *
         * @param routineId The ID of the routine to update.
         * @param isActive The new active status of the routine.
         * @param onComplete Callback function to handle completion.
         */
        private fun updateRoutineStatus(routineId: String, isActive: Boolean, onComplete: (Boolean) -> Unit) {
            db.collection("rutinas").document(routineId)
                .update("activo", isActive)
                .addOnSuccessListener {
                    Toast.makeText(context, "Routine status updated", Toast.LENGTH_SHORT).show()
                    onComplete(true)
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error updating status", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
        }

        /**
         * Shows a confirmation dialog to copy a routine.
         *
         * @param routine The routine to copy.
         */
        private fun showCopyConfirmationDialog(routine: Routine) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Confirmation")
            builder.setMessage("Are you sure you want to copy this routine?")
            builder.setPositiveButton("Yes") { _, _ ->
                copyRoutineToUser(routine)
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
        }

        /**
         * Copies a routine to the current user.
         *
         * @param routine The routine to copy.
         */
        private fun copyRoutineToUser(routine: Routine) {
            val userUuid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val currentDayOfWeek = Utils.getCurrentDayOfWeek()

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
                "day" to currentDayOfWeek,
                "exercises" to exercisesList,
                "userUUID" to userUuid,
                "activo" to false
            )

            db.collection("rutinas").add(routineData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Routine copied successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error copying routine", Toast.LENGTH_SHORT).show()
                }
        }
    }
}