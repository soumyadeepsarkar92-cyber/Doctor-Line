package com.example.data.payment

import android.util.Log
import com.example.data.DoctorLineDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository orchestrating secure payment operations.
 * Communicates with PaymentService (Supabase Edge functions) and saves results locally.
 */
class PaymentRepository(
    private val dao: DoctorLineDao,
    private val service: PaymentService
) {

    // Retrieve all payment history records reactively
    val allPaymentHistory: Flow<List<PaymentHistoryRecord>> = dao.getAllPaymentHistories()

    /**
     * Get payment histories filtered by pharmacy ID.
     */
    fun getPaymentHistoryByPharmacy(pharmacyId: String): Flow<List<PaymentHistoryRecord>> {
        return dao.getPaymentHistoryByPharmacy(pharmacyId)
    }

    /**
     * Initiate payment for the Pharmacy Registration Fee.
     */
    suspend fun initiateRegistrationFee(
        pharmacyRequestId: String,
        amount: Double,
        email: String
    ): PaymentResult<PaymentOrder> = withContext(Dispatchers.IO) {
        val metadata = mapOf(
            "pharmacyRequestId" to pharmacyRequestId,
            "email" to email,
            "paymentType" to PaymentType.PHARMACY_REGISTRATION_FEE.name
        )
        
        Log.d("PaymentRepository", "Initiating Pharmacy Registration Fee of ₹$amount for request $pharmacyRequestId")
        
        val result = service.createOrder(
            amount = amount,
            paymentType = PaymentType.PHARMACY_REGISTRATION_FEE,
            metadata = metadata
        )

        if (result is PaymentResult.Success) {
            val order = result.data
            // Store a pending log inside the database
            val record = PaymentHistoryRecord(
                paymentId = "pending_" + UUID.randomUUID().toString().take(8),
                orderId = order.orderId,
                amount = amount,
                paymentType = PaymentType.PHARMACY_REGISTRATION_FEE.name,
                paymentStatus = PaymentStatus.created.name,
                createdDate = getCurrentDateTimeString(),
                pharmacyId = pharmacyRequestId
            )
            dao.insertPaymentHistory(record)
        }
        result
    }

    /**
     * Initiate payment for the Monthly Subscription Renewal.
     */
    suspend fun initiateSubscriptionRenewal(
        pharmacyId: String,
        amount: Double
    ): PaymentResult<PaymentOrder> = withContext(Dispatchers.IO) {
        val metadata = mapOf(
            "pharmacyId" to pharmacyId,
            "paymentType" to PaymentType.MONTHLY_SUBSCRIPTION_RENEWAL.name
        )

        Log.d("PaymentRepository", "Initiating Monthly Subscription Renewal of ₹$amount for pharmacy $pharmacyId")

        val result = service.createOrder(
            amount = amount,
            paymentType = PaymentType.MONTHLY_SUBSCRIPTION_RENEWAL,
            metadata = metadata
        )

        if (result is PaymentResult.Success) {
            val order = result.data
            val record = PaymentHistoryRecord(
                paymentId = "pending_" + UUID.randomUUID().toString().take(8),
                orderId = order.orderId,
                amount = amount,
                paymentType = PaymentType.MONTHLY_SUBSCRIPTION_RENEWAL.name,
                paymentStatus = PaymentStatus.created.name,
                createdDate = getCurrentDateTimeString(),
                pharmacyId = pharmacyId
            )
            dao.insertPaymentHistory(record)
        }
        result
    }

    /**
     * Verify payment signature and update history status.
     */
    suspend fun verifyAndCompletePayment(
        orderId: String,
        paymentId: String,
        signature: String,
        pharmacyId: String?,
        type: PaymentType,
        amount: Double,
        paymentMethod: String = "UPI",
        failureReason: String? = null
    ): PaymentResult<Boolean> = withContext(Dispatchers.IO) {
        if (failureReason != null) {
            // Register a failed payment record locally
            val failedRecord = PaymentHistoryRecord(
                paymentId = paymentId.ifBlank { "fail_" + UUID.randomUUID().toString().take(8) },
                orderId = orderId,
                amount = amount,
                paymentType = type.name,
                paymentStatus = PaymentStatus.failed.name,
                createdDate = getCurrentDateTimeString(),
                completedDate = getCurrentDateTimeString(),
                failureReason = failureReason,
                pharmacyId = pharmacyId,
                method = paymentMethod
            )
            dao.insertPaymentHistory(failedRecord)
            return@withContext PaymentResult.Success(false)
        }

        val verificationResult = service.verifyPayment(orderId, paymentId, signature)
        
        if (verificationResult is PaymentResult.Success && verificationResult.data) {
            // Update/Save successful payment record
            val record = PaymentHistoryRecord(
                paymentId = paymentId,
                orderId = orderId,
                amount = amount,
                paymentType = type.name,
                paymentStatus = PaymentStatus.success.name,
                createdDate = getCurrentDateTimeString(),
                completedDate = getCurrentDateTimeString(),
                pharmacyId = pharmacyId,
                signature = signature,
                method = paymentMethod
            )
            dao.insertPaymentHistory(record)
            PaymentResult.Success(true)
        } else {
            val record = PaymentHistoryRecord(
                paymentId = paymentId.ifBlank { "failed_" + UUID.randomUUID().toString().take(8) },
                orderId = orderId,
                amount = amount,
                paymentType = type.name,
                paymentStatus = PaymentStatus.failed.name,
                createdDate = getCurrentDateTimeString(),
                completedDate = getCurrentDateTimeString(),
                failureReason = "Signature verification failed",
                pharmacyId = pharmacyId,
                signature = signature,
                method = paymentMethod
            )
            dao.insertPaymentHistory(record)
            PaymentResult.Error(Exception("Signature verification failed"), "Verification failed securely on backend.")
        }
    }

    /**
     * Support retrying a failed or expired payment.
     */
    suspend fun retryPayment(
        orderId: String,
        record: PaymentHistoryRecord
    ): PaymentResult<PaymentOrder> = withContext(Dispatchers.IO) {
        val result = service.retryPayment(orderId)
        if (result is PaymentResult.Success) {
            val newOrder = result.data
            val updatedRecord = record.copy(
                orderId = newOrder.orderId,
                paymentStatus = PaymentStatus.processing.name,
                failureReason = null
            )
            dao.insertPaymentHistory(updatedRecord)
        }
        result
    }

    /**
     * Retrieve visual URL for downloading Receipt.
     */
    suspend fun getReceiptUrl(paymentId: String): PaymentResult<String> {
        return service.downloadReceipt(paymentId)
    }

    /**
     * Retrieve visual URL for downloading Invoice.
     */
    suspend fun getInvoiceUrl(paymentId: String): PaymentResult<String> {
        return service.downloadInvoice(paymentId)
    }

    /**
     * Inserts a payment history record into the local SQLite database.
     */
    suspend fun insertPaymentHistory(record: PaymentHistoryRecord) = withContext(Dispatchers.IO) {
        dao.insertPaymentHistory(record)
    }

    private fun getCurrentDateTimeString(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }
}
