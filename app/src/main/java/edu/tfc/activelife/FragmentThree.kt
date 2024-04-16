package edu.tfc.activelife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController

class FragmentThree : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_three, container, false)

        // Referencia al botón
        val btnAddCita: Button = view.findViewById(R.id.btn_add_cita)

        // Manejador del clic del botón
        btnAddCita.setOnClickListener {
            // Navegar al fragmento FragmentCrearCita al hacer clic en el botón
            findNavController().navigate(R.id.action_fragmentThree_to_fragmentCrearCita)
        }

        return view
    }
}
