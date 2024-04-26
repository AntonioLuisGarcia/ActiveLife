package edu.tfc.activelife.dao

// Define common interface for all exercise types
interface BaseExercise {
    val uuid: String
    val exerciseName: String
    val series: String
    val repetitions: String
    val description: String // Include description if used commonly
}

// Standard exercise class
data class Exercise(
    override var uuid: String = "",
    override var exerciseName: String = "",
    override var series: String = "",
    override var repetitions: String = "",
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
    val gifUrl: String,
    val instructions: List<String>,
    val target: String,
    val secondaryMuscles: List<String>,
    val public: Boolean,
    val title: String,
    val userUUID: String
) : BaseExercise

