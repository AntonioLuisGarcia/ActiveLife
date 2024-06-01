package edu.tfc.activelife.dao

import java.io.Serializable

// Define common interface for all exercise types
interface BaseExercise : Serializable {
    val uuid: String
    val exerciseName: String
    val series: String
    val repetitions: String
    var gifUrl: String // Agrega esto si todos los ejercicios van a tener una URL de GIF
    val description: String // Include description if used commonly
}

// Standard exercise class
data class Exercise(
    override var uuid: String = "",
    override var exerciseName: String = "",
    override var series: String = "",
    override var repetitions: String = "",
    override var gifUrl: String, // Agrega esto si todos los ejercicios van a tener una URL de GIF
    override var description: String = ""  // Assume description might be needed
) : BaseExercise {
    constructor() : this("", "", "", "", "")
}

// Public exercise class that also implements BaseExercise
data class PublicExercise(
    override val uuid: String,
    override val exerciseName: String,
    override val series: String,
    override val repetitions: String,
    override val description: String,
    val bodyPart: String,
    val equipment: String,
    override var gifUrl: String,
    val instructions: List<String>,
    val target: String,
    val secondaryMuscles: List<String>,
    val public: Boolean,
    val title: String,
    val userUUID: String
) : BaseExercise

