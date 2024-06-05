package edu.tfc.activelife.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
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

        if (currentItem.image.isNotEmpty()) {
            holder.imageViewCita.load(currentItem.image) {
                size(400, 300) // Ajusta los valores según lo necesario
                crossfade(true)
            }
            holder.imageViewCita.visibility = View.VISIBLE
        } else {
            holder.imageViewCita.visibility = View.GONE
        }

        holder.textViewTitulo.text = currentItem.titulo
        holder.textViewDescripcion.text = currentItem.descripcion
        holder.textViewFecha.text = currentItem.fecha
        holder.textViewEncargado.text = currentItem.encargado
        holder.textViewEstado.text = currentItem.estado
        holder.textViewRespuesta.text = currentItem.respuesta

        holder.btnDownloadPdf.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentItem.fileUrl))
            context.startActivity(intent)
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
        val mutableCitasList = citasList.toMutableList()
        if (index in mutableCitasList.indices) {
            mutableCitasList[index] = updatedCita
            citasList = mutableCitasList
            notifyItemChanged(index)
        }
    }

    class CitasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTitulo: TextView = itemView.findViewById(R.id.text_view_titulo)
        val textViewDescripcion: TextView = itemView.findViewById(R.id.text_view_descripcion)
        val textViewFecha: TextView = itemView.findViewById(R.id.text_fecha_cita)
        val textViewEncargado: TextView = itemView.findViewById(R.id.text_view_encargado)
        val textViewEstado: TextView = itemView.findViewById(R.id.text_view_estado)
        val textViewRespuesta: TextView = itemView.findViewById(R.id.text_view_respuesta) // Añadir TextView para respuesta
        val imageViewCita: ImageView = itemView.findViewById(R.id.image_view_cita)
        val btnDownloadPdf: Button = itemView.findViewById(R.id.btn_download_pdf) // Añadir Button para descargar PDF
        val btnEditCita: Button = itemView.findViewById(R.id.btn_edit_cita)
        val btnDeleteCita: Button = itemView.findViewById(R.id.btn_delete_cita)
        val cardView: CardView = itemView.findViewById(R.id.card_view_cita)
    }
}