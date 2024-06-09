package edu.tfc.activelife.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import edu.tfc.activelife.R

/**
 * LoginRegisterActivity is responsible for handling the login and registration process.
 * It checks if the user is already authenticated and navigates to the main screen if so.
 */
@AndroidEntryPoint
class LoginRegisterActivity : AppCompatActivity() {

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Otherwise, it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_register_activity)
        // Check if the user is already authenticated
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User is already authenticated, navigate to the main screen
            navigateToMain()
            finish() // Finish the current activity to prevent the user from going back
        }
    }

    /**
     * Navigates to the main screen of the application.
     */
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}