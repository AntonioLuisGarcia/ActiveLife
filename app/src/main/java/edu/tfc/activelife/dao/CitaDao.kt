package edu.tfc.activelife.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CitaDao {
    @Query("SELECT * FROM cita")
    fun getAll(): List<Cita>

    @Query("SELECT * FROM cita WHERE id = :id")
    fun getById(id: String): Cita

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cita: Cita)

    @Update
    fun update(cita: Cita)

    @Delete
    fun delete(cita: Cita)
}
