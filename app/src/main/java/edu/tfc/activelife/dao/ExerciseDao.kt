package edu.tfc.activelife.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise")
    fun getAll(): List<Exercise>

    @Query("SELECT * FROM exercise WHERE uuid = :uuid")
    fun getByUuid(uuid: String): Exercise

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(exercise: Exercise)

    @Update
    fun update(exercise: Exercise)

    @Delete
    fun delete(exercise: Exercise)
}

@Dao
interface PublicExerciseDao {
    @Query("SELECT * FROM public_exercise")
    fun getAll(): List<PublicExercise>

    @Query("SELECT * FROM public_exercise WHERE uuid = :uuid")
    fun getByUuid(uuid: String): PublicExercise

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(publicExercise: PublicExercise)

    @Update
    fun update(publicExercise: PublicExercise)

    @Delete
    fun delete(publicExercise: PublicExercise)
}

