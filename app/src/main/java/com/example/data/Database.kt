package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.data.payment.PaymentHistoryRecord

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
        PharmacyRequestEntity::class,
        FavouriteDoctorEntity::class,
        PricingSettingsEntity::class,
        PaymentHistoryRecord::class,
        AppointmentAuditLogEntity::class,
        AppointmentNotificationEntity::class,
        AppointmentHistoryEntity::class,
        PaymentAuditLogEntity::class,
        PaymentNotificationEntity::class,
        PaymentTrackingHistoryEntity::class,
        PharmacyApprovalAuditLogEntity::class,
        PharmacyApprovalNotificationEntity::class,
        PharmacyApprovalHistoryEntity::class
    ],
    version = 17,
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
                createUpdateTriggers(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.doctorLineDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                createUpdateTriggers(db)
            }

            private fun createUpdateTriggers(db: SupportSQLiteDatabase) {
                val tables = listOf(
                    "users", "pharmacies", "doctors", "schedules", "bookings",
                    "subscriptions", "payments", "notifications", "reviews",
                    "pharmacy_requests", "pricing_settings", "appointment_audit_logs",
                    "appointment_notifications", "appointment_history", "payment_audit_logs",
                    "payment_notifications", "payment_tracking_history", "pharmacy_approval_audit_logs",
                    "pharmacy_approval_notifications", "pharmacy_approval_history"
                )
                tables.forEach { table ->
                    db.execSQL("""
                        CREATE TRIGGER IF NOT EXISTS tr_${table}_updated_at
                        AFTER UPDATE ON $table
                        FOR EACH ROW
                        WHEN NEW.updatedAt <= OLD.updatedAt OR NEW.updatedAt IS NULL
                        BEGIN
                            UPDATE $table SET updatedAt = (strftime('%s', 'now') * 1000) WHERE id = NEW.id;
                        END;
                    """.trimIndent())
                }

                // payment_history table trigger (using paymentId instead of id)
                db.execSQL("""
                    CREATE TRIGGER IF NOT EXISTS tr_payment_history_updated_at
                    AFTER UPDATE ON payment_history
                    FOR EACH ROW
                    WHEN NEW.updatedAt <= OLD.updatedAt OR NEW.updatedAt IS NULL
                    BEGIN
                        UPDATE payment_history SET updatedAt = (strftime('%s', 'now') * 1000) WHERE paymentId = NEW.paymentId;
                    END;
                """.trimIndent())
            }
        }

        suspend fun populateDatabase(dao: DoctorLineDao) {
            // Delete all to prevent duplications just in case
            dao.deleteAllUsers()

            // 1. Seed Default Pricing Settings
            dao.insertPricingSettings(
                PricingSettingsEntity(
                    id = "default_pricing",
                    registrationFee = 10.0,
                    monthlySubscriptionFee = 10.0,
                    quarterlySubscriptionFee = 30.0,
                    yearlySubscriptionFee = 100.0
                )
            )
        }
    }
}
