package edu.tfc.activelife

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.tfc.activelife.dao.Exercise

class ExerciseAdapter(private var exercises: List<Exercise>) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewExerciseDescription)
        val seriesTextView: TextView = itemView.findViewById(R.id.textViewExerciseSeries)
        val repetitionsTextView: TextView = itemView.findViewById(R.id.textViewExerciseRepetitions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.descriptionTextView.text = exercise.exerciseName
        holder.seriesTextView.text = "Series: ${exercise.series}"
        holder.repetitionsTextView.text = "Repetitions: ${exercise.repetitions}"
    }

    override fun getItemCount(): Int {
        return exercises.size
    }

    fun setExercises(exercises: List<Exercise>) {
        this.exercises = exercises
        notifyDataSetChanged()
    }
}
