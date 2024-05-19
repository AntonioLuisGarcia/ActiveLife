package edu.tfc.activelife.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CitaDao {
    @Query("SELECT * FROM cita")
    fun getAll(): LiveData<List<CitaEntity>>  // Cambia el tipo de retorno a LiveData

    @Query("SELECT * FROM cita WHERE id = :id")
    fun getById(id: String): LiveData<CitaEntity>  // Si también necesitas que esto sea LiveData

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cita: CitaEntity)

    @Update
    fun update(cita: CitaEntity)

    @Delete
    fun delete(cita: CitaEntity)
}
