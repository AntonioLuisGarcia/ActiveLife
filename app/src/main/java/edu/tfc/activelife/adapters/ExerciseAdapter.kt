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

class ExerciseAdapter(private var exercises: List<BaseExercise>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_SIMPLE = 0
        const val TYPE_DETAILED = 1
    }

    // Decide qué tipo de vista usar basado en la instancia del ejercicio
    override fun getItemViewType(position: Int): Int {
        return if (exercises[position] is PublicExercise) TYPE_DETAILED else TYPE_SIMPLE
    }

    inner class SimpleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewExerciseDescription)
        val repetitionsTextView: TextView = itemView.findViewById(R.id.textViewExerciseRepetitions)
    }

    inner class DetailedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDetailedExerciseDescription)
        val seriesTextView: TextView = itemView.findViewById(R.id.textViewDetailedExerciseSeries)
        val repetitionsTextView: TextView = itemView.findViewById(R.id.textViewDetailedExerciseRepetitions)
        val gifImageView: ImageView = itemView.findViewById(R.id.imageViewExerciseGif)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SIMPLE -> SimpleViewHolder(inflater.inflate(R.layout.item_exercise_simple, parent, false))
            TYPE_DETAILED -> DetailedViewHolder(inflater.inflate(R.layout.item_exercise_detailed, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val exercise = exercises[position]
        when (holder) {
            is SimpleViewHolder -> {
                holder.descriptionTextView.text = exercise.description
                holder.repetitionsTextView.text = "Repetitions: ${exercise.repetitions}"
            }
            is DetailedViewHolder -> {
                if (exercise is PublicExercise) {
                    holder.descriptionTextView.text = exercise.description
                    holder.seriesTextView.text = "Series: ${exercise.series}"
                    holder.repetitionsTextView.text = "Repetitions: ${exercise.repetitions}"
                    Glide.with(holder.itemView.context)
                        .load(exercise.gifUrl)
                        .into(holder.gifImageView)
                }
            }
        }
    }

    override fun getItemCount(): Int = exercises.size

    fun setExerciseList(exercises: List<BaseExercise>) {
        this.exercises = exercises
        notifyDataSetChanged()
    }
}
