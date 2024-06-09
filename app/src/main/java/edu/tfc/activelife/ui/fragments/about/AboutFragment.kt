package edu.tfc.activelife.ui.fragments.about

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import edu.tfc.activelife.R

/**
 * AboutFragment is responsible for displaying the about section of the application.
 * This fragment includes information about the creators of the app and their images.
 */
class AboutFragment : Fragment() {

    /**
     * Inflates the fragment's layout and applies the background color based on user preferences.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The inflated view of the fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        applyBackgroundColor(view)
        return view
    }

    /**
     * Called immediately after onCreateView has returned. Sets up the view and listeners for interactive elements.
     *
     * @param view The view returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
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

    /**
     * Displays a dialog with information about a person.
     *
     * @param name The name of the person.
     * @param imageResId The resource ID of the person's image.
     * @param text The description text about the person.
     */
    private fun showPersonDialog(name: String, imageResId: Int, text: String) {
        val dialog = PersonDialogFragment.newInstance(name, imageResId, text)
        dialog.show(parentFragmentManager, "PersonDialogFragment")
    }

    /**
     * Applies the background color to the view based on the user's preference stored in SharedPreferences.
     *
     * @param view The view to which the background color will be applied.
     */
    private fun applyBackgroundColor(view: View) {
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val colorResId = sharedPreferences.getInt("background_color", R.color.white)
        view.setBackgroundResource(colorResId)
    }
}