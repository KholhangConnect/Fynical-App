package com.kholhang.fynical.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_store")
val Context.historyDataStore: DataStore<Preferences> by preferencesDataStore(name = "history_store")
val Context.denominationHistoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "denomination_history_store")

data class AppSettings(
    val isDarkMode: Boolean,
    val keepScreenOn: Boolean,
    val hiddenDenominations: Set<String>, // Set of denomination IDs that are hidden
    val cardSizeMultiplier: Float, // Multiplier for card size (0.8 to 1.5)
    val textSizeMultiplier: Float, // Multiplier for text size (0.8 to 1.5)
    val cardPadding: Float, // Padding multiplier (0.5 to 2.0)
    val cardSpacing: Float // Spacing between cards in dp (0 to 24)
)

data class CashBookHistoryEntry(
    val id: String,
    val date: String,
    val openingBalance: Double,
    val totalDeposit: Double,
    val totalPayment: Double,
    val totalCashInHand: Double,
    val closingBalance: Double,
    val notesTotal: Double,
    val coinsTotal: Double,
    val grandTotal: Double
)

class SettingsRepository(private val context: Context) {
    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val keepScreenOnKey = booleanPreferencesKey("keep_screen_on")
    private val hiddenDenominationsKey = stringPreferencesKey("hidden_denominations")
    private val cardSizeKey = floatPreferencesKey("card_size_multiplier")
    private val textSizeKey = floatPreferencesKey("text_size_multiplier")
    private val cardPaddingKey = floatPreferencesKey("card_padding_multiplier")
    private val cardSpacingKey = floatPreferencesKey("card_spacing")
    
    fun getSettingsFlow(): Flow<AppSettings> {
        return context.settingsDataStore.data.map { preferences ->
            AppSettings(
                isDarkMode = preferences[darkModeKey] ?: false,
                keepScreenOn = preferences[keepScreenOnKey] ?: false,
                hiddenDenominations = preferences[hiddenDenominationsKey]?.let { json ->
                    try {
                        JSONArray(json).let { array ->
                            (0 until array.length()).map { array.getString(it) }.toSet()
                        }
                    } catch (e: Exception) {
                        emptySet()
                    }
                } ?: emptySet(),
                cardSizeMultiplier = preferences[cardSizeKey] ?: 1.0f,
                textSizeMultiplier = preferences[textSizeKey] ?: 1.0f,
                cardPadding = preferences[cardPaddingKey] ?: 1.0f,
                cardSpacing = preferences[cardSpacingKey] ?: 6.0f
            )
        }
    }
    
    suspend fun getSettings(): AppSettings {
        return getSettingsFlow().first()
    }
    
    suspend fun setDarkMode(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[darkModeKey] = enabled
        }
    }
    
    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[keepScreenOnKey] = enabled
        }
    }
    
    suspend fun setHiddenDenominations(ids: Set<String>) {
        context.settingsDataStore.edit { preferences ->
            preferences[hiddenDenominationsKey] = JSONArray(ids.toList()).toString()
        }
    }
    
    suspend fun toggleDenominationVisibility(id: String) {
        val current = getSettings()
        val newSet = if (current.hiddenDenominations.contains(id)) {
            current.hiddenDenominations - id
        } else {
            current.hiddenDenominations + id
        }
        setHiddenDenominations(newSet)
    }
    
    suspend fun setCardSizeMultiplier(multiplier: Float) {
        context.settingsDataStore.edit { preferences ->
            preferences[cardSizeKey] = multiplier.coerceIn(0.5f, 2.0f)
        }
    }
    
    suspend fun setTextSizeMultiplier(multiplier: Float) {
        context.settingsDataStore.edit { preferences ->
            preferences[textSizeKey] = multiplier.coerceIn(0.5f, 2.0f)
        }
    }
    
    suspend fun setCardPadding(multiplier: Float) {
        context.settingsDataStore.edit { preferences ->
            preferences[cardPaddingKey] = multiplier.coerceIn(0.3f, 2.5f)
        }
    }
    
    suspend fun setCardSpacing(spacing: Float) {
        context.settingsDataStore.edit { preferences ->
            preferences[cardSpacingKey] = spacing.coerceIn(0f, 24f)
        }
    }
}

class HistoryRepository(private val context: Context) {
    private val historyKey = stringPreferencesKey("cash_book_history")
    
    suspend fun addHistoryEntry(entry: CashBookHistoryEntry) {
        context.historyDataStore.edit { preferences ->
            val currentHistory = preferences[historyKey] ?: "[]"
            val historyArray = try {
                JSONArray(currentHistory)
            } catch (e: Exception) {
                JSONArray()
            }
            
            val entryJson = org.json.JSONObject().apply {
                put("id", entry.id)
                put("date", entry.date)
                put("openingBalance", entry.openingBalance)
                put("totalDeposit", entry.totalDeposit)
                put("totalPayment", entry.totalPayment)
                put("totalCashInHand", entry.totalCashInHand)
                put("closingBalance", entry.closingBalance)
                put("notesTotal", entry.notesTotal)
                put("coinsTotal", entry.coinsTotal)
                put("grandTotal", entry.grandTotal)
            }
            
            historyArray.put(entryJson)
            
            // Keep only last 100 entries
            val entriesList = mutableListOf<org.json.JSONObject>()
            for (i in 0 until historyArray.length()) {
                entriesList.add(historyArray.getJSONObject(i))
            }
            if (entriesList.size > 100) {
                entriesList.removeAt(0)
            }
            
            val newArray = JSONArray()
            entriesList.forEach { newArray.put(it) }
            
            preferences[historyKey] = newArray.toString()
        }
    }
    
    fun getHistoryFlow(): Flow<List<CashBookHistoryEntry>> {
        return context.historyDataStore.data.map { preferences ->
            val historyJson = preferences[historyKey] ?: "[]"
            try {
                val array = JSONArray(historyJson)
                (0 until array.length()).map { index ->
                    val obj = array.getJSONObject(index)
                    CashBookHistoryEntry(
                        id = obj.getString("id"),
                        date = obj.getString("date"),
                        openingBalance = obj.getDouble("openingBalance"),
                        totalDeposit = obj.getDouble("totalDeposit"),
                        totalPayment = obj.getDouble("totalPayment"),
                        totalCashInHand = obj.getDouble("totalCashInHand"),
                        closingBalance = obj.getDouble("closingBalance"),
                        notesTotal = obj.getDouble("notesTotal"),
                        coinsTotal = obj.getDouble("coinsTotal"),
                        grandTotal = obj.getDouble("grandTotal")
                    )
                }.reversed() // Most recent first
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    suspend fun clearHistory() {
        context.historyDataStore.edit { preferences ->
            preferences[historyKey] = "[]"
        }
    }
    
    suspend fun deleteHistoryEntry(id: String) {
        context.historyDataStore.edit { preferences ->
            val currentHistory = preferences[historyKey] ?: "[]"
            val historyArray = try {
                JSONArray(currentHistory)
            } catch (e: Exception) {
                JSONArray()
            }
            
            val entriesList = mutableListOf<org.json.JSONObject>()
            for (i in 0 until historyArray.length()) {
                val entry = historyArray.getJSONObject(i)
                if (entry.getString("id") != id) {
                    entriesList.add(entry)
                }
            }
            
            val newArray = JSONArray()
            entriesList.forEach { newArray.put(it) }
            
            preferences[historyKey] = newArray.toString()
        }
    }
}

data class DenominationSnapshot(
    val id: String,
    val date: String,
    val notes: List<DenominationItem>,
    val coins: List<DenominationItem>,
    val notesTotal: Double,
    val coinsTotal: Double,
    val grandTotal: Double
)

class DenominationHistoryRepository(private val context: Context) {
    private val historyKey = stringPreferencesKey("denomination_history")
    
    suspend fun saveSnapshot(snapshot: DenominationSnapshot) {
        context.denominationHistoryDataStore.edit { preferences ->
            val currentHistory = preferences[historyKey] ?: "[]"
            val historyArray = try {
                JSONArray(currentHistory)
            } catch (e: Exception) {
                JSONArray()
            }
            
            val snapshotJson = org.json.JSONObject().apply {
                put("id", snapshot.id)
                put("date", snapshot.date)
                put("notesTotal", snapshot.notesTotal)
                put("coinsTotal", snapshot.coinsTotal)
                put("grandTotal", snapshot.grandTotal)
                
                val notesArray = JSONArray()
                snapshot.notes.forEach { note ->
                    val noteObj = org.json.JSONObject().apply {
                        put("id", note.id)
                        put("value", note.value)
                        put("quantity", note.quantity)
                    }
                    notesArray.put(noteObj)
                }
                put("notes", notesArray)
                
                val coinsArray = JSONArray()
                snapshot.coins.forEach { coin ->
                    val coinObj = org.json.JSONObject().apply {
                        put("id", coin.id)
                        put("value", coin.value)
                        put("quantity", coin.quantity)
                    }
                    coinsArray.put(coinObj)
                }
                put("coins", coinsArray)
            }
            
            historyArray.put(snapshotJson)
            
            // Keep only last 100 entries
            val entriesList = mutableListOf<org.json.JSONObject>()
            for (i in 0 until historyArray.length()) {
                entriesList.add(historyArray.getJSONObject(i))
            }
            if (entriesList.size > 100) {
                entriesList.removeAt(0)
            }
            
            val newArray = JSONArray()
            entriesList.forEach { newArray.put(it) }
            
            preferences[historyKey] = newArray.toString()
        }
    }
    
    fun getSnapshotsFlow(): Flow<List<DenominationSnapshot>> {
        return context.denominationHistoryDataStore.data.map { preferences ->
            val historyJson = preferences[historyKey] ?: "[]"
            try {
                val array = JSONArray(historyJson)
                (0 until array.length()).map { index ->
                    val obj = array.getJSONObject(index)
                    val notesArray = obj.getJSONArray("notes")
                    val notes = (0 until notesArray.length()).map { i ->
                        val noteObj = notesArray.getJSONObject(i)
                        DenominationItem(
                            id = noteObj.getString("id"),
                            value = noteObj.getDouble("value"),
                            quantity = noteObj.getInt("quantity"),
                            type = DenominationType.RUPEES
                        )
                    }
                    
                    val coinsArray = obj.getJSONArray("coins")
                    val coins = (0 until coinsArray.length()).map { i ->
                        val coinObj = coinsArray.getJSONObject(i)
                        DenominationItem(
                            id = coinObj.getString("id"),
                            value = coinObj.getDouble("value"),
                            quantity = coinObj.getInt("quantity"),
                            type = DenominationType.COINS
                        )
                    }
                    
                    DenominationSnapshot(
                        id = obj.getString("id"),
                        date = obj.getString("date"),
                        notes = notes,
                        coins = coins,
                        notesTotal = obj.getDouble("notesTotal"),
                        coinsTotal = obj.getDouble("coinsTotal"),
                        grandTotal = obj.getDouble("grandTotal")
                    )
                }.reversed() // Most recent first
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    suspend fun deleteSnapshot(id: String) {
        context.denominationHistoryDataStore.edit { preferences ->
            val currentHistory = preferences[historyKey] ?: "[]"
            val historyArray = try {
                JSONArray(currentHistory)
            } catch (e: Exception) {
                JSONArray()
            }
            
            val entriesList = mutableListOf<org.json.JSONObject>()
            for (i in 0 until historyArray.length()) {
                val entry = historyArray.getJSONObject(i)
                if (entry.getString("id") != id) {
                    entriesList.add(entry)
                }
            }
            
            val newArray = JSONArray()
            entriesList.forEach { newArray.put(it) }
            
            preferences[historyKey] = newArray.toString()
        }
    }
    
    suspend fun clearHistory() {
        context.denominationHistoryDataStore.edit { preferences ->
            preferences[historyKey] = "[]"
        }
    }
}

