package edu.tfc.activelife.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routine")
    fun getAll(): List<Routine>

    @Query("SELECT * FROM routine WHERE id = :id")
    fun getById(id: String): Routine

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(routine: Routine)

    @Update
    fun update(routine: Routine)

    @Delete
    fun delete(routine: Routine)
}
