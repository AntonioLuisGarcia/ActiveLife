package edu.tfc.activelife.api

/**
 * Data class representing the response for an exercise.
 *
 * @property id The unique identifier for the exercise.
 * @property name The name of the exercise.
 * @property bodyPart The body part targeted by the exercise.
 * @property equipment The equipment required for the exercise.
 * @property gifUrl The URL for the exercise GIF.
 * @property target The primary muscle targeted by the exercise.
 * @property secondaryMuscles The secondary muscles targeted by the exercise.
 * @property instructions The list of instructions for performing the exercise.
 */
data class ExerciseResponse(
    val id: String,
    val name: String,
    val bodyPart: String,
    val equipment: String,
    val gifUrl: String,
    val target: String,
    val secondaryMuscles: List<String>,
    val instructions: List<String>
)
