package edu.tfc.activelife

import FragmentOne
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
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mAuth: FirebaseAuth


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Mostrar el fragmento inicial al iniciar la actividad
        val initialFragment = MainFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, initialFragment)
            .commit()

        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

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
            R.id.nav_item_one -> {
                // Reemplaza el contenido principal con el Fragment correspondiente
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, FragmentOne())
                    .commit()
            }
            R.id.nav_item_two -> {
                // Reemplaza el contenido principal con el Fragment correspondiente
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, FragmentTwo())
                    .commit()
            }
            R.id.nav_item_three -> Toast.makeText(this, "Item 3", Toast.LENGTH_SHORT).show()
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