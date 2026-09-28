package com.rifqi.uangkas.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
// Request Payload
data class KasRequest(
    val name: String,
    val amount: Int,
    val weekNumber: String?,
    val type: String // "pemasukan" atau "pengeluaran"
)

// Response Payload
data class KasResponse(
    val status: String,
    val message: String,
    val totalKasSekarang: Double?
)

// Retrofit Interface
interface GasApiService {
    // GANTI DENGAN URL WEB APP ANDA (Jangan sertakan https://script.google.com/)
    // Cukup endpoint-nya saja karena base URL diatur di Retrofit Builder
    @POST("YOUR_SCRIPT_ENDPOINT")
    suspend fun submitKas(@Body request: KasRequest): KasResponse
}

// Retrofit Object Builder
object RetrofitClient {
    val apiService: GasApiService by lazy {
        Retrofit.Builder()
            // Gunakan BuildConfig untuk BASE_URL
            .baseUrl("https://script.google.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GasApiService::class.java)
    }
}