package edu.tfc.activelife.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import edu.tfc.activelife.dao.PublicExercise
import edu.tfc.activelife.ui.fragments.ExerciseDetailFragment

class ExerciseSwiperAdapter(fragment: Fragment, private val exercises: List<PublicExercise>) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = exercises.size

    override fun createFragment(position: Int): Fragment {
        return ExerciseDetailFragment.newInstance(exercises[position])
    }
}
