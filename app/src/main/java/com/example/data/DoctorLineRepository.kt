package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class DoctorLineRepository(private val dao: DoctorLineDao) {

    // Users
    val activeUser: Flow<UserEntity?> = dao.getActiveUser()

    suspend fun getLoggedInUser(): UserEntity? = dao.getLoggedInUser()

    suspend fun loginUser(name: String, email: String, phone: String, role: String) {
        dao.clearUserLogin()
        val user = UserEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            phone = phone,
            role = role,
            isLoggedIn = true
        )
        dao.insertUser(user)
        syncUser(user)
        
        // Log action
        logAction("User Login", "Logged in as $name ($role)")
    }

    suspend fun logoutUser() {
        val user = dao.getLoggedInUser()
        if (user != null) {
            val loggedOutUser = user.copy(isLoggedIn = false)
            syncUser(loggedOutUser)
        }
        dao.clearUserLogin()
        logAction("User Logout", "User signed out.")
    }

    // Pharmacies
    val allPharmacies: Flow<List<PharmacyEntity>> = dao.getAllPharmacies()

    suspend fun getPharmacyById(id: String): PharmacyEntity? = dao.getPharmacyById(id)

    suspend fun addPharmacy(pharmacy: PharmacyEntity) {
        dao.insertPharmacy(pharmacy)
        syncPharmacy(pharmacy)
        logAction("Add Pharmacy", "Registered pharmacy: ${pharmacy.name}")
    }

    suspend fun editPharmacy(pharmacy: PharmacyEntity) {
        dao.updatePharmacy(pharmacy)
        syncPharmacy(pharmacy)
        logAction("Edit Pharmacy", "Updated pharmacy: ${pharmacy.name}")
    }

    suspend fun removePharmacy(id: String) {
        val pharmacy = dao.getPharmacyById(id)
        if (pharmacy != null) {
            dao.deletePharmacyById(id)
            SupabaseManager.delete("pharmacies", id)
            logAction("Remove Pharmacy", "Removed pharmacy: ${pharmacy.name}")
        }
    }


    // Doctors
    val allDoctors: Flow<List<DoctorEntity>> = dao.getAllDoctorsFlow()
    val activeDoctors: Flow<List<DoctorEntity>> = dao.getActiveDoctorsFlow()

    fun getDoctorsByPharmacy(pharmacyId: String): Flow<List<DoctorEntity>> = 
        dao.getDoctorsByPharmacyFlow(pharmacyId)

    suspend fun getDoctorById(id: String): DoctorEntity? = dao.getDoctorById(id)

    suspend fun addDoctor(doctor: DoctorEntity) {
        dao.insertDoctor(doctor)
        syncDoctor(doctor)
        logAction("Add Doctor", "Added doctor profile: ${doctor.name}")
    }

    suspend fun updateDoctor(doctor: DoctorEntity) {
        dao.updateDoctor(doctor)
        syncDoctor(doctor)
        logAction("Update Doctor", "Modified doctor profile: ${doctor.name}")
    }

    suspend fun toggleDoctorEnabled(doctorId: String, isEnabled: Boolean) {
        val doc = dao.getDoctorById(doctorId)
        if (doc != null) {
            val updated = doc.copy(isEnabled = isEnabled)
            dao.updateDoctor(updated)
            syncDoctor(updated)
            logAction("Toggle Doctor Status", "Set ${doc.name} status to: ${if (isEnabled) "Active" else "Disabled"}")
        }
    }

    suspend fun fetchAndSyncDoctors() {
        if (!SupabaseManager.isConfigured) return
        val remoteDoctors = SupabaseManager.fetchDoctors()
        if (remoteDoctors.isNotEmpty()) {
            for (doctor in remoteDoctors) {
                dao.insertDoctor(doctor)
            }
            logAction("Fetch Supabase Doctors", "Synchronized ${remoteDoctors.size} doctor profiles from live database.")
        }
    }

    // Schedules
    fun getSchedulesForDoctor(doctorId: String): Flow<List<ScheduleEntity>> =
        dao.getSchedulesForDoctorFlow(doctorId)

    suspend fun addSchedule(schedule: ScheduleEntity) {
        dao.insertSchedule(schedule)
        logAction("Add Schedule", "Configured shift for doctor on ${schedule.dateStr}")
    }

    suspend fun markHoliday(scheduleId: String, isHoliday: Boolean) {
        dao.markHoliday(scheduleId, isHoliday)
        logAction("Mark Holiday", "Status update: Schedule ID $scheduleId holiday set to $isHoliday")
    }

    // Bookings
    val allBookings: Flow<List<BookingEntity>> = dao.getAllBookingsFlow()

    fun getBookingsByDoctor(doctorId: String): Flow<List<BookingEntity>> =
        dao.getBookingsByDoctorFlow(doctorId)

    fun getBookingsByPatientPhone(phone: String): Flow<List<BookingEntity>> =
        dao.getBookingsByPatientPhone(phone)

    suspend fun createBooking(
        patientName: String,
        patientPhone: String,
        age: Int,
        gender: String,
        doctorId: String,
        dateStr: String,
        timeStr: String,
        paymentMode: String
    ): BookingEntity {
        // Calculate dynamic token number based on bookings for the same doctor and date
        val currentBookings = dao.getBookingsByDoctorFlow(doctorId).firstOrNull() ?: emptyList()
        val matchCount = currentBookings.filter { it.dateStr == dateStr }.size
        val token = matchCount + 1

        val booking = BookingEntity(
            id = UUID.randomUUID().toString(),
            tokenNumber = token,
            patientName = patientName,
            patientPhone = patientPhone,
            age = age,
            gender = gender,
            doctorId = doctorId,
            dateStr = dateStr,
            timeStr = timeStr,
            status = "Upcoming",
            paymentMode = paymentMode,
            paymentStatus = "Pending"
        )

        dao.insertBooking(booking)
        syncBooking(booking)

        // Insert payment ledger
        dao.insertPayment(
            PaymentEntity(
                bookingId = booking.id,
                amount = 800.0,
                status = "Pending",
                dateStr = dateStr
            )
        )

        // Add Notification
        dao.insertNotification(
            NotificationEntity(
                title = "Booking Confirmed #$token",
                message = "Your slot with doctor has been locked for $dateStr at $timeStr. Present this token at pharmacy counter."
            )
        )

        logAction("Create Booking", "New doctor booking. Token: #$token for ${booking.patientName}")
        return booking
    }

    suspend fun updateBookingStatus(bookingId: String, status: String) {
        val bookingsList = dao.getAllBookingsFlow().firstOrNull() ?: emptyList()
        val booking = bookingsList.find { it.id == bookingId }
        if (booking != null) {
            val updated = booking.copy(status = status)
            dao.updateBooking(updated)
            syncBooking(updated)
            logAction("Update Booking", "Status of booking for ${booking.patientName} changed to $status")
        }
    }

    // Subscriptions
    fun getSubscriptionForPharmacy(pharmacyId: String): Flow<SubscriptionEntity?> =
        dao.getSubscriptionForPharmacy(pharmacyId)

    suspend fun updateSubscription(subscription: SubscriptionEntity) {
        dao.insertSubscription(subscription)
        logAction("Update Subscription", "Pharmacy subscription details modified.")
    }

    suspend fun toggleSubscriptionRenewal(id: String, autoRenew: Boolean) {
        dao.toggleSubscriptionRenewal(id, autoRenew)
        logAction("Toggle Renew", "Auto renewal status configured for subscription: $autoRenew")
    }

    // Notifications
    val allNotifications: Flow<List<NotificationEntity>> = dao.getNotificationsFlow()

    suspend fun deleteNotification(id: String) {
        dao.markNotificationAsRead(id)
    }

    // Audit Logs
    val auditLogs: Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()

    suspend fun logAction(action: String, details: String) {
        val log = AuditLogEntity(
            action = action,
            details = details
        )
        dao.insertAuditLog(log)
        syncAuditLog(log)
    }

    // SUPABASE NATIVE SYNC HELPERS
    private suspend fun syncUser(user: UserEntity) {
        val payload = """
            {
                "id": "${user.id}",
                "name": "${user.name.replace("\"", "\\\"")}",
                "email": "${user.email.replace("\"", "\\\"")}",
                "phone": "${user.phone}",
                "role": "${user.role}",
                "is_logged_in": ${user.isLoggedIn}
            }
        """.trimIndent()
        SupabaseManager.insertOrUpdate("users", payload)
    }

    private suspend fun syncBooking(booking: BookingEntity) {
        val payload = """
            {
                "id": "${booking.id}",
                "token_number": ${booking.tokenNumber},
                "patient_name": "${booking.patientName.replace("\"", "\\\"")}",
                "patient_phone": "${booking.patientPhone}",
                "age": ${booking.age},
                "gender": "${booking.gender}",
                "doctor_id": "${booking.doctorId}",
                "date_str": "${booking.dateStr}",
                "time_str": "${booking.timeStr}",
                "status": "${booking.status}",
                "payment_mode": "${booking.paymentMode}",
                "payment_status": "${booking.paymentStatus}"
            }
        """.trimIndent()
        SupabaseManager.insertOrUpdate("bookings", payload)
    }

    private suspend fun syncDoctor(doctor: DoctorEntity) {
        val payload = """
            {
                "id": "${doctor.id}",
                "name": "${doctor.name.replace("\"", "\\\"")}",
                "specialization": "${doctor.specialization.replace("\"", "\\\"")}",
                "experience": ${doctor.experience},
                "fee": ${doctor.fee},
                "rating": ${doctor.rating},
                "pharmacy_id": "${doctor.pharmacyId}",
                "banner_name": "${doctor.bannerName}",
                "slots_json": "${doctor.slotsJson.replace("\"", "\\\"")}",
                "is_enabled": ${doctor.isEnabled}
            }
        """.trimIndent()
        SupabaseManager.insertOrUpdate("doctors", payload)
    }

    private suspend fun syncAuditLog(log: AuditLogEntity) {
        val payload = """
            {
                "id": "${log.id}",
                "action": "${log.action}",
                "details": "${log.details.replace("\"", "\\\"")}",
                "timestamp": ${log.timestamp}
            }
        """.trimIndent()
        SupabaseManager.insertOrUpdate("audit_logs", payload)
    }

    private suspend fun syncPharmacy(pharmacy: PharmacyEntity) {
        val payload = """
            {
                "id": "${pharmacy.id}",
                "pharmacy_name": "${pharmacy.name.replace("\"", "\\\"")}",
                "owner_name": "${pharmacy.ownerName.replace("\"", "\\\"")}",
                "mobile": "${pharmacy.phone}",
                "address": "${pharmacy.address.replace("\"", "\\\"")}",
                "status": "${pharmacy.status}",
                "created_at": "${pharmacy.createdDate}"
            }
        """.trimIndent()
        SupabaseManager.insertOrUpdate("pharmacies", payload)
    }

}
