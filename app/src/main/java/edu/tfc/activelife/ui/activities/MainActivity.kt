package edu.tfc.activelife.ui.activities

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.tfc.activelife.R

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mAuth: FirebaseAuth
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawer, toolbar,
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

        editIcon.setOnClickListener {
            // Navigate to the Edit Profile Fragment
            navController.navigate(R.id.editarPerfilFragment)
            drawer.closeDrawer(GravityCompat.START)
        }
    }

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
                    usernameTextView.text = username ?: "Nombre de usuario"
                    val imageUrl = snapshot.getString("imageUrl")
                    if (!imageUrl.isNullOrEmpty()) {
                        userImageView.load(imageUrl) {
                            transformations(CircleCropTransformation())
                        }
                    } else {
                        userImageView.setImageResource(R.drawable.person)
                    }
                } else {
                    Log.e("MainActivity", "Current data: null")
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.nav_item_one -> navController.navigate(R.id.homeFragment)
            R.id.nav_item_two -> navController.navigate(R.id.fragmentTwo)
            R.id.nav_item_three -> navController.navigate(R.id.fragmentThree)
            R.id.nav_item_four -> navController.navigate(R.id.aboutFragment)
            R.id.nav_item_five -> {
                mAuth.signOut()
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginRegisterActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
