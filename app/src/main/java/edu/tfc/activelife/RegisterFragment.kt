package edu.tfc.activelife

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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonRegister: Button
    private lateinit var checkBoxAdmin: CheckBox // Agregamos la referencia al CheckBox
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Obtener referencias de los elementos de la vista
        editTextUsername = view.findViewById(R.id.editTextUsername)
        editTextEmail = view.findViewById(R.id.editTextEmail)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        editTextConfirmPassword = view.findViewById(R.id.editTextConfirmPassword)
        buttonRegister = view.findViewById(R.id.buttonRegister)
        checkBoxAdmin = view.findViewById(R.id.checkBoxAdmin) // Inicializamos el CheckBox

        view.findViewById<TextView>(R.id.textViewLoginLink).setOnClickListener {
            // Navegar de vuelta al fragmento de inicio de sesión
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        // Configurar OnClickListener para el botón de registro
        buttonRegister.setOnClickListener {
            val username = editTextUsername.text.toString()
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val confirmPassword = editTextConfirmPassword.text.toString()

            // Verificar que las contraseñas coincidan
            if (password == confirmPassword) {
                // Llamar al método para registrar un nuevo usuario con el correo electrónico y contraseña
                registerWithEmailAndPassword(username, email, password)
            } else {
                // Mostrar un mensaje de error si las contraseñas no coinciden
                Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun registerWithEmailAndPassword(username: String, email: String, password: String) {
        val isAdmin = checkBoxAdmin.isChecked // Verificamos si el usuario es administrador
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Registro exitoso
                    val user = auth.currentUser
                    user?.let {
                        val userUid = it.uid // Obtener el UID del usuario
                        val userEmail = it.email // Obtener el email del usuario

                        // Crear un mapa con los datos del usuario, incluyendo si es administrador o no
                        val userData = hashMapOf(
                            "username" to username,
                            "email" to userEmail,
                            "uid" to userUid,
                            "admin" to isAdmin,
                            "aceptado" to false
                        )

                        // Añadir los datos del usuario a la colección "users" en Firestore
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(userUid)
                            .set(userData)
                            .addOnSuccessListener {
                                // Registro exitoso y datos guardados en Firestore
                                Toast.makeText(requireContext(), "Registro exitoso", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_registerFragment_to_mainActivity)
                            }
                            .addOnFailureListener { e ->
                                // Error al guardar los datos en Firestore
                                Toast.makeText(requireContext(), "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // El registro falló
                    Toast.makeText(requireContext(), "Error al registrar: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

}
