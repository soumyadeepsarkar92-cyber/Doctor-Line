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
                    install(Auth) {
                        scheme = "io.supabase.flutter"
                        host = "login-callback"
                    }
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

    suspend fun deleteByColumn(tableName: String, columnName: String, value: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            Log.d("SupabaseManager", "[Local Fallback Mode] Deletion by column skipped for table '$tableName' ($columnName=$value)")
            return@withContext false
        }
        try {
            val urlStr = "${BuildConfig.SUPABASE_URL}/rest/v1/$tableName?$columnName=eq.$value"
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "DELETE"
            connection.setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
            connection.setRequestProperty("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
            val responseCode = connection.responseCode
            Log.d("SupabaseManager", "Supabase REST Delete on '$tableName' ($columnName=$value) -> Response HTTP $responseCode")
            connection.disconnect()
            responseCode in 200..299
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Supabase delete by column exception on table '$tableName': ${e.message}", e)
            false
        }
    }

    suspend fun deleteByQuery(tableName: String, query: String): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            Log.d("SupabaseManager", "[Local Fallback Mode] Deletion by query skipped for table '$tableName' ($query)")
            return@withContext false
        }
        try {
            val urlStr = "${BuildConfig.SUPABASE_URL}/rest/v1/$tableName?$query"
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "DELETE"
            connection.setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
            connection.setRequestProperty("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
            val responseCode = connection.responseCode
            Log.d("SupabaseManager", "Supabase REST Delete on '$tableName' query ($query) -> Response HTTP $responseCode")
            connection.disconnect()
            responseCode in 200..299
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Supabase delete by query exception on table '$tableName': ${e.message}", e)
            false
        }
    }


    suspend fun fetchDoctors(limit: Int = 100, offset: Int = 0): List<DoctorEntity> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            Log.d("SupabaseManager", "[Local Fallback Mode] Fetch skipped (Supabase URL/Key not configured in Secrets)")
            return@withContext emptyList()
        }
        val doctorsList = mutableListOf<DoctorEntity>()
        try {
            val urlStr = "${BuildConfig.SUPABASE_URL}/rest/v1/doctors?limit=$limit&offset=$offset"
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
                    val isSoftDeleted = if (obj.has("is_soft_deleted")) obj.optBoolean("is_soft_deleted", false) else false
                    val availabilityStatus = if (obj.has("availability_status")) obj.optString("availability_status", "Available") else "Available"
                    val expectedStartTime = if (obj.has("expected_start_time")) obj.optString("expected_start_time", "") else ""
                    val delayReason = if (obj.has("delay_reason")) obj.optString("delay_reason", "") else ""
                    
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
                            isEnabled = isEnabled,
                            isSoftDeleted = isSoftDeleted,
                            availabilityStatus = availabilityStatus,
                            expectedStartTime = expectedStartTime,
                            delayReason = delayReason
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

    suspend fun fetchFavourites(patientId: String, limit: Int = 100, offset: Int = 0): List<FavouriteDoctorEntity> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            Log.d("SupabaseManager", "[Local Fallback Mode] Fetch favourites skipped (Supabase URL/Key not configured)")
            return@withContext emptyList()
        }
        val favouritesList = mutableListOf<FavouriteDoctorEntity>()
        try {
            val urlStr = "${BuildConfig.SUPABASE_URL}/rest/v1/favourite_doctors?patient_id=eq.$patientId&limit=$limit&offset=$offset"
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
            connection.setRequestProperty("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
            
            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d("SupabaseManager", "Supabase favourites fetch response: $responseText")
                val jsonArray = org.json.JSONArray(responseText)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optString("id", java.util.UUID.randomUUID().toString())
                    val pId = obj.optString("patient_id", "")
                    val dId = obj.optString("doctor_id", "")
                    val createdAt = obj.optLong("created_at", System.currentTimeMillis())
                    
                    favouritesList.add(
                        FavouriteDoctorEntity(
                            id = id,
                            patientId = pId,
                            doctorId = dId,
                            createdAt = createdAt
                        )
                    )
                }
            } else {
                Log.e("SupabaseManager", "Failed to fetch favourites from Supabase: Res Code $responseCode")
            }
            connection.disconnect()
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Error fetching favourites from Supabase: ${e.message}", e)
        }
        favouritesList
    }

    suspend fun fetchPharmacies(limit: Int = 100, offset: Int = 0): List<PharmacyEntity> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            Log.d("SupabaseManager", "[Local Fallback Mode] Fetch pharmacies skipped (Supabase URL/Key not configured)")
            return@withContext emptyList()
        }
        val list = mutableListOf<PharmacyEntity>()
        try {
            val urlStr = "${BuildConfig.SUPABASE_URL}/rest/v1/pharmacies?limit=$limit&offset=$offset"
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
            connection.setRequestProperty("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
            
            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d("SupabaseManager", "Supabase pharmacies fetch response: $responseText")
                val jsonArray = org.json.JSONArray(responseText)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optString("id", java.util.UUID.randomUUID().toString())
                    val name = if (obj.has("name")) obj.optString("name", "") else obj.optString("pharmacy_name", "")
                    val address = obj.optString("address", "")
                    val phone = if (obj.has("phone")) obj.optString("phone", "") else obj.optString("mobile", "")
                    val license = obj.optString("license", "")
                    val bannerName = if (obj.has("banner_name")) obj.optString("banner_name", "medplus") else obj.optString("bannerName", "medplus")
                    val ownerName = if (obj.has("owner_name")) obj.optString("owner_name", "Dr. Amit Patra") else obj.optString("ownerName", "Dr. Amit Patra")
                    val status = obj.optString("status", "Active")
                    val createdDate = if (obj.has("created_at")) obj.optString("created_at", "2026-06-22") else obj.optString("createdDate", "2026-06-22")
                    val subscriptionPlan = if (obj.has("subscription_plan")) obj.optString("subscription_plan", "Basic") else obj.optString("subscriptionPlan", "Basic")
                    val subscriptionStart = if (obj.has("subscription_start")) obj.optString("subscription_start", "2026-06-22") else obj.optString("subscriptionStart", "2026-06-22")
                    val subscriptionExpiry = if (obj.has("subscription_expiry")) obj.optString("subscription_expiry", "2026-07-22") else obj.optString("subscriptionExpiry", "2026-07-22")
                    val subscriptionAmount = if (obj.has("subscription_amount")) obj.optDouble("subscription_amount", 0.0) else obj.optDouble("subscriptionAmount", 0.0)
                    val subscriptionPaymentStatus = if (obj.has("subscription_payment_status")) obj.optString("subscription_payment_status", "Paid") else obj.optString("subscriptionPaymentStatus", "Paid")
                    val trialStarted = if (obj.has("trial_started")) obj.optBoolean("trial_started", false) else obj.optBoolean("trialStarted", false)
                    val trialStartDate = if (obj.has("trial_start_date")) obj.optString("trial_start_date", "") else obj.optString("trialStartDate", "")
                    val trialEndDate = if (obj.has("trial_end_date")) obj.optString("trial_end_date", "") else obj.optString("trialEndDate", "")
                    
                    list.add(
                        PharmacyEntity(
                            id = id,
                            name = name,
                            address = address,
                            phone = phone,
                            license = license,
                            bannerName = bannerName,
                            ownerName = ownerName,
                            status = status,
                            createdDate = createdDate,
                            subscriptionPlan = subscriptionPlan,
                            subscriptionStart = subscriptionStart,
                            subscriptionExpiry = subscriptionExpiry,
                            subscriptionAmount = subscriptionAmount,
                            subscriptionPaymentStatus = subscriptionPaymentStatus,
                            trialStarted = trialStarted,
                            trialStartDate = trialStartDate,
                            trialEndDate = trialEndDate
                        )
                    )
                }
            } else {
                Log.e("SupabaseManager", "Failed to fetch pharmacies: Res Code $responseCode")
            }
            connection.disconnect()
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Error fetching pharmacies: ${e.message}", e)
        }
        list
    }
}
