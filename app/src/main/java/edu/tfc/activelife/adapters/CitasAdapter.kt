package edu.tfc.activelife.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.firestore.FirebaseFirestore
import edu.tfc.activelife.R
import edu.tfc.activelife.dao.Cita
import edu.tfc.activelife.ui.fragments.cita.FragmentThreeDirections


/**
 * Adapter for displaying a list of appointments (citas) in a RecyclerView.
 *
 * @param citasList List of appointments to display.
 * @param context Context in which the adapter is used.
 */
class CitasAdapter(private var citasList: List<Cita>, private val context: Context) : RecyclerView.Adapter<CitasAdapter.CitasViewHolder>() {

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitasViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_cita, parent, false)
        return CitasViewHolder(itemView)
    }
    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */

    override fun onBindViewHolder(holder: CitasViewHolder, position: Int) {
        val currentItem = citasList[position]

        if (currentItem.image.isNotEmpty()) {
            holder.imageViewCita.load(currentItem.image) {
                size(400, 300)
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
        if(!currentItem.respuesta.isEmpty()){
            holder.textViewRespuesta.text = context.getString(R.string.respuesta_format, currentItem.respuesta)
        }else{
            holder.textViewRespuesta.text = context.getString(R.string.respuesta_format, context?.getString(R.string.no_response))
        }


        holder.btnDownloadPdf.setOnClickListener {
            if (currentItem.fileUrl.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentItem.fileUrl))
                context.startActivity(intent)
            } else {
                Toast.makeText(context, R.string.no_pdf, Toast.LENGTH_SHORT).show()
            }
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

    /**
     * Displays a confirmation dialog to the user before deleting an appointment.
     *
     * @param citaId The ID of the appointment to be deleted.
     */
    private fun showDeleteConfirmationDialog(citaId: String) {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("Confirmación")
        builder.setMessage("¿Estás seguro que deseas borrar esta cita?")

        builder.setPositiveButton("Sí") { _, _ ->
            deleteCitaFromFirestore(citaId)
        }

        builder.setNegativeButton("Cancelar") { _, _ -> }

        builder.show()
    }

    /**
     * Deletes the appointment from Firestore.
     *
     * @param citaId The ID of the appointment to be deleted.
     */
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

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount() = citasList.size

    fun updateData(newCitasList: List<Cita>) {
        citasList = newCitasList
        notifyDataSetChanged()
    }

    /**
     * Updates the data set of the adapter and notifies any registered observers that the data set has changed.
     *
     * @param newCitasList The new list of appointments to be displayed.
     */
    fun updateItem(index: Int, updatedCita: Cita) {
        val mutableCitasList = citasList.toMutableList()
        if (index in mutableCitasList.indices) {
            mutableCitasList[index] = updatedCita
            citasList = mutableCitasList
            notifyItemChanged(index)
        }
    }

    /**
     * ViewHolder for displaying an appointment item.
     *
     * @param itemView The view of the appointment item.
     */
    class CitasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTitulo: TextView = itemView.findViewById(R.id.text_view_titulo)
        val textViewDescripcion: TextView = itemView.findViewById(R.id.text_view_descripcion)
        val textViewFecha: TextView = itemView.findViewById(R.id.text_fecha_cita)
        val textViewEncargado: TextView = itemView.findViewById(R.id.text_view_encargado)
        val textViewEstado: TextView = itemView.findViewById(R.id.text_view_estado)
        val textViewRespuesta: TextView = itemView.findViewById(R.id.text_view_respuesta)
        val imageViewCita: ImageView = itemView.findViewById(R.id.image_view_cita)
        val btnDownloadPdf: Button = itemView.findViewById(R.id.btn_download_pdf)
        val btnEditCita: Button = itemView.findViewById(R.id.btn_edit_cita)
        val btnDeleteCita: Button = itemView.findViewById(R.id.btn_delete_cita)
        val cardView: CardView = itemView.findViewById(R.id.card_view_cita)
    }
}