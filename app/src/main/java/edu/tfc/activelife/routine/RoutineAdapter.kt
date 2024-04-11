package edu.tfc.activelife.routine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.tfc.activelife.R
import edu.tfc.activelife.dao.Routine

class RoutineAdapter() : RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_routine, parent, false)
        return RoutineViewHolder(view)
    }

    private var routineList = emptyList<Routine>()

    fun setRoutineList(list: List<Routine>) {
        routineList = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val currentRoutine = routineList[position]
        holder.bind(currentRoutine)
    }

    override fun getItemCount(): Int {
        return routineList.size
    }

    inner class RoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewRoutineTitle)

        fun bind(routine: Routine) {
            titleTextView.text = routine.title
        }
    }
}
