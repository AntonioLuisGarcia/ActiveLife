package edu.tfc.activelife.ui.fragments.about

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import edu.tfc.activelife.R

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        applyBackgroundColor(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyBackgroundColor(view)

        val imageMe = view.findViewById<ImageView>(R.id.imageMe)
        val imagePartner = view.findViewById<ImageView>(R.id.imagePartner)

        imageMe.setOnClickListener {
            showPersonDialog(
                getString(R.string.name_creator_1),
                R.mipmap.imgpablorecortado,
                getString(R.string.text_person_1)
            )
        }

        imagePartner.setOnClickListener {
            showPersonDialog(
                getString(R.string.name_creator_2),
                R.mipmap.garciaguerreroantonioluisrecortado,
                getString(R.string.text_person_2)
            )
        }
    }

    private fun showPersonDialog(name: String, imageResId: Int, text: String) {
        val dialog = PersonDialogFragment.newInstance(name, imageResId, text)
        dialog.show(parentFragmentManager, "PersonDialogFragment")
    }

    private fun applyBackgroundColor(view: View) {
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val colorResId = sharedPreferences.getInt("background_color", R.color.white)
        view.setBackgroundResource(colorResId)
    }
}