package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val phone: String,
    val role: String, // "Patient", "Pharmacy", "Admin"
    val isLoggedIn: Boolean = false
)

@Entity(tableName = "pharmacies")
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
    val subscriptionPaymentStatus: String = "Paid"
)

@Entity(tableName = "doctors")
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
    val languages: String = "English, Hindi, Bengali"
)

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val doctorId: String,
    val dateStr: String, // YYYY-MM-DD
    val fromTimeStr: String,
    val toTimeStr: String,
    val maxPatients: Int,
    val isOpenForBooking: Boolean,
    val isHoliday: Boolean = false
)

@Entity(tableName = "bookings")
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
    val paymentStatus: String // "Pending", "Paid"
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val id: String = "sub_p_1",
    val pharmacyId: String,
    val currentPlan: String = "Standard Monthly Plan",
    val price: Double = 299.0,
    val validityDate: String, // e.g. "2026-07-20"
    val autoRenewal: Boolean = true
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val bookingId: String,
    val amount: Double,
    val status: String,
    val dateStr: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
