package edu.tfc.activelife.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cita")
data class CitaEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val image: String
)
