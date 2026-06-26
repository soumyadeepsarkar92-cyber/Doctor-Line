package com.example.data.payment

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Supported payment types in DoctorLine.
 */
enum class PaymentType {
    PHARMACY_REGISTRATION_FEE,
    MONTHLY_SUBSCRIPTION_RENEWAL,
    QUARTERLY_SUBSCRIPTION,
    YEARLY_SUBSCRIPTION
}

/**
 * Standard payment status.
 */
enum class PaymentStatus {
    pending,
    created,
    processing,
    success,
    failed,
    cancelled,
    refunded,
    expired
}

/**
 * Secure configuration for Razorpay.
 * Note: Only publishable Key ID is stored client-side. Secret Key remains server-side.
 */
data class RazorpayConfig(
    val keyId: String,
    val mode: String = "test", // "test" or "live"
    val isTestMode: Boolean = true
) {
    companion object {
        fun fromBuildConfig(): RazorpayConfig {
            val key = try {
                // Read from injected BuildConfig
                com.example.BuildConfig.RAZORPAY_KEY_ID
            } catch (e: Exception) {
                "rzp_test_placeholder_key_id"
            }
            val modeStr = try {
                com.example.BuildConfig.RAZORPAY_MODE
            } catch (e: Exception) {
                "test"
            }
            return RazorpayConfig(
                keyId = if (key.isBlank()) "rzp_test_placeholder_key_id" else key,
                mode = modeStr,
                isTestMode = modeStr.lowercase() == "test"
            )
        }
    }
}

/**
 * Represents a Razorpay / Supabase Order created for payments.
 */
data class PaymentOrder(
    val orderId: String,
    val amount: Double,
    val currency: String = "INR",
    val status: PaymentStatus = PaymentStatus.created,
    val paymentType: PaymentType,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Reusable entity for local Payment History tracking.
 */
@Entity(tableName = "payment_history")
data class PaymentHistoryRecord(
    @PrimaryKey val paymentId: String = UUID.randomUUID().toString(),
    val orderId: String,
    val amount: Double,
    val currency: String = "INR",
    val paymentType: String, // String representation of PaymentType
    val paymentStatus: String, // String representation of PaymentStatus
    val createdDate: String, // YYYY-MM-DD HH:MM:SS
    val completedDate: String? = null,
    val failureReason: String? = null,
    val pharmacyId: String? = null,
    val signature: String? = null,
    val method: String? = null
)

/**
 * Result state wrapper for handling payment transactions modularly.
 */
sealed class PaymentResult<out T> {
    data class Success<out T>(val data: T) : PaymentResult<T>()
    data class Error(val exception: Throwable, val message: String) : PaymentResult<Nothing>()
    object Loading : PaymentResult<Nothing>()
}
