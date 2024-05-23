package edu.tfc.activelife.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.tfc.activelife.R
import edu.tfc.activelife.dao.BaseExercise
import edu.tfc.activelife.dao.PublicExercise

class ExerciseAdapter(private var exercises: List<BaseExercise>) : RecyclerView.Adapter<ExerciseAdapter.DetailedViewHolder>() {

    inner class DetailedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exerciseNameTextView: TextView = itemView.findViewById(R.id.textViewDetailedExerciseName)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDetailedExerciseDescription)
        val seriesTextView: TextView = itemView.findViewById(R.id.textViewDetailedExerciseSeries)
        val repetitionsTextView: TextView = itemView.findViewById(R.id.textViewDetailedExerciseRepetitions)
        val gifImageView: ImageView = itemView.findViewById(R.id.imageViewExerciseGif)
        val musclesTextView: TextView = itemView.findViewById(R.id.textViewDetailedExerciseMuscles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailedViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_exercise_detailed, parent, false)
        return DetailedViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailedViewHolder, position: Int) {
        val exercise = exercises[position]
        if (exercise is PublicExercise) {
            holder.exerciseNameTextView.text = exercise.exerciseName
            holder.descriptionTextView.text = exercise.description
            holder.seriesTextView.text = "Series: ${exercise.series}"
            holder.repetitionsTextView.text = "Repetitions: ${exercise.repetitions}"
            holder.musclesTextView.text = "Targeted muscles: ${exercise.target}"
            Glide.with(holder.itemView.context)
                .load(exercise.gifUrl)
                .into(holder.gifImageView)
        }
    }

    override fun getItemCount(): Int = exercises.size

    fun setExerciseList(exercises: List<BaseExercise>) {
        this.exercises = exercises
        notifyDataSetChanged()
    }
}
