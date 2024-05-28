package edu.tfc.activelife.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.firestore.FirebaseFirestore
import edu.tfc.activelife.R
import edu.tfc.activelife.dao.Cita
import edu.tfc.activelife.ui.fragments.cita.FragmentThreeDirections

class CitasAdapter(private var citasList: List<Cita>, private val context: Context) : RecyclerView.Adapter<CitasAdapter.CitasViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitasViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_cita, parent, false)
        return CitasViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CitasViewHolder, position: Int) {
        val currentItem = citasList[position]

        holder.imageViewCita.load(currentItem.image)
        holder.textViewTitulo.text = currentItem.titulo
        holder.textViewDescripcion.text = currentItem.descripcion
        holder.textViewFecha.text = currentItem.fecha
        holder.textViewEncargado.text = currentItem.encargado

        // Establecer el estado por defecto si no está presente
        val estado = if (currentItem.estado.isEmpty()) "Espera" else currentItem.estado
        holder.textViewEstado.text = estado

        // Aplicar estilos según el estado
        if (estado == "denegado") {
            holder.btnEditCita.visibility = View.GONE
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red)) // Asegúrate de tener un color rojo en tu archivo colors.xml
        } else {
            holder.btnEditCita.visibility = View.VISIBLE
        }

        holder.btnEditCita.setOnClickListener {
            val citaId = currentItem.id
            val action = FragmentThreeDirections.actionFragmentThreeToFragmentCrearCita(citaId)
            holder.itemView.findNavController().navigate(action)
        }

        holder.btnDeleteCita.setOnClickListener {
            showDeleteConfirmationDialog(currentItem.id)
        }
    }


    private fun showDeleteConfirmationDialog(citaId: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmación")
        builder.setMessage("¿Estás seguro que deseas borrar esta cita?")

        builder.setPositiveButton("Sí") { _, _ ->
            deleteCitaFromFirestore(citaId)
        }

        builder.setNegativeButton("Cancelar") { _, _ -> }

        builder.show()
    }

    private fun deleteCitaFromFirestore(citaId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("citas").document(citaId)
            .delete()
            .addOnSuccessListener {
                // La cita se eliminó correctamente
            }
            .addOnFailureListener {
                // Manejar el error
            }
    }

    override fun getItemCount() = citasList.size

    fun updateData(newCitasList: List<Cita>) {
        citasList = newCitasList
        notifyDataSetChanged()
    }

    fun updateItem(index: Int, updatedCita: Cita) {
        if (index in citasList.indices) {
            (citasList as MutableList)[index] = updatedCita
            notifyItemChanged(index)
        }
    }

    class CitasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTitulo: TextView = itemView.findViewById(R.id.text_view_titulo)
        val textViewDescripcion: TextView = itemView.findViewById(R.id.text_view_descripcion)
        val textViewFecha: TextView = itemView.findViewById(R.id.text_fecha_cita)
        val textViewEncargado: TextView = itemView.findViewById(R.id.text_view_encargado)
        val textViewEstado: TextView = itemView.findViewById(R.id.text_view_estado)
        val imageViewCita: ImageView = itemView.findViewById(R.id.image_view_cita)
        val btnEditCita: Button = itemView.findViewById(R.id.btn_edit_cita)
        val btnDeleteCita: Button = itemView.findViewById(R.id.btn_delete_cita)
        val cardView: CardView = itemView.findViewById(R.id.card_view_cita) // Nueva referencia para la CardView
    }

}

