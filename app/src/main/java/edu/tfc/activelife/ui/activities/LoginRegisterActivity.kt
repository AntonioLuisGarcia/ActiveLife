package edu.tfc.activelife.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import edu.tfc.activelife.R

class LoginRegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_register_activity)
        // Verificar si el usuario ya está autenticado
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // El usuario ya está autenticado, navegar a la pantalla principal
            navigateToMain()
            finish() // Finaliza la actividad actual para que el usuario no pueda volver atrás
            }
        }

    private fun navigateToMain() {
        // Navegar a la pantalla principal de la aplicación
        startActivity(Intent(this, MainActivity::class.java))
    }

}