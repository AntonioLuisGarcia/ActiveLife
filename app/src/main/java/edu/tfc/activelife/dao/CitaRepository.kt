package edu.tfc.activelife.dao

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CitaRepository @Inject constructor(
    private val citaDao: CitaDao
) {
    fun getAllCitas(): LiveData<List<CitaEntity>> = citaDao.getAll()

    fun getCitaById(id: String): LiveData<CitaEntity> = citaDao.getById(id)

    suspend fun insertCita(cita: CitaEntity) {
        citaDao.insert(cita)
    }

    fun addCita(cita: CitaEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            citaDao.insert(cita)
        }
    }
}
