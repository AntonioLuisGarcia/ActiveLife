package edu.tfc.activelife.api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Repository class for fetching exercise data from the API.
 * This class uses Retrofit to make network requests and LiveData to hold the data.
 */
class ExerciseRepository private constructor(private val api: ApiService) {

    // LiveData to hold the list of exercises
    private val _exercises = MutableLiveData<List<ExerciseResponse>>()
    val exercises: LiveData<List<ExerciseResponse>>
        get() = _exercises

    // LiveData to hold the list of body parts
    private val _bodyParts = MutableLiveData<List<String>>()
    val bodyParts: LiveData<List<String>>
        get() = _bodyParts

    companion object {
        private var INSTANCE: ExerciseRepository? = null

        /**
         * Returns the singleton instance of the repository.
         * Initializes the Retrofit instance and API service if not already done.
         *
         * @return The singleton instance of ExerciseRepository
         */
        fun getInstance(): ExerciseRepository {
            if (INSTANCE == null) {
                val client = OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request: Request = chain.request()
                            .newBuilder()
                            .addHeader("X-RapidAPI-Key", "1e73d1c1cbmsh1dd3dcd4b01213cp18cbb5jsnd42486a571fe")
                            .addHeader("X-RapidAPI-Host", "exercisedb.p.rapidapi.com")
                            .build()
                        chain.proceed(request)
                    }
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl("https://exercisedb.p.rapidapi.com/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(ApiService::class.java)
                INSTANCE = ExerciseRepository(api)
            }
            return INSTANCE!!
        }
    }

    /**
     * Fetches the list of exercises from the API.
     * Updates the _exercises LiveData with the fetched data.
     */
    fun fetchExercises() {
        api.getExercises().enqueue(object : Callback<List<ExerciseResponse>> {
            override fun onResponse(call: Call<List<ExerciseResponse>>, response: Response<List<ExerciseResponse>>) {
                if (response.isSuccessful) {
                    _exercises.postValue(response.body())
                    response.body()?.forEach {
                        Log.d("ExerciseRepository", it.toString())
                    }
                } else {
                    Log.e("ExerciseRepository", "Error in API response")
                }
            }

            override fun onFailure(call: Call<List<ExerciseResponse>>, t: Throwable) {
                Log.e("ExerciseRepository", "API call failed", t)
            }
        })
    }

    /**
     * Fetches the list of body parts from the API.
     * Updates the _bodyParts LiveData with the fetched data.
     */
    fun fetchBodyParts() {
        api.getBodyPartList().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    _bodyParts.postValue(response.body())
                    response.body()?.forEach {
                        Log.d("ExerciseRepository", it.toString())
                    }
                } else {
                    Log.e("ExerciseRepository", "Error in API response")
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e("ExerciseRepository", "API call failed", t)
            }
        })
    }

    /**
     * Fetches the list of exercises by body part from the API.
     * Updates the _exercises LiveData with the fetched data.
     *
     * @param bodyPart The body part to fetch exercises for
     */
    fun fetchExercisesByBodyPart(bodyPart: String) {
        api.getExercisesByBodyPart(bodyPart).enqueue(object : Callback<List<ExerciseResponse>> {
            override fun onResponse(call: Call<List<ExerciseResponse>>, response: Response<List<ExerciseResponse>>) {
                if (response.isSuccessful) {
                    _exercises.postValue(response.body())
                    response.body()?.forEach {
                        Log.d("ExerciseRepository", it.toString())
                    }
                } else {
                    Log.e("ExerciseRepository", "Error in API response")
                }
            }

            override fun onFailure(call: Call<List<ExerciseResponse>>, t: Throwable) {
                Log.e("ExerciseRepository", "API call failed", t)
            }
        })
    }
}