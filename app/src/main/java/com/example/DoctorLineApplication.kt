package com.example

import android.app.Application
import com.example.data.DoctorLineDatabase
import com.example.data.DoctorLineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DoctorLineApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { DoctorLineDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { DoctorLineRepository(database.doctorLineDao()) }

    // Lazy initialization of payment foundation components
    val paymentService by lazy {
        if (com.example.data.SupabaseManager.isConfigured) {
            com.example.data.payment.SupabasePaymentServiceImpl()
        } else {
            com.example.data.payment.MockPaymentServiceImpl()
        }
    }
    val paymentRepository by lazy {
        com.example.data.payment.PaymentRepository(database.doctorLineDao(), paymentService)
    }

    override fun onCreate() {
        super.onCreate()
        // Pre-populating setup is inside Database Callback
    }
}
