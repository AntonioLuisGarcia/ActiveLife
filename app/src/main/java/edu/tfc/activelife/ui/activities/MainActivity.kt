package edu.tfc.activelife.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.tfc.activelife.R

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mAuth: FirebaseAuth
    private lateinit var navController: NavController

    @SuppressLint("MissingInflatedId")
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

        // Configurar NavController después de inflar el diseño de la actividad
        navController = findNavController(R.id.nav_host_fragment)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val headerView = navigationView.getHeaderView(0)
        val usernameTextView: TextView = headerView.findViewById(R.id.nav_header_textView)

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance()

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userUuid = currentUser?.uid

        if (userUuid != null) {
            val db = FirebaseFirestore.getInstance()
            val usersCollection = db.collection("users")
            val userDocument = usersCollection.document(userUuid)

            userDocument.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val username = documentSnapshot.getString("username")
                    if (username != null) {
                        // El valor del nombre de usuario se ha encontrado
                        // Puedes usar el valor de username aquí
                        usernameTextView.text = username
                        Log.d("Username", "El nombre de usuario es: $username")
                    } else {
                        // El campo username no está presente en el documento
                        Log.d("Username", "El campo 'username' no está presente en el documento")
                    }
                } else {
                    // El documento del usuario no existe
                    Log.d("Username", "El documento del usuario no existe")
                }
            }.addOnFailureListener { exception ->
                // Error al obtener el documento del usuario
                Log.e("Username", "Error al obtener el documento del usuario: $exception")
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.nav_item_one -> navController.navigate(R.id.fragmentOne)
            R.id.nav_item_two -> navController.navigate(R.id.fragmentTwo)
            R.id.nav_item_three -> navController.navigate(R.id.fragmentThree)
            R.id.nav_item_four -> Toast.makeText(this, "Item 4", Toast.LENGTH_SHORT).show()
            R.id.nav_item_five -> {
                // Cerrar sesión en Firebase
                mAuth.signOut()
                // Mostrar un mensaje de Toast
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                // Iniciar la actividad de inicio de sesión y registro
                val intent = Intent(this, LoginRegisterActivity::class.java)
                startActivity(intent)
                finish() // Cierra la actividad actual para que el usuario no pueda volver atrás
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
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
