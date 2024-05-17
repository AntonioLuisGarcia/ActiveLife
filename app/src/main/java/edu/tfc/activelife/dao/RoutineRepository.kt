package edu.tfc.activelife.dao

class RoutineRepository(private val routineDao: RoutineDao) {
    fun getAllRoutines(): List<Routine> = routineDao.getAll()

    fun getRoutineById(id: String): Routine = routineDao.getById(id)

    suspend fun insertRoutine(routine: Routine) {
        routineDao.insert(routine)
        // También agregar a Firebase
    }

    suspend fun updateRoutine(routine: Routine) {
        routineDao.update(routine)
        // Sincronizar cambios con Firebase
    }

    suspend fun deleteRoutine(routine: Routine) {
        routineDao.delete(routine)
        // Eliminar también en Firebase
    }
}
