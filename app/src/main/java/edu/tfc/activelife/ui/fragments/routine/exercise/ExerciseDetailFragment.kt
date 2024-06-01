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

class ExerciseDetailFragment : Fragment() {
    private lateinit var exercise: PublicExercise

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            exercise = it.getSerializable("exercise") as PublicExercise
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.item_exercise_detailed, container, false)
        view.findViewById<TextView>(R.id.textViewDetailedExerciseName).text = exercise.exerciseName
        view.findViewById<TextView>(R.id.textViewDetailedExerciseDescription).text = exercise.description
        view.findViewById<TextView>(R.id.textViewDetailedExerciseSeries).text = "Series: ${exercise.series}"
        view.findViewById<TextView>(R.id.textViewDetailedExerciseRepetitions).text = "Repetitions: ${exercise.repetitions}"
        if(!exercise.gifUrl.isEmpty()){
            Glide.with(this).load(exercise.gifUrl).into(view.findViewById<ImageView>(R.id.imageViewExerciseGif))
        }
        return view
    }

    companion object {
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