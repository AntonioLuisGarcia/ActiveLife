package edu.tfc.activelife.ui.fragments.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.tfc.activelife.R

/**
 * RegisterFragment handles the user registration process.
 * Users can enter their username, email, password, and confirm their password to register.
 * The fragment also includes an admin checkbox for special user roles.
 */
class RegisterFragment : Fragment() {

    // UI elements
    private lateinit var editTextUsername: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonRegister: Button
    private lateinit var checkBoxAdmin: CheckBox
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Get references to the view elements
        editTextUsername = view.findViewById(R.id.editTextUsername)
        editTextEmail = view.findViewById(R.id.editTextEmail)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        editTextConfirmPassword = view.findViewById(R.id.editTextConfirmPassword)
        buttonRegister = view.findViewById(R.id.buttonRegister)
        checkBoxAdmin = view.findViewById(R.id.checkBoxAdmin)

        // Set up the click listener for the login link
        view.findViewById<TextView>(R.id.textViewLoginLink).setOnClickListener {
            // Navigate back to the login fragment
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        // Set up the click listener for the register button
        buttonRegister.setOnClickListener {
            val username = editTextUsername.text.toString()
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val confirmPassword = editTextConfirmPassword.text.toString()

            // Validate input and register the user
            validateAndRegister(username, email, password, confirmPassword)
        }

        return view
    }

    /**
     * Validates user input and registers a new user if the input is valid.
     *
     * @param username The entered username
     * @param email The entered email
     * @param password The entered password
     * @param confirmPassword The entered password confirmation
     * @return True if the input is valid, false otherwise
     */
    private fun validateAndRegister(username: String, email: String, password: String, confirmPassword: String): Boolean {
        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a username", Toast.LENGTH_SHORT).show()
            return false
        }
        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter an email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a password", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }
        // Register the user with email and password
        registerWithEmailAndPassword(username, email, password)
        return true
    }

    /**
     * Registers a new user with email and password, and saves the user data to Firestore.
     *
     * @param username The entered username
     * @param email The entered email
     * @param password The entered password
     */
    private fun registerWithEmailAndPassword(username: String, email: String, password: String) {
        val isAdmin = checkBoxAdmin.isChecked // Check if the user is an admin
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Registration successful
                    val user = auth.currentUser
                    user?.let {
                        val userUid = it.uid // Get user UID
                        val userEmail = it.email // Get user email

                        // Create a map with user data, including admin status
                        val userData = hashMapOf(
                            "username" to username,
                            "email" to userEmail,
                            "uuid" to userUid,
                            "admin" to isAdmin,
                            "aceptado" to false
                        )

                        // Save user data to Firestore
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(userUid)
                            .set(userData)
                            .addOnSuccessListener {
                                // Data saved successfully
                                Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_registerFragment_to_mainActivity)
                            }
                            .addOnFailureListener { e ->
                                // Error saving data
                                Toast.makeText(requireContext(), "Error saving data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Registration failed
                    Toast.makeText(requireContext(), "Registration error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}