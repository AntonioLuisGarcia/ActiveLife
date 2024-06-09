package edu.tfc.activelife.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import edu.tfc.activelife.dao.PublicExercise
import edu.tfc.activelife.ui.fragments.routine.exercise.ExerciseDetailFragment

/**
 * Adapter for the ViewPager2 that handles the swiping between different exercise detail fragments.
 * This adapter is used to display the details of each exercise in a separate fragment.
 *
 * @param fragment The parent fragment hosting the ViewPager2.
 * @param exercises The list of public exercises to be displayed in the ViewPager2.
 */
class ExerciseSwiperAdapter(fragment: Fragment, private val exercises: List<PublicExercise>) : FragmentStateAdapter(fragment) {

    /**
     * Returns the total number of items in the adapter.
     *
     * @return The size of the exercises list.
     */
    override fun getItemCount(): Int = exercises.size

    /**
     * Creates a new fragment for the given position.
     *
     * @param position The position of the item within the adapter's data set.
     * @return A new ExerciseDetailFragment instance for the exercise at the given position.
     */
    override fun createFragment(position: Int): Fragment {
        return ExerciseDetailFragment.newInstance(exercises[position])
    }
}