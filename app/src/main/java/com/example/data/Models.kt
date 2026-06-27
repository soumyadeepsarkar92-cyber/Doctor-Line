package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.UUID

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["isLoggedIn"])
    ]
)
data class UserEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val phone: String,
    val role: String, // "Patient", "Pharmacy", "Admin", "Doctor"
    val isLoggedIn: Boolean = false,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val sessionExpiresAt: Long? = null,
    val deviceId: String? = null,
    val failedLoginAttempts: Int = 0,
    val accountLockedUntil: Long? = null,
    val emailVerified: Boolean = false,
    val profilePhotoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "pharmacies",
    indices = [
        Index(value = ["license"], unique = true),
        Index(value = ["status"])
    ]
)
data class PharmacyEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val address: String,
    val phone: String,
    val license: String,
    val bannerName: String, 
    val ownerName: String = "Dr. Amit Patra",
    val status: String = "Active", // "Active", "Suspended", "Inactive"
    val createdDate: String = "2026-06-22",
    val subscriptionPlan: String = "Basic", // "Basic", "Premium", "Enterprise"
    val subscriptionStart: String = "2026-06-22",
    val subscriptionExpiry: String = "2026-07-22",
    val subscriptionAmount: Double = 499.0,
    val subscriptionPaymentStatus: String = "Paid",
    val trialStarted: Boolean = false,
    val trialStartDate: String = "",
    val trialEndDate: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "doctors",
    foreignKeys = [
        ForeignKey(
            entity = PharmacyEntity::class,
            parentColumns = ["id"],
            childColumns = ["pharmacyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["pharmacyId"]),
        Index(value = ["specialization"]),
        Index(value = ["isEnabled", "isSoftDeleted"])
    ]
)
data class DoctorEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val specialization: String,
    val experience: Int, // years
    val fee: Double,
    val rating: Double,
    val pharmacyId: String,
    val bannerName: String,
    val slotsJson: String, 
    val isEnabled: Boolean = true,
    val photoUrl: String = "",
    val bio: String = "Dedicated health professional focused on high-quality patient outcomes and modern diagnostics.",
    val degree: String = "MBBS, MD",
    val languages: String = "English, Hindi, Bengali",
    val isSoftDeleted: Boolean = false,
    val availabilityStatus: String = "Available", // "Available", "Running Late", "On Holiday"
    val expectedStartTime: String = "",
    val delayReason: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = DoctorEntity::class,
            parentColumns = ["id"],
            childColumns = ["doctorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["doctorId"]),
        Index(value = ["doctorId", "dateStr"]),
        Index(value = ["dateStr"])
    ]
)
data class ScheduleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val doctorId: String,
    val dateStr: String, // YYYY-MM-DD
    val fromTimeStr: String,
    val toTimeStr: String,
    val maxPatients: Int,
    val isOpenForBooking: Boolean,
    val isHoliday: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(
            entity = DoctorEntity::class,
            parentColumns = ["id"],
            childColumns = ["doctorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["doctorId"]),
        Index(value = ["patientPhone"]),
        Index(value = ["doctorId", "dateStr", "status"]),
        Index(value = ["dateStr"])
    ]
)
data class BookingEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val tokenNumber: Int,
    val patientName: String,
    val patientPhone: String,
    val age: Int,
    val gender: String,
    val doctorId: String,
    val dateStr: String,
    val timeStr: String,
    val status: String, // "Upcoming", "Completed", "Cancelled"
    val paymentMode: String, // "Pay Online", "Pay at Pharmacy"
    val paymentStatus: String, // "Pending", "Paid"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "subscriptions",
    foreignKeys = [
        ForeignKey(
            entity = PharmacyEntity::class,
            parentColumns = ["id"],
            childColumns = ["pharmacyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["pharmacyId"], unique = true)
    ]
)
data class SubscriptionEntity(
    @PrimaryKey val id: String = "sub_p_1",
    val pharmacyId: String,
    val currentPlan: String = "Standard Monthly Plan",
    val price: Double = 299.0,
    val validityDate: String, // e.g. "2026-07-20"
    val autoRenewal: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = BookingEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bookingId"], unique = true)
    ]
)
data class PaymentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val bookingId: String,
    val amount: Double,
    val status: String,
    val dateStr: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["isRead", "timestamp"]),
        Index(value = ["timestamp"])
    ]
)
data class NotificationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "audit_logs",
    indices = [
        Index(value = ["timestamp"])
    ]
)
data class AuditLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = DoctorEntity::class,
            parentColumns = ["id"],
            childColumns = ["doctorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["doctorId"]),
        Index(value = ["patientId"]),
        Index(value = ["doctorId", "rating"])
    ]
)
data class ReviewEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val patientName: String,
    val doctorId: String,
    val rating: Int,
    val review: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "pharmacy_requests",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["licenseNo"], unique = true),
        Index(value = ["status"])
    ]
)
data class PharmacyRequestEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val pharmacyName: String,
    val ownerName: String,
    val licenseNo: String,
    val mobile: String,
    val email: String,
    val passwordHash: String,
    val address: String,
    val licenseImage: String,
    val pharmacyPhoto: String?,
    val status: String = "pending", // "pending", "approved", "rejected", "pending_verification", "correction_requested"
    val approvedAt: Long? = null,
    val approvedBy: String? = null,
    val paymentId: String? = null,
    val paymentStatus: String? = null,
    val paymentAmount: Double? = null,
    val paymentDate: Long? = null,
    val rejectionReason: String? = null,
    val correctionNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "favourite_doctors",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DoctorEntity::class,
            parentColumns = ["id"],
            childColumns = ["doctorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["patientId", "doctorId"], unique = true),
        Index(value = ["doctorId"])
    ]
)
data class FavouriteDoctorEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val doctorId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pricing_settings")
data class PricingSettingsEntity(
    @PrimaryKey val id: String = "default_pricing",
    val registrationFee: Double = 10.0,
    val monthlySubscriptionFee: Double = 10.0,
    val quarterlySubscriptionFee: Double = 30.0,
    val yearlySubscriptionFee: Double = 100.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// Appointments Audit & History & Notifications
@Entity(
    tableName = "appointment_audit_logs",
    foreignKeys = [
        ForeignKey(
            entity = BookingEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookingId"])]
)
data class AppointmentAuditLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val bookingId: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "appointment_notifications",
    foreignKeys = [
        ForeignKey(
            entity = BookingEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookingId"])]
)
data class AppointmentNotificationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val bookingId: String,
    val message: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "appointment_history",
    foreignKeys = [
        ForeignKey(
            entity = BookingEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookingId"])]
)
data class AppointmentHistoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val bookingId: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// Payments Audit & History & Notifications
@Entity(
    tableName = "payment_audit_logs",
    foreignKeys = [
        ForeignKey(
            entity = PaymentEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["paymentId"])]
)
data class PaymentAuditLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val paymentId: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "payment_notifications",
    foreignKeys = [
        ForeignKey(
            entity = PaymentEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["paymentId"])]
)
data class PaymentNotificationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val paymentId: String,
    val message: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "payment_tracking_history",
    foreignKeys = [
        ForeignKey(
            entity = PaymentEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["paymentId"])]
)
data class PaymentTrackingHistoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val paymentId: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// Pharmacy Approvals Audit & History & Notifications
@Entity(
    tableName = "pharmacy_approval_audit_logs",
    foreignKeys = [
        ForeignKey(
            entity = PharmacyRequestEntity::class,
            parentColumns = ["id"],
            childColumns = ["requestId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["requestId"])]
)
data class PharmacyApprovalAuditLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val requestId: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "pharmacy_approval_notifications",
    foreignKeys = [
        ForeignKey(
            entity = PharmacyRequestEntity::class,
            parentColumns = ["id"],
            childColumns = ["requestId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["requestId"])]
)
data class PharmacyApprovalNotificationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val requestId: String,
    val message: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "pharmacy_approval_history",
    foreignKeys = [
        ForeignKey(
            entity = PharmacyRequestEntity::class,
            parentColumns = ["id"],
            childColumns = ["requestId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["requestId"])]
)
data class PharmacyApprovalHistoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val requestId: String,
    val status: String,
    val notes: String,
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
