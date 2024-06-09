package edu.tfc.activelife.ui.fragments.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import edu.tfc.activelife.R

/**
 * PersonDialogFragment is a DialogFragment that displays information about a person.
 * It shows the person's name, an image, and a description in a dialog.
 */
class PersonDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_IMAGE_RES_ID = "image_res_id"
        private const val ARG_TEXT = "text"

        /**
         * Creates a new instance of PersonDialogFragment with the provided details.
         *
         * @param name The name of the person.
         * @param imageResId The resource ID of the person's image.
         * @param text The description text of the person.
         * @return A new instance of PersonDialogFragment.
         */
        fun newInstance(name: String, imageResId: Int, text: String): PersonDialogFragment {
            val fragment = PersonDialogFragment()
            val args = Bundle()
            args.putString(ARG_NAME, name)
            args.putInt(ARG_IMAGE_RES_ID, imageResId)
            args.putString(ARG_TEXT, text)
            fragment.arguments = args
            return fragment
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_person_dialog, container, false)

        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val nameTextView = view.findViewById<TextView>(R.id.textView)
        val descriptionTextView = view.findViewById<TextView>(R.id.descriptionView)

        // Retrieve the arguments passed to the fragment and set the corresponding views.
        val name = arguments?.getString(ARG_NAME)
        val imageResId = arguments?.getInt(ARG_IMAGE_RES_ID)
        val text = arguments?.getString(ARG_TEXT)

        nameTextView.text = name
        imageResId?.let { imageView.setImageResource(it) }
        descriptionTextView.text = text

        return view
    }
}