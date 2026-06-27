package com.example.data

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

object NetworkHelper {
    private const val TAG = "NetworkHelper"
    private const val MAX_RETRIES = 3
    private const val TIMEOUT_MS = 15000L // 15 seconds

    private val activeRequests = ConcurrentHashMap<String, Mutex>()

    /**
     * Executes a network block with retry, timeout, and duplicate request prevention.
     */
    suspend fun <T> executeWithRetry(
        requestKey: String,
        allowOfflineCache: Boolean = false,
        fetchFromCache: (suspend () -> T)? = null,
        saveToCache: (suspend (T) -> Unit)? = null,
        networkBlock: suspend () -> T
    ): Result<T> {
        val mutex = activeRequests.getOrPut(requestKey) { Mutex() }
        
        return mutex.withLock {
            var currentAttempt = 0
            var lastException: Exception? = null

            while (currentAttempt < MAX_RETRIES) {
                try {
                    val result = withTimeout(TIMEOUT_MS) {
                        networkBlock()
                    }
                    // Save to cache on success
                    saveToCache?.invoke(result)
                    return Result.success(result)
                } catch (e: TimeoutCancellationException) {
                    lastException = e
                    Log.w(TAG, "Request $requestKey timed out. Attempt ${currentAttempt + 1}")
                } catch (e: IOException) {
                    lastException = e
                    Log.w(TAG, "Network error for $requestKey. Attempt ${currentAttempt + 1}")
                } catch (e: CancellationException) {
                    Log.i(TAG, "Request $requestKey cancelled.")
                    throw e // Propagate cancellation
                } catch (e: Exception) {
                    lastException = e
                    Log.e(TAG, "Unexpected error for $requestKey", e)
                    break // Don't retry on unexpected exceptions (like JSON parsing errors)
                }
                
                currentAttempt++
                if (currentAttempt < MAX_RETRIES) {
                    // Exponential backoff
                    delay((1000 * Math.pow(2.0, currentAttempt.toDouble())).toLong())
                }
            }

            // If we get here, network failed. Try offline cache if allowed.
            if (allowOfflineCache && fetchFromCache != null) {
                try {
                    Log.i(TAG, "Serving $requestKey from offline cache after network failure.")
                    val cachedData = fetchFromCache()
                    if (cachedData != null && (cachedData !is Collection<*> || cachedData.isNotEmpty())) {
                        return Result.success(cachedData)
                    }
                } catch (cacheEx: Exception) {
                    Log.e(TAG, "Failed to load from cache for $requestKey", cacheEx)
                }
            }

            return Result.failure(lastException ?: Exception("Unknown network failure"))
        }
    }
}
