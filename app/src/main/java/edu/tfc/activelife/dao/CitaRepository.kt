package edu.tfc.activelife.dao

class CitaRepository(private val citaDao: CitaDao) {
    fun getAllCitas(): List<Cita> = citaDao.getAll()

    fun getCitaById(id: String): Cita = citaDao.getById(id)

    suspend fun insertCita(cita: Cita) {
        citaDao.insert(cita)
        // Aquí también podrías agregar lógica para insertar en Firebase
    }

    suspend fun updateCita(cita: Cita) {
        citaDao.update(cita)
        // Sincronizar cambios con Firebase
    }

    suspend fun deleteCita(cita: Cita) {
        citaDao.delete(cita)
        // Eliminar también en Firebase si es necesario
    }
}
