package edu.tfc.activelife.api

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("exercises")
    fun getExercises(): Call<List<ExerciseResponse>>
}
