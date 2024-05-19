import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import edu.tfc.activelife.dao.CitaDao
import edu.tfc.activelife.dao.CitaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CitasRepository(private val citaDao: CitaDao, private val firebaseDb: FirebaseFirestore, private val context: Context,     private val scope: CoroutineScope) {

    init {
        observeFirebaseChanges()
    }

    private fun observeFirebaseChanges() {
        if (isNetworkAvailable()) {
            firebaseDb.collection("citas").addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("CitasRepo", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val citaList = mutableListOf<CitaEntity>()
                for (doc in snapshot!!) {
                    doc.toObject(CitaEntity::class.java)?.let {
                        citaList.add(it)
                    }
                }

                scope.launch { // Usa el scope proporcionado
                    citaList.forEach { cita ->
                        insertCita(cita)
                    }
                }
            }
        }
    }


    fun getAllCitas(): LiveData<List<CitaEntity>> = citaDao.getAll()

    fun getCitaById(id: String): LiveData<CitaEntity> = citaDao.getById(id)

    suspend fun insertCita(cita: CitaEntity) {
        citaDao.insert(cita)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        return activeNetwork != null
    }
}
