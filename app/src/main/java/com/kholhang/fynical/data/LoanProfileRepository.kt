package com.kholhang.fynical.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

val Context.loanProfileDataStore: DataStore<Preferences> by preferencesDataStore(name = "loan_profile_store")

class LoanProfileRepository(private val context: Context) {
    private val loanProfilesKey = stringPreferencesKey("loan_profiles")
    
    fun getLoanProfilesFlow(): Flow<List<LoanProfile>> {
        return context.loanProfileDataStore.data.map { preferences ->
            val profilesJson = preferences[loanProfilesKey] ?: "[]"
            try {
                val array = JSONArray(profilesJson)
                (0 until array.length()).map { index ->
                    val obj = array.getJSONObject(index)
                    LoanProfile(
                        id = obj.getString("id"),
                        bankName = obj.getString("bankName"),
                        principal = obj.getDouble("principal"),
                        interestRate = obj.getDouble("interestRate"),
                        tenureMonths = obj.getInt("tenureMonths"),
                        startDate = obj.getString("startDate"),
                        loanType = obj.getString("loanType"),
                        emi = obj.getDouble("emi"),
                        totalAmount = obj.getDouble("totalAmount"),
                        totalInterest = obj.getDouble("totalInterest")
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    suspend fun saveLoanProfile(profile: LoanProfile) {
        context.loanProfileDataStore.edit { preferences ->
            val currentProfiles = preferences[loanProfilesKey] ?: "[]"
            val profilesArray = try {
                JSONArray(currentProfiles)
            } catch (e: Exception) {
                JSONArray()
            }
            
            // Remove existing profile with same ID if any
            val profilesList = mutableListOf<JSONObject>()
            for (i in 0 until profilesArray.length()) {
                val obj = profilesArray.getJSONObject(i)
                if (obj.getString("id") != profile.id) {
                    profilesList.add(obj)
                }
            }
            
            // Add new/updated profile
            val profileJson = JSONObject().apply {
                put("id", profile.id)
                put("bankName", profile.bankName)
                put("principal", profile.principal)
                put("interestRate", profile.interestRate)
                put("tenureMonths", profile.tenureMonths)
                put("startDate", profile.startDate)
                put("loanType", profile.loanType)
                put("emi", profile.emi)
                put("totalAmount", profile.totalAmount)
                put("totalInterest", profile.totalInterest)
            }
            profilesList.add(profileJson)
            
            val newArray = JSONArray()
            profilesList.forEach { newArray.put(it) }
            
            preferences[loanProfilesKey] = newArray.toString()
        }
    }
    
    suspend fun deleteLoanProfile(id: String) {
        context.loanProfileDataStore.edit { preferences ->
            val currentProfiles = preferences[loanProfilesKey] ?: "[]"
            val profilesArray = try {
                JSONArray(currentProfiles)
            } catch (e: Exception) {
                JSONArray()
            }
            
            val profilesList = mutableListOf<JSONObject>()
            for (i in 0 until profilesArray.length()) {
                val obj = profilesArray.getJSONObject(i)
                if (obj.getString("id") != id) {
                    profilesList.add(obj)
                }
            }
            
            val newArray = JSONArray()
            profilesList.forEach { newArray.put(it) }
            
            preferences[loanProfilesKey] = newArray.toString()
        }
    }
    
    suspend fun clearAllProfiles() {
        context.loanProfileDataStore.edit { preferences ->
            preferences[loanProfilesKey] = "[]"
        }
    }
}


