package edu.tfc.activelife.utils

import android.content.Context
import android.net.ConnectivityManager
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.ImageView
import androidx.fragment.app.Fragment
import coil.load
import coil.transform.CircleCropTransformation
import java.util.Calendar

object Utils {

    const val REQUEST_IMAGE_CAPTURE = 1
    const val REQUEST_IMAGE_PICK = 2

    fun showImagePickerDialog(fragment: Fragment, context: Context, title: String, callback: (Bitmap?, Uri?) -> Unit) {
        val options = arrayOf<CharSequence>("Tomar Foto", "Elegir de la Galería", "Cancelar")
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setItems(options) { dialog, item ->
            when (options[item]) {
                "Tomar Foto" -> {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    fragment.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
                "Elegir de la Galería" -> {
                    val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    fragment.startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK)
                }
                "Cancelar" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?, callback: (Bitmap?, Uri?) -> Unit) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    callback(imageBitmap, null)
                }
                REQUEST_IMAGE_PICK -> {
                    val imageUri = data?.data
                    callback(null, imageUri)
                }
            }
        }
    }

    fun loadImageIntoView(imageView: ImageView, bitmap: Bitmap?, uri: Uri?, roundImage: Boolean = false) {
        if (bitmap != null) {
            imageView.load(bitmap) {
                if (roundImage) {
                    transformations(CircleCropTransformation())
                }else{
                    imageView.setImageBitmap(bitmap)
                }
            }
        } else if (uri != null) {
            imageView.load(uri) {
                if (roundImage) {
                    transformations(CircleCropTransformation())
                }else{
                    imageView.setImageURI(uri)
                }
            }
        }
    }

    fun isNetworkAvailable(context: Context?): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetwork = connectivityManager?.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }

    fun formatFirebaseTimestamp(seconds: Long, nanoseconds: Int): String {
        // Crea un objeto Instant usando segundos y nanosegundos
        val instant = Instant.ofEpochSecond(seconds, nanoseconds.toLong())

        // Convierte el Instant a ZonedDateTime para aplicar zona horaria
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())

        // Formatea la fecha y hora en el formato deseado
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        return formatter.format(zonedDateTime)
    }

    fun getCurrentDayOfWeek(): String {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            Calendar.MONDAY -> "Lunes"
            Calendar.TUESDAY -> "Martes"
            Calendar.WEDNESDAY -> "Miércoles"
            Calendar.THURSDAY -> "Jueves"
            Calendar.FRIDAY -> "Viernes"
            Calendar.SATURDAY -> "Sábado"
            Calendar.SUNDAY -> "Domingo"
            else -> ""
        }
    }
}
