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

/**
 * Utility class providing common functionalities such as image handling, network checking, and date formatting.
 */
object Utils {

    const val REQUEST_IMAGE_CAPTURE = 1
    const val REQUEST_IMAGE_PICK = 2

    /**
     * Shows a dialog to pick an image from the camera or gallery.
     *
     * @param fragment The fragment from which the dialog is invoked.
     * @param context The context in which the dialog should be shown.
     * @param title The title of the dialog.
     * @param callback The callback to handle the picked image, either as a Bitmap or Uri.
     */
    fun showImagePickerDialog(fragment: Fragment, context: Context, title: String, callback: (Bitmap?, Uri?) -> Unit) {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setItems(options) { dialog, item ->
            when (options[item]) {
                "Take Photo" -> {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    fragment.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
                "Choose from Gallery" -> {
                    val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    fragment.startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK)
                }
                "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    /**
     * Handles the result of an image picking activity.
     *
     * @param requestCode The request code identifying the request.
     * @param resultCode The result code returned by the activity.
     * @param data The intent data returned by the activity.
     * @param callback The callback to handle the picked image, either as a Bitmap or Uri.
     */
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

    /**
     * Loads an image into an ImageView, either from a Bitmap or Uri.
     *
     * @param imageView The ImageView to load the image into.
     * @param bitmap The Bitmap of the image.
     * @param uri The Uri of the image.
     * @param roundImage Whether to apply a circular crop transformation.
     */
    fun loadImageIntoView(imageView: ImageView, bitmap: Bitmap?, uri: Uri?, roundImage: Boolean = false) {
        if (bitmap != null) {
            imageView.load(bitmap) {
                if (roundImage) {
                    transformations(CircleCropTransformation())
                } else {
                    imageView.setImageBitmap(bitmap)
                }
            }
        } else if (uri != null) {
            imageView.load(uri) {
                if (roundImage) {
                    transformations(CircleCropTransformation())
                } else {
                    imageView.setImageURI(uri)
                }
            }
        }
    }

    /**
     * Checks if the network is available.
     *
     * @param context The context to check the network availability.
     * @return True if the network is available, false otherwise.
     */
    fun isNetworkAvailable(context: Context?): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetwork = connectivityManager?.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }

    /**
     * Formats a Firebase timestamp into a readable date and time string.
     *
     * @param seconds The seconds part of the timestamp.
     * @param nanoseconds The nanoseconds part of the timestamp.
     * @return The formatted date and time string.
     */
    fun formatFirebaseTimestamp(seconds: Long, nanoseconds: Int): String {
        val instant = Instant.ofEpochSecond(seconds, nanoseconds.toLong())
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        return formatter.format(zonedDateTime)
    }

    /**
     * Gets the current day of the week as a string.
     *
     * @return The current day of the week.
     */
    fun getCurrentDayOfWeek(): String {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            Calendar.SUNDAY -> "Sunday"
            else -> ""
        }
    }
}
