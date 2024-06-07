package edu.tfc.activelife.ui.fragments.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import edu.tfc.activelife.R

class PersonDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_NAME = "name"
        private const val ARG_IMAGE_RES_ID = "image_res_id"
        private const val ARG_TEXT = "text"

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_person_dialog, container, false)

        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val nameTextView = view.findViewById<TextView>(R.id.textView)
        val descriptionTextView = view.findViewById<TextView>(R.id.descriptionView)

        val name = arguments?.getString(ARG_NAME)
        val imageResId = arguments?.getInt(ARG_IMAGE_RES_ID)
        val text = arguments?.getString(ARG_TEXT)

        nameTextView.text = name
        imageResId?.let { imageView.setImageResource(it) }
        descriptionTextView.text = text

        return view
    }
}