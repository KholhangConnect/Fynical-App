package com.kholhang.fynical.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApiService {
    @GET("latest/USD")
    suspend fun getLatestRates(): CurrencyResponse
    
    companion object {
        private const val BASE_URL = "https://api.exchangerate-api.com/v4/"
        
        fun create(): CurrencyApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CurrencyApiService::class.java)
        }
    }
}

data class CurrencyResponse(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)

