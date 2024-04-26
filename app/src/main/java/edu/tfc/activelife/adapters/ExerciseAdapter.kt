package edu.tfc.activelife.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.tfc.activelife.R
import edu.tfc.activelife.dao.BaseExercise
import edu.tfc.activelife.dao.Exercise
import edu.tfc.activelife.dao.PublicExercise

class ExerciseAdapter(private var exercises: List<BaseExercise>) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewExerciseDescription)
        val seriesTextView: TextView = itemView.findViewById(R.id.textViewExerciseSeries)
        val repetitionsTextView: TextView = itemView.findViewById(R.id.textViewExerciseRepetitions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        when (exercise) {
            is Exercise -> {
                holder.descriptionTextView.text = exercise.description  // Use specific fields from Exercise
                holder.seriesTextView.text = "Series: ${exercise.series}"
                holder.repetitionsTextView.text = "Repetitions: ${exercise.repetitions}"
            }
            is PublicExercise -> {
                // Handle PublicExercise specific fields if necessary
                holder.descriptionTextView.text = exercise.description
                holder.seriesTextView.text = "Series: ${exercise.series}"  // Note serie vs series
                holder.repetitionsTextView.text = "Repetitions: ${exercise.repetitions}"
            }
        }
    }


    override fun getItemCount(): Int = exercises.size

    fun setExerciseList(exercises: List<Any>) {
        this.exercises = exercises as List<BaseExercise>
        notifyDataSetChanged()
    }
}
