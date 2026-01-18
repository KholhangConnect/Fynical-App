package com.kholhang.fynical.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "denomination_store")

data class DenominationItem(
    val id: String,
    val value: Double,
    val quantity: Int,
    val type: DenominationType
) {
    val total: Double
        get() = value * quantity
}

enum class DenominationType {
    RUPEES, COINS
}

class DenominationRepository(private val context: Context) {
    private val notesKey = stringPreferencesKey("notes")
    private val cashKey = stringPreferencesKey("cash")
    
    suspend fun getRupees(): List<DenominationItem> {
        return getDenominations(notesKey, DenominationType.RUPEES)
    }
    
    suspend fun getCoins(): List<DenominationItem> {
        return getDenominations(cashKey, DenominationType.COINS)
    }
    
    companion object {
        val DEFAULT_NOTES = listOf(500.0, 200.0, 100.0, 50.0, 20.0, 10.0, 5.0, 2.0, 1.0)
        val DEFAULT_COINS = listOf(20.0, 10.0, 5.0, 2.0, 1.0, 0.01)
    }
    
    fun getRupeesFlow(): Flow<List<DenominationItem>> {
        return context.dataStore.data.map { preferences ->
            val stored = getDenominationsFromJson(preferences[notesKey] ?: "[]", DenominationType.RUPEES)
            mergeWithDefaults(stored, DEFAULT_NOTES, DenominationType.RUPEES)
        }
    }
    
    fun getCoinsFlow(): Flow<List<DenominationItem>> {
        return context.dataStore.data.map { preferences ->
            val stored = getDenominationsFromJson(preferences[cashKey] ?: "[]", DenominationType.COINS)
            mergeWithDefaults(stored, DEFAULT_COINS, DenominationType.COINS)
        }
    }
    
    private fun mergeWithDefaults(
        stored: List<DenominationItem>,
        defaults: List<Double>,
        type: DenominationType
    ): List<DenominationItem> {
        val result = mutableListOf<DenominationItem>()
        val storedMap = stored.associateBy { it.value }
        
        defaults.forEach { value ->
            val storedItem = storedMap[value]
            result.add(
                storedItem ?: DenominationItem(
                    id = "default_${type.name}_$value",
                    value = value,
                    quantity = 0,
                    type = type
                )
            )
        }
        
        // Add any stored items that aren't in defaults
        stored.forEach { item ->
            if (!defaults.contains(item.value)) {
                result.add(item)
            }
        }
        
        return result.sortedByDescending { it.value }
    }
    
    suspend fun saveRupees(denominations: List<DenominationItem>) {
        saveDenominations(notesKey, denominations)
    }
    
    suspend fun saveCoins(denominations: List<DenominationItem>) {
        saveDenominations(cashKey, denominations)
    }
    
    suspend fun addRupee(item: DenominationItem) {
        val rupees = getRupees().toMutableList()
        val existingIndex = rupees.indexOfFirst { it.value == item.value }
        if (existingIndex >= 0) {
            rupees[existingIndex] = rupees[existingIndex].copy(quantity = rupees[existingIndex].quantity + item.quantity)
        } else {
            rupees.add(item)
        }
        saveRupees(rupees)
    }
    
    suspend fun addCoin(item: DenominationItem) {
        val coins = getCoins().toMutableList()
        val existingIndex = coins.indexOfFirst { it.value == item.value }
        if (existingIndex >= 0) {
            coins[existingIndex] = coins[existingIndex].copy(quantity = coins[existingIndex].quantity + item.quantity)
        } else {
            coins.add(item)
        }
        saveCoins(coins)
    }
    
    suspend fun updateRupee(id: String, value: Double, quantity: Int) {
        val stored = getRupees().toMutableList()
        val index = stored.indexOfFirst { it.id == id }
        
        if (index >= 0) {
            // Update existing item
            stored[index] = stored[index].copy(quantity = maxOf(0, quantity))
        } else {
            // Item not in stored list (default item), add it
            stored.add(DenominationItem(
                id = id,
                value = value,
                quantity = maxOf(0, quantity),
                type = DenominationType.RUPEES
            ))
        }
        saveRupees(stored)
    }
    
    suspend fun updateCoin(id: String, value: Double, quantity: Int) {
        val stored = getCoins().toMutableList()
        val index = stored.indexOfFirst { it.id == id }
        
        if (index >= 0) {
            // Update existing item
            stored[index] = stored[index].copy(quantity = maxOf(0, quantity))
        } else {
            // Item not in stored list (default item), add it
            stored.add(DenominationItem(
                id = id,
                value = value,
                quantity = maxOf(0, quantity),
                type = DenominationType.COINS
            ))
        }
        saveCoins(stored)
    }
    
    suspend fun deleteRupee(id: String) {
        val rupees = getRupees().toMutableList()
        rupees.removeAll { it.id == id }
        saveRupees(rupees)
    }
    
    suspend fun deleteCoin(id: String) {
        val coins = getCoins().toMutableList()
        coins.removeAll { it.id == id }
        saveCoins(coins)
    }
    
    private suspend fun getDenominations(key: Preferences.Key<String>, type: DenominationType): List<DenominationItem> {
        val json = context.dataStore.data.first()[key] ?: "[]"
        return getDenominationsFromJson(json, type)
    }
    
    private fun getDenominationsFromJson(json: String, type: DenominationType): List<DenominationItem> {
        return try {
            val jsonArray = JSONArray(json)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                DenominationItem(
                    id = obj.getString("id"),
                    value = obj.getDouble("value"),
                    quantity = obj.getInt("quantity"),
                    type = type
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun saveDenominations(key: Preferences.Key<String>, denominations: List<DenominationItem>) {
        context.dataStore.edit { preferences ->
            val jsonArray = JSONArray()
            // Save all items including those with 0 quantity (for default list)
            denominations.forEach { item ->
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("value", item.value)
                obj.put("quantity", item.quantity)
                jsonArray.put(obj)
            }
            preferences[key] = jsonArray.toString()
        }
    }
}

