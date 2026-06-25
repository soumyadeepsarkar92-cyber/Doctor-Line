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
        PharmacyRequestEntity::class,
        FavouriteDoctorEntity::class,
        PricingSettingsEntity::class
    ],
    version = 10,
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
