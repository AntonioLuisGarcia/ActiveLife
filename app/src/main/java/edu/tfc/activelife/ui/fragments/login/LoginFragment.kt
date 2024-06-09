package edu.tfc.activelife.ui.fragments.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import edu.tfc.activelife.R

/**
 * LoginFragment handles the user login process.
 * Users can enter their email and password to log in.
 * The fragment also includes a link to navigate to the registration screen.
 */
class LoginFragment : Fragment() {
    // Declare the UI elements and FirebaseAuth instance
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var textViewRegister: TextView
    private lateinit var auth: FirebaseAuth

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional and can be null for non-graphical fragments.
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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // Initialize Firebase Auth instance
        auth = FirebaseAuth.getInstance()

        // Get references to the UI elements
        editTextUsername = view.findViewById(R.id.editTextUsername)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        buttonLogin = view.findViewById(R.id.buttonLogin)
        textViewRegister = view.findViewById(R.id.textViewRegister)

        return view
    }

    /**
     * Called immediately after onCreateView has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once they know their view hierarchy has been completely created.
     *
     * @param view The view returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up OnClickListener for the login button
        buttonLogin.setOnClickListener {
            val email = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()

            // Validate input and call method to sign in with email and password
            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(requireContext(), "Please enter email", Toast.LENGTH_SHORT).show()
            } else if(password.isEmpty()){
                Toast.makeText(requireContext(), "Please enter password", Toast.LENGTH_SHORT).show()
            } else {
                signInWithEmailAndPassword(email, password)
            }
        }

        // Set up OnClickListener for the register text view
        textViewRegister.setOnClickListener {
            // Navigate to the register fragment
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    /**
     * Method to sign in with email and password using Firebase Authentication.
     *
     * @param email The user's email address.
     * @param password The user's password.
     */
    private fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                    // Navigate to MainActivity
                    findNavController().navigate(R.id.action_loginFragment_to_mainActivity)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(requireContext(), "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}