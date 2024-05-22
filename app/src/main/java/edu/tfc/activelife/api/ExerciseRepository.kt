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

class ExerciseRepository private constructor(private val api: ApiService) {

    private val _exercises = MutableLiveData<List<ExerciseResponse>>()
    val exercises: LiveData<List<ExerciseResponse>>
        get() = _exercises

    private val _bodyParts = MutableLiveData<List<String>>()
    val bodyParts: LiveData<List<String>>
        get() = _bodyParts

    companion object {
        private var INSTANCE: ExerciseRepository? = null

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

    fun fetchExercises() {
        api.getExercises().enqueue(object : Callback<List<ExerciseResponse>> {
            override fun onResponse(call: Call<List<ExerciseResponse>>, response: Response<List<ExerciseResponse>>) {
                if (response.isSuccessful) {
                    _exercises.postValue(response.body())
                    response.body()?.forEach {
                        Log.d("ExerciseRepository", it.toString())
                    }
                } else {
                    Log.e("ExerciseRepository", "Error en la respuesta de la API")
                }
            }

            override fun onFailure(call: Call<List<ExerciseResponse>>, t: Throwable) {
                Log.e("ExerciseRepository", "Error en la llamada a la API", t)
            }
        })
    }

    fun fetchBodyParts() {
        api.getBodyPartList().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    _bodyParts.postValue(response.body())
                    response.body()?.forEach {
                        Log.d("ExerciseRepository", it.toString())
                    }
                } else {
                    Log.e("ExerciseRepository", "Error en la respuesta de la API")
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.e("ExerciseRepository", "Error en la llamada a la API", t)
            }
        })
    }

    fun fetchExercisesByBodyPart(bodyPart: String) {
        api.getExercisesByBodyPart(bodyPart).enqueue(object : Callback<List<ExerciseResponse>> {
            override fun onResponse(call: Call<List<ExerciseResponse>>, response: Response<List<ExerciseResponse>>) {
                if (response.isSuccessful) {
                    _exercises.postValue(response.body())
                    response.body()?.forEach {
                        Log.d("ExerciseRepository", it.toString())
                    }
                } else {
                    Log.e("ExerciseRepository", "Error en la respuesta de la API")
                }
            }

            override fun onFailure(call: Call<List<ExerciseResponse>>, t: Throwable) {
                Log.e("ExerciseRepository", "Error en la llamada a la API", t)
            }
        })
    }
}
