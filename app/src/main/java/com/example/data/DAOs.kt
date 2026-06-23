package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DoctorLineDao {
    // Users
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getActiveUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun clearUserLogin()

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    // Pharmacies
    @Query("SELECT * FROM pharmacies")
    fun getAllPharmacies(): Flow<List<PharmacyEntity>>

    @Query("SELECT * FROM pharmacies WHERE id = :id")
    suspend fun getPharmacyById(id: String): PharmacyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPharmacy(pharmacy: PharmacyEntity)

    @Update
    suspend fun updatePharmacy(pharmacy: PharmacyEntity)

    @Query("DELETE FROM pharmacies WHERE id = :id")
    suspend fun deletePharmacyById(id: String)

    // Doctors
    @Query("SELECT * FROM doctors")
    fun getAllDoctorsFlow(): Flow<List<DoctorEntity>>

    @Query("SELECT * FROM doctors WHERE isEnabled = 1")
    fun getActiveDoctorsFlow(): Flow<List<DoctorEntity>>

    @Query("SELECT * FROM doctors WHERE id = :id")
    suspend fun getDoctorById(id: String): DoctorEntity?

    @Query("SELECT * FROM doctors WHERE pharmacyId = :pharmacyId")
    fun getDoctorsByPharmacyFlow(pharmacyId: String): Flow<List<DoctorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctor(doctor: DoctorEntity)

    @Update
    suspend fun updateDoctor(doctor: DoctorEntity)

    // Schedules
    @Query("SELECT * FROM schedules WHERE doctorId = :doctorId ORDER BY dateStr ASC")
    fun getSchedulesForDoctorFlow(doctorId: String): Flow<List<ScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity)

    @Query("UPDATE schedules SET isHoliday = :isHoliday WHERE id = :id")
    suspend fun markHoliday(id: String, isHoliday: Boolean)

    // Bookings
    @Query("SELECT * FROM bookings ORDER BY dateStr DESC, timeStr DESC")
    fun getAllBookingsFlow(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE doctorId = :doctorId")
    fun getBookingsByDoctorFlow(doctorId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE patientPhone = :phone")
    fun getBookingsByPatientPhone(phone: String): Flow<List<BookingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    // Subscriptions
    @Query("SELECT * FROM subscriptions WHERE pharmacyId = :pharmacyId LIMIT 1")
    fun getSubscriptionForPharmacy(pharmacyId: String): Flow<SubscriptionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity)

    @Query("UPDATE subscriptions SET autoRenewal = :autoRenew WHERE id = :id")
    suspend fun toggleSubscriptionRenewal(id: String, autoRenew: Boolean)

    // Payments
    @Query("SELECT * FROM payments")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotificationsFlow(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: String)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // Reviews
    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun getAllReviewsFlow(): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE doctorId = :doctorId ORDER BY createdAt DESC")
    fun getReviewsForDoctorFlow(doctorId: String): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getReviewsForPatientFlow(patientId: String): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Update
    suspend fun updateReview(review: ReviewEntity)

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun deleteReviewById(id: String)

    // Pharmacy approval requests
    @Query("SELECT * FROM pharmacy_requests ORDER BY createdAt DESC")
    fun getAllPharmacyRequests(): Flow<List<PharmacyRequestEntity>>

    @Query("SELECT * FROM pharmacy_requests WHERE email = :email LIMIT 1")
    suspend fun getPharmacyRequestByEmail(email: String): PharmacyRequestEntity?

    @Query("SELECT * FROM pharmacy_requests WHERE licenseNo = :licenseNo LIMIT 1")
    suspend fun getPharmacyRequestByLicense(licenseNo: String): PharmacyRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPharmacyRequest(request: PharmacyRequestEntity)

    @Update
    suspend fun updatePharmacyRequest(request: PharmacyRequestEntity)
}
