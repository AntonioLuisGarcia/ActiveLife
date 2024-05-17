package edu.tfc.activelife.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "exercises") val exercises: List<BaseExercise>, // Convertir a JSON String para almacenamiento
    val userUuid: String
)
