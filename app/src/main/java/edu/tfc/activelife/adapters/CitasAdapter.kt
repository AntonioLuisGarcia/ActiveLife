package edu.tfc.activelife.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import edu.tfc.activelife.R
import edu.tfc.activelife.dao.Cita
import edu.tfc.activelife.ui.fragments.FragmentThreeDirections

class CitasAdapter(private var citasList: List<Cita>) : RecyclerView.Adapter<CitasAdapter.CitasViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitasViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_cita, parent, false)
        return CitasViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CitasViewHolder, position: Int) {
        val currentItem = citasList[position]
        holder.textViewTitulo.text = currentItem.titulo
        holder.textViewDescripcion.text = currentItem.descripcion
        holder.textViewFecha.text = currentItem.fecha

        // Manejar el clic en el botón de edición
        holder.btnEditCita.setOnClickListener {
            val citaId = currentItem.id
            val action = FragmentThreeDirections.actionFragmentThreeToFragmentCrearCita(citaId)
            holder.itemView.findNavController().navigate(action)
        }
    }

    override fun getItemCount() = citasList.size

    fun updateData(newCitasList: List<Cita>) {
        citasList = newCitasList
        notifyDataSetChanged()
    }

    class CitasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTitulo: TextView = itemView.findViewById(R.id.text_view_titulo)
        val textViewDescripcion: TextView = itemView.findViewById(R.id.text_view_descripcion)
        val textViewFecha: TextView = itemView.findViewById(R.id.text_fecha_cita)
        val btnEditCita: Button = itemView.findViewById(R.id.btn_edit_cita)
    }
}
