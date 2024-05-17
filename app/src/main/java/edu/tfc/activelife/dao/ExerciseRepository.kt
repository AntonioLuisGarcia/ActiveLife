package edu.tfc.activelife.dao

class ExerciseRepository(private val exerciseDao: ExerciseDao) {
    fun getAllExercises(): List<Exercise> = exerciseDao.getAll()

    fun getExerciseByUuid(uuid: String): Exercise = exerciseDao.getByUuid(uuid)

    suspend fun insertExercise(exercise: Exercise) {
        exerciseDao.insert(exercise)
        // Agregar a Firebase
    }

    suspend fun updateExercise(exercise: Exercise) {
        exerciseDao.update(exercise)
        // Sincronizar con Firebase
    }

    suspend fun deleteExercise(exercise: Exercise) {
        exerciseDao.delete(exercise)
        // Eliminar de Firebase
    }
}

class PublicExerciseRepository(private val publicExerciseDao: PublicExerciseDao) {
    fun getAllPublicExercises(): List<PublicExercise> = publicExerciseDao.getAll()

    fun getPublicExerciseByUuid(uuid: String): PublicExercise = publicExerciseDao.getByUuid(uuid)

    suspend fun insertPublicExercise(publicExercise: PublicExercise) {
        publicExerciseDao.insert(publicExercise)
        // Sincronización con Firebase
    }

    suspend fun updatePublicExercise(publicExercise: PublicExercise) {
        publicExerciseDao.update(publicExercise)
        // Sincronizar con Firebase
    }

    suspend fun deletePublicExercise(publicExercise: PublicExercise) {
        publicExerciseDao.delete(publicExercise)
        // Eliminar de Firebase
    }
}

