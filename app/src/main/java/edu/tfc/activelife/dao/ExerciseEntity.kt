package edu.tfc.activelife.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise")
data class ExerciseEntity(
    @PrimaryKey override val uuid: String,
    override val exerciseName: String,
    override val series: String,
    override val repetitions: String,
    override val gifUrl: String,
    override val description: String
) : BaseExercise

@Entity(tableName = "public_exercise")
data class PublicExerciseEntity(
    @PrimaryKey override val uuid: String,
    override val exerciseName: String,
    override val series: String,
    override val repetitions: String,
    override val description: String,
    val bodyPart: String,
    val equipment: String,
    override val gifUrl: String,
    val instructions: List<String>, // Convertir a JSON String para almacenamiento
    val target: String,
    val secondaryMuscles: List<String>, // Convertir a JSON String para almacenamiento
    val public: Boolean,
    val title: String,
    val userUUID: String
) : BaseExercise

