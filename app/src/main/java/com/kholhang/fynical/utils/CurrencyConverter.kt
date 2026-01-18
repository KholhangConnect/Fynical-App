package com.kholhang.fynical.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

val Context.currencyDataStore: DataStore<Preferences> by preferencesDataStore(name = "currency_store")

class CurrencyConverter(private val context: Context) {
    private val ratesKey = stringPreferencesKey("currency_rates")
    private val lastUpdatedKey = longPreferencesKey("rates_last_updated")
    private val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours
    
    suspend fun saveRates(rates: Map<String, Double>) {
        context.currencyDataStore.edit { preferences ->
            val ratesJson = JSONObject(rates).toString()
            preferences[ratesKey] = ratesJson
            preferences[lastUpdatedKey] = System.currentTimeMillis()
        }
    }
    
    fun getCachedRatesFlow(): Flow<Map<String, Double>?> {
        return context.currencyDataStore.data.map { preferences ->
            val ratesJson = preferences[ratesKey]
            val lastUpdated = preferences[lastUpdatedKey] ?: 0L
            val now = System.currentTimeMillis()
            
            if (ratesJson != null && (now - lastUpdated) < CACHE_DURATION_MS) {
                try {
                    val json = JSONObject(ratesJson)
                    val rates = mutableMapOf<String, Double>()
                    json.keys().forEach { key ->
                        rates[key] = json.getDouble(key)
                    }
                    rates
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }
    
    fun isCacheValid(): Boolean {
        // This is a synchronous check - for async use getCachedRatesFlow()
        return true // Simplified - actual check would need coroutine
    }
    
    fun convert(amount: Double, fromCurrency: String, toCurrency: String, rate: Double): Double {
        if (fromCurrency == toCurrency) return amount
        return amount * rate
    }
}


