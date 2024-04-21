package edu.tfc.activelife.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.firestore.FirebaseFirestore
import edu.tfc.activelife.R
import edu.tfc.activelife.dao.Cita
import edu.tfc.activelife.ui.fragments.FragmentThreeDirections

class CitasAdapter(private var citasList: List<Cita>, private val context: Context) : RecyclerView.Adapter<CitasAdapter.CitasViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitasViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_cita, parent, false)
        return CitasViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CitasViewHolder, position: Int) {
        val currentItem = citasList[position]

        holder.imageViewCita.load(currentItem?.image)

        holder.textViewTitulo.text = currentItem.titulo
        holder.textViewDescripcion.text = currentItem.descripcion
        holder.textViewFecha.text = currentItem.fecha

        // Manejar el clic en el botón de edición
        holder.btnEditCita.setOnClickListener {
            val citaId = currentItem.id
            val action = FragmentThreeDirections.actionFragmentThreeToFragmentCrearCita(citaId)
            holder.itemView.findNavController().navigate(action)
        }

        // Manejar el clic en el botón de borrado
        holder.btnDeleteCita.setOnClickListener {
            // Mostrar la ventana emergente de confirmación
            showDeleteConfirmationDialog(currentItem.id)
        }
    }

    // Método para mostrar la ventana emergente de confirmación
    private fun showDeleteConfirmationDialog(citaId: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmación")
        builder.setMessage("¿Estás seguro que deseas borrar esta cita?")

        builder.setPositiveButton("Sí") { _, _ ->
            // Eliminar la cita de la base de datos
            deleteCitaFromFirestore(citaId)
        }

        builder.setNegativeButton("Cancelar") { _, _ -> }

        builder.show()
    }

    // Método para eliminar la cita de la base de datos
    private fun deleteCitaFromFirestore(citaId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("citas").document(citaId)
            .delete()
            .addOnSuccessListener {
                // La cita se eliminó correctamente
                // Actualizar la lista de citas si es necesario
                // Por ejemplo, puedes volver a cargar las citas desde Firestore
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

    class CitasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTitulo: TextView = itemView.findViewById(R.id.text_view_titulo)
        val textViewDescripcion: TextView = itemView.findViewById(R.id.text_view_descripcion)
        val textViewFecha: TextView = itemView.findViewById(R.id.text_fecha_cita)
        val imageViewCita: ImageView = itemView.findViewById(R.id.image_view_cita) // Agrega referencia al ImageView
        val btnEditCita: Button = itemView.findViewById(R.id.btn_edit_cita)
        val btnDeleteCita: Button = itemView.findViewById(R.id.btn_delete_cita)
    }
}
