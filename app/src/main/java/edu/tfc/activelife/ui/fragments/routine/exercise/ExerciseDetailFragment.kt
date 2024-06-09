package edu.tfc.activelife.ui.fragments.routine.exercise

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import edu.tfc.activelife.R
import edu.tfc.activelife.dao.PublicExercise

/**
 * A Fragment subclass that displays the details of a selected exercise.
 * It uses the arguments passed to it to show the exercise name, description,
 * number of series, repetitions, and a GIF image if available.
 */
class ExerciseDetailFragment : Fragment() {
    private lateinit var exercise: PublicExercise

    /**
     * Called to do initial creation of the fragment.
     * This is where we retrieve the exercise object from the fragment's arguments.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            exercise = it.getSerializable("exercise") as PublicExercise
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.item_exercise_detailed, container, false)
        view.findViewById<TextView>(R.id.textViewDetailedExerciseName).text = exercise.exerciseName
        view.findViewById<TextView>(R.id.textViewDetailedExerciseDescription).text = exercise.description
        view.findViewById<TextView>(R.id.textViewDetailedExerciseSeries).text = "Series: ${exercise.series}"
        view.findViewById<TextView>(R.id.textViewDetailedExerciseRepetitions).text = "Repetitions: ${exercise.repetitions}"
        if (!exercise.gifUrl.isEmpty()) {
            Glide.with(this).load(exercise.gifUrl).into(view.findViewById<ImageView>(R.id.imageViewExerciseGif))
        }
        return view
    }

    companion object {
        /**
         * Factory method to create a new instance of this fragment using the provided parameters.
         *
         * @param exercise The exercise object to be displayed in the fragment.
         * @return A new instance of fragment ExerciseDetailFragment.
         */
        fun newInstance(exercise: PublicExercise): ExerciseDetailFragment {
            val fragment = ExerciseDetailFragment()
            val bundle = Bundle().apply {
                putSerializable("exercise", exercise)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}