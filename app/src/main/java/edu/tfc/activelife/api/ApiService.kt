package edu.tfc.activelife.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("exercises")
    fun getExercises(): Call<List<ExerciseResponse>>

    @GET("exercises/bodyPartList")
    fun getBodyPartList(): Call<List<String>>

    @GET("exercises/bodyPart/{bodyPart}")
    fun getExercisesByBodyPart(@Path("bodyPart") bodyPart: String): Call<List<ExerciseResponse>>
}
