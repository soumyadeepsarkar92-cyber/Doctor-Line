package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        PharmacyEntity::class,
        DoctorEntity::class,
        ScheduleEntity::class,
        BookingEntity::class,
        SubscriptionEntity::class,
        PaymentEntity::class,
        NotificationEntity::class,
        AuditLogEntity::class,
        ReviewEntity::class,
        PharmacyRequestEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class DoctorLineDatabase : RoomDatabase() {
    abstract fun doctorLineDao(): DoctorLineDao

    companion object {
        @Volatile
        private var INSTANCE: DoctorLineDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): DoctorLineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DoctorLineDatabase::class.java,
                    "doctorline_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.doctorLineDao())
                    }
                }
            }
        }

        suspend fun populateDatabase(dao: DoctorLineDao) {
            // Delete all to prevent duplications just in case
            dao.deleteAllUsers()

            // 1. Seed Pharmacies
            val phar1Id = "phar_apollo"
            val phar2Id = "phar_medplus"
            val phar3Id = "phar_care"

            dao.insertPharmacy(
                PharmacyEntity(
                    id = phar1Id,
                    name = "Apollo Pharmacy - Lifeline",
                    address = "Sector 5, Salt Lake, Kolkata, India",
                    phone = "+91 98765 43210",
                    license = "DL-9087-A/2026",
                    bannerName = "apollo"
                )
            )
            dao.insertPharmacy(
                PharmacyEntity(
                    id = phar2Id,
                    name = "MedPlus Wellness Hub",
                    address = "Gachibowli Main Rd, Hyderabad, India",
                    phone = "+91 87654 32109",
                    license = "DL-7612-M/2025",
                    bannerName = "medplus"
                )
            )
            dao.insertPharmacy(
                PharmacyEntity(
                    id = phar3Id,
                    name = "Care & Cure Pharmacy",
                    address = "MG Road, Bengaluru, India",
                    phone = "+91 76543 21098",
                    license = "DL-4532-C/2026",
                    bannerName = "care"
                )
            )

            // 2. Seed Doctors
            val doc1 = DoctorEntity(
                id = "doc1",
                name = "Dr. Ananya Roy",
                specialization = "Cardiologist",
                experience = 12,
                fee = 800.0,
                rating = 4.9,
                pharmacyId = phar1Id,
                bannerName = "doctor_female_1",
                slotsJson = "09:00 AM, 10:30 AM, 12:00 PM, 04:30 PM, 06:00 PM"
            )
            val doc2 = DoctorEntity(
                id = "doc2",
                name = "Dr. Vikram Seth",
                specialization = "Pediatrician",
                experience = 8,
                fee = 500.0,
                rating = 4.7,
                pharmacyId = phar1Id,
                bannerName = "doctor_male_1",
                slotsJson = "10:00 AM, 11:30 AM, 02:00 PM, 03:30 PM"
            )
            val doc3 = DoctorEntity(
                id = "doc3",
                name = "Dr. Aisha Sharma",
                specialization = "Dermatologist",
                experience = 10,
                fee = 650.0,
                rating = 4.8,
                pharmacyId = phar2Id,
                bannerName = "doctor_female_2",
                slotsJson = "09:30 AM, 11:00 AM, 01:30 PM, 03:00 PM, 05:00 PM"
            )
            val doc4 = DoctorEntity(
                id = "doc4",
                name = "Dr. Rajesh K. Patel",
                specialization = "General Physician",
                experience = 15,
                fee = 400.0,
                rating = 4.6,
                pharmacyId = phar3Id,
                bannerName = "doctor_male_2",
                slotsJson = "08:00 AM, 09:30 AM, 11:00 AM, 05:00 PM, 06:30 PM"
            )

            dao.insertDoctor(doc1)
            dao.insertDoctor(doc2)
            dao.insertDoctor(doc3)
            dao.insertDoctor(doc4)

            // 3. Seed Schedules
            dao.insertSchedule(
                ScheduleEntity(
                    id = "sch1",
                    doctorId = "doc1",
                    dateStr = "2026-06-21",
                    fromTimeStr = "09:00 AM",
                    toTimeStr = "01:00 PM",
                    maxPatients = 15,
                    isOpenForBooking = true
                )
            )
            dao.insertSchedule(
                ScheduleEntity(
                    id = "sch2",
                    doctorId = "doc1",
                    dateStr = "2026-06-22",
                    fromTimeStr = "04:00 PM",
                    toTimeStr = "07:00 PM",
                    maxPatients = 12,
                    isOpenForBooking = true
                )
            )
            dao.insertSchedule(
                ScheduleEntity(
                    id = "sch3",
                    doctorId = "doc2",
                    dateStr = "2026-06-21",
                    fromTimeStr = "10:00 AM",
                    toTimeStr = "04:00 PM",
                    maxPatients = 20,
                    isOpenForBooking = true
                )
            )

            // 4. Seed Subscriptions
            dao.insertSubscription(
                SubscriptionEntity(
                    id = "sub_apollo",
                    pharmacyId = phar1Id,
                    currentPlan = "Quarterly Professional",
                    price = 799.0,
                    validityDate = "2026-09-20",
                    autoRenewal = true
                )
            )
            dao.insertSubscription(
                SubscriptionEntity(
                    id = "sub_medplus",
                    pharmacyId = phar2Id,
                    currentPlan = "Monthly Basic",
                    price = 299.0,
                    validityDate = "2026-07-20",
                    autoRenewal = false
                )
            )

            // 5. Seed some initial notifications
            dao.insertNotification(
                NotificationEntity(
                    id = "n1",
                    title = "Welcome to DoctorLine!",
                    message = "Your medical touchpoint is now active. Explore verified pharmacies and book instant appointments.",
                    isRead = false
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    id = "n2",
                    title = "Appointment Reminder",
                    message = "You have an upcoming consultation with Dr. Ananya Roy scheduled for tomorrow at 10:30 AM.",
                    isRead = false
                )
            )

            // 6. Seed a default audit log
            dao.insertAuditLog(
                AuditLogEntity(
                    id = "log1",
                    action = "System Genesis",
                    details = "DoctorLine Local Room Database successfully initialized and pre-seeded with clinic records."
                )
            )
        }
    }
}
