package com.rifqi.uangkas.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
// Request Payload
data class KasRequest(
    val name: String,
    val amount: Int,
    val weekNumber: String?,
    val type: String, // "pemasukan" atau "pengeluaran"
    val description: String?
    )

// Response Payload
data class KasResponse(
    val status: String,
    val message: String,
    val totalKasSekarang: Double?
)

// Retrofit Interface
interface GasApiService {
    // YOUR_ENDPOINT
    @POST("YOUR_ENDPOINT")
    suspend fun submitKas(@Body request: KasRequest): KasResponse

    @GET("YOUR_ENDPOINT")
    suspend fun getTotalKas(): KasResponse
}

// Retrofit Object Builder
object RetrofitClient {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val apiService: GasApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://script.google.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GasApiService::class.java)
    }
}