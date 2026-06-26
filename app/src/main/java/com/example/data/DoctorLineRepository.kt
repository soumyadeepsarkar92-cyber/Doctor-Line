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
    private val _allDoctors = kotlinx.coroutines.flow.MutableStateFlow<List<DoctorEntity>>(emptyList())
    val allDoctors: Flow<List<DoctorEntity>> = if (SupabaseManager.isConfigured) {
        _allDoctors
    } else {
        dao.getAllDoctorsFlow()
    }

    private val _activeDoctors = kotlinx.coroutines.flow.MutableStateFlow<List<DoctorEntity>>(emptyList())
    val activeDoctors: Flow<List<DoctorEntity>> = if (SupabaseManager.isConfigured) {
        _activeDoctors
    } else {
        dao.getActiveDoctorsFlow()
    }

    fun getDoctorsByPharmacy(pharmacyId: String): Flow<List<DoctorEntity>> = 
        if (SupabaseManager.isConfigured) {
            kotlinx.coroutines.flow.flow {
                allDoctors.collect { list ->
                    emit(list.filter { it.pharmacyId == pharmacyId })
                }
            }
        } else {
            dao.getDoctorsByPharmacyFlow(pharmacyId)
        }

    suspend fun getDoctorById(id: String): DoctorEntity? {
        return if (SupabaseManager.isConfigured) {
            _allDoctors.value.find { it.id == id }
        } else {
            dao.getDoctorById(id)
        }
    }

    suspend fun addDoctor(doctor: DoctorEntity) {
        val currentUser = getLoggedInUser()
        if (currentUser != null && currentUser.role == "Pharmacy" && doctor.pharmacyId != currentUser.id) {
            logAction("Security Alert", "Unauthorized attempt by pharmacy ${currentUser.name} to register doctor ${doctor.name} under another pharmacy's ID.")
            return
        }

        if (SupabaseManager.isConfigured) {
            syncDoctor(doctor)
            fetchAndSyncDoctors()
        } else {
            dao.insertDoctor(doctor)
        }
        logAction("Add Doctor", "Added doctor profile: ${doctor.name}")
    }

    suspend fun updateDoctor(doctor: DoctorEntity) {
        val currentUser = getLoggedInUser()
        if (currentUser != null && currentUser.role == "Pharmacy" && doctor.pharmacyId != currentUser.id) {
            logAction("Security Alert", "Unauthorized attempt by pharmacy ${currentUser.name} to modify doctor ${doctor.name}")
            return
        }

        if (SupabaseManager.isConfigured) {
            syncDoctor(doctor)
            fetchAndSyncDoctors()
        } else {
            dao.updateDoctor(doctor)
        }
        logAction("Update Doctor", "Modified doctor profile: ${doctor.name}")
    }

    suspend fun toggleDoctorEnabled(doctorId: String, isEnabled: Boolean) {
        val doc = getDoctorById(doctorId)
        if (doc != null) {
            val currentUser = getLoggedInUser()
            if (currentUser != null && currentUser.role == "Pharmacy" && doc.pharmacyId != currentUser.id) {
                logAction("Security Alert", "Unauthorized attempt by pharmacy ${currentUser.name} to toggle status of doctor ${doc.name}")
                return
            }

            val updated = doc.copy(isEnabled = isEnabled)
            if (SupabaseManager.isConfigured) {
                syncDoctor(updated)
                fetchAndSyncDoctors()
            } else {
                dao.updateDoctor(updated)
            }
            logAction("Toggle Doctor Status", "Set ${doc.name} status to: ${if (isEnabled) "Active" else "Disabled"}")
        }
    }

    suspend fun softDeleteDoctor(doctorId: String) {
        val doc = getDoctorById(doctorId)
        if (doc != null) {
            val currentUser = getLoggedInUser()
            if (currentUser != null && currentUser.role == "Pharmacy" && doc.pharmacyId != currentUser.id) {
                logAction("Security Alert", "Unauthorized attempt by pharmacy ${currentUser.name} to soft delete doctor ${doc.name}")
                return
            }

            val updated = doc.copy(isSoftDeleted = true)
            if (SupabaseManager.isConfigured) {
                syncDoctor(updated)
                fetchAndSyncDoctors()
            } else {
                dao.updateDoctor(updated)
            }
            logAction("Soft Delete Doctor", "Moved doctor ${doc.name} to Deleted Doctors roster.")
        }
    }

    suspend fun restoreDoctor(doctorId: String) {
        val doc = getDoctorById(doctorId)
        if (doc != null) {
            val currentUser = getLoggedInUser()
            if (currentUser != null && currentUser.role == "Pharmacy" && doc.pharmacyId != currentUser.id) {
                logAction("Security Alert", "Unauthorized attempt by pharmacy ${currentUser.name} to restore doctor ${doc.name}")
                return
            }

            val updated = doc.copy(isSoftDeleted = false)
            if (SupabaseManager.isConfigured) {
                syncDoctor(updated)
                fetchAndSyncDoctors()
            } else {
                dao.updateDoctor(updated)
            }
            logAction("Restore Doctor", "Restored doctor ${doc.name} to active roster.")
        }
    }

    suspend fun permanentlyDeleteDoctor(doctorId: String) {
        val doc = getDoctorById(doctorId)
        if (doc != null) {
            val currentUser = getLoggedInUser()
            if (currentUser != null && currentUser.role == "Pharmacy" && doc.pharmacyId != currentUser.id) {
                logAction("Security Alert", "Unauthorized attempt by pharmacy ${currentUser.name} to permanently delete doctor ${doc.name}")
                return
            }

            val todayDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val allDoctorBookings = dao.getBookingsByDoctorList(doctorId)
            val historicalBookings = allDoctorBookings.filter { it.dateStr < todayDateStr || it.status != "Upcoming" }

            if (historicalBookings.isNotEmpty()) {
                // To preserve historical appointments, analytics, and revenue reports:
                // We keep the doctor entity in the DB but completely strip active visibility (isEnabled = false, isSoftDeleted = true)
                val archivedDoc = doc.copy(isEnabled = false, isSoftDeleted = true)
                
                if (SupabaseManager.isConfigured) {
                    syncDoctor(archivedDoc)
                    // Remove future schedules and future bookings (queues) from Supabase
                    SupabaseManager.deleteByQuery("schedules", "doctor_id=eq.$doctorId&date_str=gte.$todayDateStr")
                    SupabaseManager.deleteByQuery("bookings", "doctor_id=eq.$doctorId&date_str=gte.$todayDateStr&status=eq.Upcoming")
                    fetchAndSyncDoctors()
                } else {
                    dao.updateDoctor(archivedDoc)
                }

                // Remove future schedules and future bookings (queues) from local Room DB
                dao.deleteFutureSchedules(doctorId, todayDateStr)
                dao.deleteFutureBookings(doctorId, todayDateStr)

                logAction(
                    "Archive Doctor (Safe Delete)", 
                    "Archived doctor ${doc.name} to preserve ${historicalBookings.size} historical records. Removed future schedules and bookings."
                )
            } else {
                // If there are no historical records, we can safely hard-delete the doctor completely without creating orphans.
                if (SupabaseManager.isConfigured) {
                    SupabaseManager.delete("doctors", doctorId)
                    SupabaseManager.deleteByColumn("schedules", "doctor_id", doctorId)
                    SupabaseManager.deleteByColumn("bookings", "doctor_id", doctorId)
                    SupabaseManager.deleteByColumn("reviews", "doctor_id", doctorId)
                    fetchAndSyncDoctors()
                } else {
                    dao.deleteDoctorById(doctorId)
                    dao.deleteSchedulesByDoctorId(doctorId)
                    dao.deleteReviewsByDoctorId(doctorId)
                    dao.deleteFutureBookings(doctorId, todayDateStr)
                }
                logAction(
                    "Hard Delete Doctor", 
                    "Permanently deleted doctor ${doc.name} as they had no historical booking records."
                )
            }
        }
    }

    suspend fun fetchAndSyncDoctors() {
        if (SupabaseManager.isConfigured) {
            val remoteDoctors = SupabaseManager.fetchDoctors()
            _allDoctors.value = remoteDoctors
            _activeDoctors.value = remoteDoctors.filter { it.isEnabled && !it.isSoftDeleted }
            logAction("Fetch Supabase Doctors", "Retrieved ${remoteDoctors.size} live doctor profiles directly from Supabase.")
        } else {
            logAction("Fetch Doctors", "Using offline SQLite database for doctor profiles.")
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

    suspend fun addNotification(notification: NotificationEntity) {
        dao.insertNotification(notification)
        if (SupabaseManager.isConfigured) {
            syncNotification(notification)
        }
    }

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
                "is_enabled": ${doctor.isEnabled},
                "is_soft_deleted": ${doctor.isSoftDeleted},
                "availability_status": "${doctor.availabilityStatus}",
                "expected_start_time": "${doctor.expectedStartTime}",
                "delay_reason": "${doctor.delayReason.replace("\"", "\\\"")}"
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
                "name": "${pharmacy.name.replace("\"", "\\\"")}",
                "pharmacy_name": "${pharmacy.name.replace("\"", "\\\"")}",
                "owner_name": "${pharmacy.ownerName.replace("\"", "\\\"")}",
                "phone": "${pharmacy.phone}",
                "mobile": "${pharmacy.phone}",
                "address": "${pharmacy.address.replace("\"", "\\\"")}",
                "license": "${pharmacy.license}",
                "banner_name": "${pharmacy.bannerName}",
                "subscription_plan": "${pharmacy.subscriptionPlan}",
                "subscription_start": "${pharmacy.subscriptionStart}",
                "subscription_expiry": "${pharmacy.subscriptionExpiry}",
                "subscription_amount": ${pharmacy.subscriptionAmount},
                "subscription_payment_status": "${pharmacy.subscriptionPaymentStatus}",
                "status": "${pharmacy.status.lowercase()}",
                "trial_started": ${pharmacy.trialStarted},
                "trial_start_date": ${if (pharmacy.trialStartDate.isEmpty()) "null" else "\"${pharmacy.trialStartDate}\""},
                "trial_end_date": ${if (pharmacy.trialEndDate.isEmpty()) "null" else "\"${pharmacy.trialEndDate}\""},
                "created_at": "${pharmacy.createdDate}"
            }
        """.trimIndent()
        SupabaseManager.insertOrUpdate("pharmacies", payload)
    }

    suspend fun fetchAndSyncPharmacies() {
        if (SupabaseManager.isConfigured) {
            val remotePharmacies = SupabaseManager.fetchPharmacies()
            remotePharmacies.forEach { phar ->
                val existing = dao.getPharmacyById(phar.id)
                if (existing != null) {
                    dao.updatePharmacy(phar)
                } else {
                    dao.insertPharmacy(phar)
                }
            }
            logAction("Sync Pharmacies", "Synced ${remotePharmacies.size} pharmacies from Supabase.")
        }
    }

    // Reviews Section
    val allReviews: Flow<List<ReviewEntity>> = dao.getAllReviewsFlow()

    fun getReviewsForDoctor(doctorId: String): Flow<List<ReviewEntity>> =
        dao.getReviewsForDoctorFlow(doctorId)

    fun getReviewsForPatient(patientId: String): Flow<List<ReviewEntity>> =
        dao.getReviewsForPatientFlow(patientId)

    suspend fun addReview(review: ReviewEntity) {
        dao.insertReview(review)
        syncReview(review)
        updateDoctorAvgRating(review.doctorId)
        logAction("Add Review", "Submitted review for doctor ID: ${review.doctorId}")
    }

    suspend fun editReview(review: ReviewEntity) {
        dao.updateReview(review)
        syncReview(review)
        updateDoctorAvgRating(review.doctorId)
        logAction("Edit Review", "Modified review for doctor ID: ${review.doctorId}")
    }

    suspend fun deleteReview(id: String, doctorId: String) {
        dao.deleteReviewById(id)
        SupabaseManager.delete("reviews", id)
        updateDoctorAvgRating(doctorId)
        logAction("Delete Review", "Deleted review ID: $id")
    }

    private suspend fun updateDoctorAvgRating(doctorId: String) {
        val doctor = dao.getDoctorById(doctorId)
        if (doctor != null) {
            val doctorReviews = dao.getReviewsForDoctorFlow(doctorId).firstOrNull() ?: emptyList()
            if (doctorReviews.isNotEmpty()) {
                val avgRating = doctorReviews.map { it.rating }.average()
                val roundedRating = Math.round(avgRating * 10.0) / 10.0
                val updatedDoctor = doctor.copy(rating = roundedRating)
                dao.updateDoctor(updatedDoctor)
                syncDoctor(updatedDoctor)
            } else {
                val updatedDoctor = doctor.copy(rating = 0.0)
                dao.updateDoctor(updatedDoctor)
                syncDoctor(updatedDoctor)
            }
        }
    }

    private suspend fun syncReview(review: ReviewEntity) {
        val payload = """
            {
                "id": "${review.id}",
                "patient_id": "${review.patientId}",
                "patient_name": "${review.patientName.replace("\"", "\\\"")}",
                "doctor_id": "${review.doctorId}",
                "rating": ${review.rating},
                "review": "${review.review.replace("\"", "\\\"")}",
                "created_at": ${review.createdAt}
            }
        """.trimIndent()
        SupabaseManager.insertOrUpdate("reviews", payload)
    }

    // Pharmacy Requests Section
    val allPharmacyRequests: Flow<List<PharmacyRequestEntity>> = dao.getAllPharmacyRequests()

    suspend fun getPharmacyRequestByEmail(email: String): PharmacyRequestEntity? = dao.getPharmacyRequestByEmail(email)

    suspend fun getPharmacyRequestByLicense(licenseNo: String): PharmacyRequestEntity? = dao.getPharmacyRequestByLicense(licenseNo)

    suspend fun addPharmacyRequest(request: PharmacyRequestEntity) {
        dao.insertPharmacyRequest(request)
        syncPharmacyRequest(request)
        logAction("Register Pharmacy Request", "Submitted registration request for: ${request.pharmacyName} (${request.email})")
    }

    suspend fun updatePharmacyPassword(email: String, passwordHash: String) {
        val req = dao.getPharmacyRequestByEmail(email)
        if (req != null) {
            val updated = req.copy(passwordHash = passwordHash)
            dao.updatePharmacyRequest(updated)
            syncPharmacyRequest(updated)
            logAction("Update Password", "Pharmacy $email updated password.")
        }
    }

    suspend fun approvePharmacyRequest(requestId: String, currentAdminId: String) {
        val requests = dao.getAllPharmacyRequests().firstOrNull() ?: emptyList()
        val req = requests.find { it.id == requestId }
        if (req != null) {
            val now = System.currentTimeMillis()
            val approvedReq = req.copy(
                status = "approved",
                approvedAt = now,
                approvedBy = currentAdminId
            )
            dao.updatePharmacyRequest(approvedReq)
            syncPharmacyRequest(approvedReq)

            // Step 1: Create local user entity (auth profile)
            val authUserId = req.id
            val userProfile = UserEntity(
                id = authUserId,
                name = req.ownerName,
                email = req.email,
                phone = req.mobile,
                role = "Pharmacy",
                isLoggedIn = false
            )
            dao.insertUser(userProfile)
            syncUser(userProfile)

            // Step 2: Create local pharmacy entity
            val pharmacy = PharmacyEntity(
                id = authUserId,
                name = req.pharmacyName,
                address = req.address,
                phone = req.mobile,
                license = req.licenseNo,
                bannerName = "medplus",
                ownerName = req.ownerName,
                status = "Active",
                createdDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                subscriptionPlan = "Basic",
                subscriptionStart = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                subscriptionExpiry = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()), // set to current date initially, or placeholder
                subscriptionAmount = 0.0,
                subscriptionPaymentStatus = "Paid",
                trialStarted = false,
                trialStartDate = "",
                trialEndDate = ""
            )
            dao.insertPharmacy(pharmacy)
            syncPharmacy(pharmacy)

            // Step 3: Add Notification for Pharmacy registration approval
            dao.insertNotification(
                NotificationEntity(
                    title = "Pharmacy Request Approved",
                    message = "Pharmacy Registration Request for '${req.pharmacyName}' has been successfully Approved and Provisioned.",
                    timestamp = System.currentTimeMillis()
                )
            )

            logAction("Approve Pharmacy Request", "Approved and provisioned pharmacy: ${req.pharmacyName}")
        }
    }

    suspend fun rejectPharmacyRequest(requestId: String, reason: String) {
        val requests = dao.getAllPharmacyRequests().firstOrNull() ?: emptyList()
        val req = requests.find { it.id == requestId }
        if (req != null) {
            val rejectedReq = req.copy(status = "rejected", rejectionReason = reason)
            dao.updatePharmacyRequest(rejectedReq)
            syncPharmacyRequest(rejectedReq)

            dao.insertNotification(
                NotificationEntity(
                    title = "Pharmacy Request Rejected",
                    message = "Pharmacy Registration Request for '${req.pharmacyName}' has been Rejected by Admin. Reason: $reason",
                    timestamp = System.currentTimeMillis()
                )
            )

            logAction("Reject Pharmacy Request", "Rejected pharmacy registration: ${req.pharmacyName} for reason: $reason")
        }
    }

    suspend fun requestCorrectionPharmacyRequest(requestId: String, notes: String) {
        val requests = dao.getAllPharmacyRequests().firstOrNull() ?: emptyList()
        val req = requests.find { it.id == requestId }
        if (req != null) {
            val correctedReq = req.copy(status = "correction_requested", correctionNotes = notes)
            dao.updatePharmacyRequest(correctedReq)
            syncPharmacyRequest(correctedReq)

            dao.insertNotification(
                NotificationEntity(
                    title = "Correction Requested",
                    message = "Pharmacy Registration Request for '${req.pharmacyName}' requires correction. Notes: $notes",
                    timestamp = System.currentTimeMillis()
                )
            )

            logAction("Correction Requested", "Requested corrections for pharmacy registration: ${req.pharmacyName} with notes: $notes")
        }
    }

    private suspend fun syncPharmacyRequest(request: PharmacyRequestEntity) {
        val payload = """
            {
                "id": "${request.id}",
                "pharmacy_name": "${request.pharmacyName.replace("\"", "\\\"")}",
                "owner_name": "${request.ownerName.replace("\"", "\\\"")}",
                "license_no": "${request.licenseNo.replace("\"", "\\\"")}",
                "mobile": "${request.mobile}",
                "email": "${request.email.replace("\"", "\\\"")}",
                "password_hash": "${request.passwordHash}",
                "address": "${request.address.replace("\"", "\\\"")}",
                "license_image": "${request.licenseImage}",
                "pharmacy_photo": ${if (request.pharmacyPhoto != null) "\"${request.pharmacyPhoto}\"" else "null"},
                "status": "${request.status}",
                "approved_at": ${if (request.approvedAt != null) request.approvedAt else "null"},
                "approved_by": ${if (request.approvedBy != null) "\"${request.approvedBy}\"" else "null"},
                "payment_id": ${if (request.paymentId != null) "\"${request.paymentId}\"" else "null"},
                "payment_status": ${if (request.paymentStatus != null) "\"${request.paymentStatus}\"" else "null"},
                "payment_amount": ${if (request.paymentAmount != null) request.paymentAmount else "null"},
                "payment_date": ${if (request.paymentDate != null) request.paymentDate else "null"},
                "created_at": ${request.createdAt}
            }
        """.trimIndent()
        SupabaseManager.insertOrUpdate("pharmacy_requests", payload)
    }

    // Favourites and Delay Management Section
    val allFavourites: Flow<List<FavouriteDoctorEntity>> = dao.getAllFavouritesFlow()

    fun getFavouritesForPatient(patientId: String): Flow<List<FavouriteDoctorEntity>> =
        dao.getFavouritesForPatientFlow(patientId)

    suspend fun toggleFavouriteDoctor(patientId: String, doctorId: String) {
        val existing = dao.getFavourite(patientId, doctorId)
        if (existing != null) {
            dao.deleteFavourite(patientId, doctorId)
            if (SupabaseManager.isConfigured) {
                SupabaseManager.deleteByQuery("favourite_doctors", "patient_id=eq.$patientId&doctor_id=eq.$doctorId")
                fetchAndSyncFavourites(patientId)
            }
            logAction("Remove Favourite", "Removed doctor ID $doctorId from patient ID $patientId favourites.")
        } else {
            val fav = FavouriteDoctorEntity(patientId = patientId, doctorId = doctorId)
            dao.insertFavourite(fav)
            if (SupabaseManager.isConfigured) {
                syncFavouriteDoctor(fav)
                fetchAndSyncFavourites(patientId)
            }
            logAction("Add Favourite", "Added doctor ID $doctorId to patient ID $patientId favourites.")
        }
    }

    suspend fun isDoctorFavourite(patientId: String, doctorId: String): Boolean {
        return dao.getFavourite(patientId, doctorId) != null
    }

    private suspend fun syncFavouriteDoctor(fav: FavouriteDoctorEntity) {
        val payload = """
            {
                "id": "${fav.id}",
                "patient_id": "${fav.patientId}",
                "doctor_id": "${fav.doctorId}",
                "created_at": ${fav.createdAt}
            }
        """.trimIndent()
        SupabaseManager.insertOrUpdate("favourite_doctors", payload)
    }

    suspend fun fetchAndSyncFavourites(patientId: String) {
        if (SupabaseManager.isConfigured) {
            val remoteFavourites = SupabaseManager.fetchFavourites(patientId)
            dao.deleteFavouritesForPatient(patientId)
            remoteFavourites.forEach { fav ->
                dao.insertFavourite(fav)
            }
            logAction("Sync Favourites", "Synced ${remoteFavourites.size} favourites from Supabase for patient $patientId.")
        }
    }

    suspend fun updateDoctorAvailability(doctorId: String, status: String, expectedStartTime: String, delayReason: String) {
        val doctor = getDoctorById(doctorId)
        if (doctor != null) {
            val updatedDoctor = doctor.copy(
                availabilityStatus = status,
                expectedStartTime = expectedStartTime,
                delayReason = delayReason
            )
            
            // Update doctor in local Room DB
            dao.updateDoctor(updatedDoctor)
            
            // Sync updated doctor status to Supabase
            if (SupabaseManager.isConfigured) {
                syncDoctor(updatedDoctor)
                // Fetch doctors list again to broadcast changes
                fetchAndSyncDoctors()
            } else {
                // local broadcast simulation
                val currentList = _allDoctors.value.toMutableList()
                val idx = currentList.indexOfFirst { it.id == doctorId }
                if (idx != -1) {
                    currentList[idx] = updatedDoctor
                    _allDoctors.value = currentList
                }
                val currentActiveList = _activeDoctors.value.toMutableList()
                val idxActive = currentActiveList.indexOfFirst { it.id == doctorId }
                if (idxActive != -1) {
                    currentActiveList[idxActive] = updatedDoctor
                    _activeDoctors.value = currentActiveList
                }
            }

            logAction("Update Doctor Availability", "Doctor ${doctor.name} availability updated to: $status. Reason: $delayReason")

            // Automatically notify all affected patients if doctor is "Running Late"
            if (status == "Running Late") {
                val todayDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                val affectedBookings = dao.getBookingsByDoctorList(doctorId).filter { 
                    it.dateStr == todayDateStr && it.status == "Upcoming" 
                }

                affectedBookings.forEach { booking ->
                    val clinicianName = doctor.name
                    val messageText = "Dr. $clinicianName will arrive at $expectedStartTime instead of 5:00 PM. Reason: $delayReason"
                    
                    val notification = NotificationEntity(
                        title = "Delay Alert: Dr. $clinicianName",
                        message = messageText,
                        timestamp = System.currentTimeMillis()
                    )
                    
                    // Store locally
                    dao.insertNotification(notification)
                    
                    // Sync to Supabase
                    if (SupabaseManager.isConfigured) {
                        syncNotification(notification)
                    }
                }
                logAction("Delay Notifications Sent", "Sent automatic delay alert to ${affectedBookings.size} affected patients.")
            }
        }
    }

    private suspend fun syncNotification(notif: NotificationEntity) {
        val payload = """
            {
                "id": "${notif.id}",
                "title": "${notif.title.replace("\"", "\\\"")}",
                "message": "${notif.message.replace("\"", "\\\"")}",
                "timestamp": ${notif.timestamp},
                "is_read": ${notif.isRead}
            }
        """.trimIndent()
        SupabaseManager.insertOrUpdate("notifications", payload)
    }

    // Pricing Settings
    val pricingSettings: Flow<PricingSettingsEntity?> = dao.getPricingSettingsFlow()

    suspend fun getPricingSettings(): PricingSettingsEntity? = dao.getPricingSettings()

    suspend fun savePricingSettings(settings: PricingSettingsEntity) {
        dao.insertPricingSettings(settings)
        logAction("Update Pricing Settings", "Registration: ₹${settings.registrationFee}, Monthly Sub: ₹${settings.monthlySubscriptionFee}, Quarterly Sub: ₹${settings.quarterlySubscriptionFee}, Yearly Sub: ₹${settings.yearlySubscriptionFee}")
    }
}
