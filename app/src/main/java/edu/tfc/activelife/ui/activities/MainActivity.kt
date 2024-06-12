package edu.tfc.activelife.ui.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import edu.tfc.activelife.R

/**
 * MainActivity is responsible for setting up the main UI components, handling navigation,
 * and managing user authentication and profile information.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mAuth: FirebaseAuth
    private lateinit var navController: NavController

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }

    /**
     * Called when the activity is first created. Sets up the toolbar, navigation drawer,
     * user profile listener, and requests camera permission.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(
            this, drawer, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        navController = findNavController(R.id.nav_host_fragment)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val headerView = navigationView.getHeaderView(0)
        val usernameTextView: TextView = headerView.findViewById(R.id.nav_header_textView)
        val userImageView: ImageView = headerView.findViewById(R.id.nav_header_imageView)
        val editIcon: ImageView = headerView.findViewById(R.id.nav_header_edit_icon)

        mAuth = FirebaseAuth.getInstance()
        setupUserProfileListener(usernameTextView, userImageView)

        moveLogoutMenuItemToEnd(navigationView)

        editIcon.setOnClickListener {
            // Navigate to the Edit Profile Fragment
            navController.navigate(R.id.editarPerfilFragment)
            drawer.closeDrawer(GravityCompat.START)
        }

        // Request camera permission at the start of the activity
        requestCameraPermission()
        applyBackgroundColor()
    }

    /**
     * Applies the background color based on user preferences.
     */
    private fun applyBackgroundColor() {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val colorResId =
            sharedPreferences.getInt("background_color", R.color.white) // Default background color
        findViewById<View>(R.id.drawer_layout).setBackgroundResource(colorResId)
    }

    /**
     * Requests camera permission from the user.
     */
    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            // Permission already granted, start the camera or related functionality
            startCamera()
        }
    }

    /**
     * Starts the camera functionality.
     */
    private fun startCamera() {
        Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
    }

    /**
     * Handles the result of permission requests.
     *
     * @param requestCode The request code passed in requestPermissions(android.app.Activity, String[], int).
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                startCamera()
            } else {
                // Permission denied
                Toast.makeText(
                    this,
                    "Camera permission is required to use this functionality",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Sets up a listener for the user's profile information.
     *
     * @param usernameTextView TextView to display the username.
     * @param userImageView ImageView to display the user's profile image.
     */
    private fun setupUserProfileListener(usernameTextView: TextView, userImageView: ImageView) {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val userDocument = db.collection("users").document(currentUser.uid)

            userDocument.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("MainActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("MainActivity", "Current data: ${snapshot.data}")
                    val username = snapshot.getString("username")
                    usernameTextView.text = username ?: "Username"
                    val imageUrl = snapshot.getString("imageUrl")
                    if (!imageUrl.isNullOrEmpty()) {
                        userImageView.load(imageUrl) {
                            transformations(CircleCropTransformation())
                        }
                    } else {
                        userImageView.setImageResource(R.drawable.imagen_por_defecto)
                    }
                } else {
                    Log.e("MainActivity", "Current data: null")
                }
            }
        }
    }

    /**
     * Handles navigation item selection.
     *
     * @param item The selected menu item.
     * @return true if the navigation item is handled, false otherwise.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item_one -> {
                navController.navigate(R.id.homeFragment)
                supportActionBar?.title = getString(R.string.home)
            }

            R.id.nav_item_two -> {
                navController.navigate(R.id.fragmentTwo)
                supportActionBar?.title = getString(R.string.routines)
            }

            R.id.nav_item_three -> {
                navController.navigate(R.id.fragmentThree)
                supportActionBar?.title = getString(R.string.meetings)
            }

            R.id.nav_item_four -> {
                navController.navigate(R.id.aboutFragment)
                supportActionBar?.title = getString(R.string.about)
            }

            R.id.nav_item_five -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginRegisterActivity::class.java))
                finish()
            }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Moves the logout menu item to the end of the navigation drawer.
     *
     * @param navigationView The NavigationView containing the menu.
     */
    private fun moveLogoutMenuItemToEnd(navigationView: NavigationView) {
        val menu = navigationView.menu
        val logoutItem = menu.findItem(R.id.nav_item_five)
        menu.removeItem(R.id.nav_item_five)
        val newLogoutItem = menu.add(Menu.NONE, logoutItem.itemId, Menu.NONE, logoutItem.title)
        newLogoutItem.icon = logoutItem.icon
    }

    /**
     * Called after onRestoreInstanceState(Bundle), onRestart(), or onPause(), for your activity to start interacting with the user.
     * This is a good place to begin animations, open exclusive-access devices (such as the camera), etc.
     */
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    /**
     * Called by the system when the device configuration changes while your component is running.
     *
     * @param newConfig The new device configuration.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item The selected menu item.
     * @return boolean Return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Called when the activity is resumed after being paused.
     * This method is typically used to refresh UI elements and resume operations that were paused.
     * In this implementation, it applies the background color based on user preferences.
     */
    override fun onResume() {
        super.onResume()
        applyBackgroundColor()
    }
}
