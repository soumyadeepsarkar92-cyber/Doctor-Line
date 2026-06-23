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

    override fun onCreate() {
        super.onCreate()
        // Pre-populating setup is inside Database Callback
    }
}
