package edu.tfc.activelife.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * ApiService defines the HTTP operations to interact with the exercise API.
 */
interface ApiService {

    /**
     * Fetches a list of exercises from the API.
     *
     * @return A Call object for the list of ExerciseResponse.
     */
    @GET("exercises")
    fun getExercises(): Call<List<ExerciseResponse>>

    /**
     * Fetches a list of body parts from the API.
     *
     * @return A Call object for the list of body parts.
     */
    @GET("exercises/bodyPartList")
    fun getBodyPartList(): Call<List<String>>

    /**
     * Fetches exercises filtered by a specific body part from the API.
     *
     * @param bodyPart The body part to filter exercises by.
     * @return A Call object for the list of ExerciseResponse filtered by body part.
     */
    @GET("exercises/bodyPart/{bodyPart}")
    fun getExercisesByBodyPart(@Path("bodyPart") bodyPart: String): Call<List<ExerciseResponse>>
}