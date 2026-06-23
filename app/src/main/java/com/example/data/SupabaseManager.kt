package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.SupabaseClient

object SupabaseManager {
    val client: SupabaseClient? by lazy {
        if (!isConfigured) null else {
            try {
                createSupabaseClient(
                    supabaseUrl = BuildConfig.SUPABASE_URL,
                    supabaseKey = BuildConfig.SUPABASE_ANON_KEY
                ) {
                    install(Postgrest)
                    install(Auth)
                }
            } catch (e: Exception) {
                Log.e("SupabaseManager", "Error creating Supabase Client", e)
                null
            }
        }
    }

    val isConfigured: Boolean by lazy {
        val url = BuildConfig.SUPABASE_URL
        val key = BuildConfig.SUPABASE_ANON_KEY
        url.isNotBlank() && 
        url != "https://your-project.supabase.co" && 
        url != "YOUR_SUPABASE_URL" &&
        key.isNotBlank() && 
        key != "your_anon_key_here" && 
        key != "YOUR_SUPABASE_ANON_KEY"
    }

    suspend fun insertOrUpdate(tableName: String, jsonPayload: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            Log.d("SupabaseManager", "[Local Fallback Mode] Sync skipped for table '$tableName' (Supabase URL/Key not configured in Secrets)")
            return@withContext false
        }
        try {
            val urlStr = "${BuildConfig.SUPABASE_URL}/rest/v1/$tableName"
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
            connection.setRequestProperty("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
            connection.setRequestProperty("Content-Type", "application/json")
            // Upsert on primary key constraint instead of duplicate error
            connection.setRequestProperty("Prefer", "resolution=merge-duplicates")
            connection.doOutput = true
            
            // Format as single item or list
            val payload = if (jsonPayload.trim().startsWith("[")) jsonPayload else "[$jsonPayload]"
            
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(payload)
                writer.flush()
            }
            
            val responseCode = connection.responseCode
            Log.d("SupabaseManager", "Supabase REST Sync on '$tableName' -> Response HTTP $responseCode")
            connection.disconnect()
            responseCode in 200..299
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Supabase sync exception on table '$tableName': ${e.message}", e)
            false
        }
    }

    suspend fun delete(tableName: String, id: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            Log.d("SupabaseManager", "[Local Fallback Mode] Deletion skipped for table '$tableName' (Supabase URL/Key not configured)")
            return@withContext false
        }
        try {
            val urlStr = "${BuildConfig.SUPABASE_URL}/rest/v1/$tableName?id=eq.$id"
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "DELETE"
            connection.setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
            connection.setRequestProperty("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
            val responseCode = connection.responseCode
            Log.d("SupabaseManager", "Supabase REST Delete on '$tableName' (id=$id) -> Response HTTP $responseCode")
            connection.disconnect()
            responseCode in 200..299
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Supabase delete exception on table '$tableName': ${e.message}", e)
            false
        }
    }


    suspend fun fetchDoctors(): List<DoctorEntity> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            Log.d("SupabaseManager", "[Local Fallback Mode] Fetch skipped (Supabase URL/Key not configured in Secrets)")
            return@withContext emptyList()
        }
        val doctorsList = mutableListOf<DoctorEntity>()
        try {
            val urlStr = "${BuildConfig.SUPABASE_URL}/rest/v1/doctors"
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
            connection.setRequestProperty("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
            
            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d("SupabaseManager", "Supabase doctors fetch response: $responseText")
                val jsonArray = org.json.JSONArray(responseText)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optString("id", java.util.UUID.randomUUID().toString())
                    val name = obj.optString("name", "")
                    val specialization = obj.optString("specialization", "")
                    val experience = obj.optInt("experience", 5)
                    val fee = obj.optDouble("fee", 500.0)
                    val rating = obj.optDouble("rating", 4.5)
                    val pharmacyId = if (obj.has("pharmacy_id")) obj.optString("pharmacy_id") else obj.optString("pharmacyId", "")
                    val bannerName = if (obj.has("banner_name")) obj.optString("banner_name") else obj.optString("bannerName", "doctor_male_1")
                    val slotsJson = if (obj.has("slots_json")) obj.optString("slots_json") else obj.optString("slotsJson", "09:00 AM, 11:30 AM, 03:00 PM")
                    val isEnabled = if (obj.has("is_enabled")) obj.optBoolean("is_enabled", true) else obj.optBoolean("isEnabled", true)
                    
                    doctorsList.add(
                        DoctorEntity(
                            id = id,
                            name = name,
                            specialization = specialization,
                            experience = experience,
                            fee = fee,
                            rating = rating,
                            pharmacyId = pharmacyId,
                            bannerName = bannerName,
                            slotsJson = slotsJson,
                            isEnabled = isEnabled
                        )
                    )
                }
            } else {
                Log.e("SupabaseManager", "Failed to fetch doctors from Supabase: Res Code $responseCode")
            }
            connection.disconnect()
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Error fetching doctors from Supabase: ${e.message}", e)
        }
        doctorsList
    }
}
