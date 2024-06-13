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

/**
 * Adapter class for displaying a list of exercises in a RecyclerView.
 *
 * @property exercises The list of exercises to display.
 */
class ExerciseAdapter(private var exercises: List<BaseExercise>) : RecyclerView.Adapter<ExerciseAdapter.DetailedViewHolder>() {

    /**
     * ViewHolder class for displaying detailed information about an exercise.
     *
     * @property exerciseNameTextView The TextView for displaying the exercise name.
     * @property descriptionTextView The TextView for displaying the exercise description.
     * @property seriesTextView The TextView for displaying the number of series.
     * @property repetitionsTextView The TextView for displaying the number of repetitions.
     * @property gifImageView The ImageView for displaying a GIF of the exercise.
     * @property musclesTextView The TextView for displaying the targeted muscles.
     */
    inner class DetailedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exerciseNameTextView: TextView = itemView.findViewById(R.id.textViewDetailedExerciseName)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textViewDetailedExerciseDescription)
        val seriesTextView: TextView = itemView.findViewById(R.id.textViewDetailedExerciseSeries)
        val repetitionsTextView: TextView = itemView.findViewById(R.id.textViewDetailedExerciseRepetitions)
        val gifImageView: ImageView = itemView.findViewById(R.id.imageViewExerciseGif)
        val musclesTextView: TextView = itemView.findViewById(R.id.textViewDetailedExerciseMuscles)
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new DetailedViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailedViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_exercise_detailed, parent, false)
        return DetailedViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method should update the contents of the ViewHolder's itemView to reflect the item at the given position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: DetailedViewHolder, position: Int) {
        val exercise = exercises[position]
        if (exercise is PublicExercise) {
            holder.exerciseNameTextView.text = exercise.exerciseName
            holder.descriptionTextView.text = exercise.description
            holder.seriesTextView.text = holder.itemView.context.getString(R.string.repetitions_format, exercise.series.toInt())
            holder.repetitionsTextView.text= holder.itemView.context.getString(R.string.repetitions_format, exercise.repetitions.toInt())
            if(exercise.target.isEmpty()){
                holder.musclesTextView.text = holder.itemView.context.getString(R.string.targeted_muscles_format,"none")
                if(exercise.bodyPart.isNotEmpty()) {
                    holder.musclesTextView.text = holder.itemView.context.getString(R.string.targeted_muscles_format, exercise.bodyPart)
                }
            }else{
                holder.musclesTextView.text = holder.itemView.context.getString(R.string.targeted_muscles_format, exercise.target)
            }

            if (exercise.gifUrl.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(exercise.gifUrl)
                    .into(holder.gifImageView)
                holder.gifImageView.visibility = View.VISIBLE
            } else {
                holder.gifImageView.visibility = View.GONE
            }
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int = exercises.size

    /**
     * Sets a new list of exercises and notifies the adapter of the data change.
     *
     * @param exercises The new list of exercises to display.
     */
    fun setExerciseList(exercises: List<BaseExercise>) {
        this.exercises = exercises
        notifyDataSetChanged()
    }
}
