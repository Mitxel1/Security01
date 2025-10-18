package mx.edu.utng.arg.security01.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://192.168.100.10:3000/api/"
    private const val TAG = "RetrofitClient"

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        val filteredMessage = filterSensitiveData(message)
        Log.d(TAG, filteredMessage)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    private fun filterSensitiveData(message: String): String {
        var filtered = message

        if (filtered.contains("password")) {
            filtered = filtered.replace(
                Regex("\"password\"\\s*:\\s*\"[^\"]*\""),
                "\"password\":\"***HIDDEN***\""
            )
        }

        if (filtered.contains("Authorization")) {
            filtered = filtered.replace(
                Regex("Bearer [A-Za-z0-9._-]+"),
                "Bearer ****"
            )
        }

        if (filtered.contains("\"token\"")) {
            filtered = filtered.replace(
                Regex("\"token\"\\s*:\\s*\"([^\"]{4})[^\"]*([^\"]{4})\""),
                "\"token\":\"$1****$2\""
            )
        }

        return filtered
    }
}
