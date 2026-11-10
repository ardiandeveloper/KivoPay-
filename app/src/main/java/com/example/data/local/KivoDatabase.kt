package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.dao.AdminConfigDao
import com.example.data.dao.AuditLogDao
import com.example.data.dao.BannerDao
import com.example.data.dao.BankChangeRequestDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.UserDao
import com.example.data.model.AdminConfigEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.BannerEntity
import com.example.data.model.BankChangeRequestEntity
import com.example.data.model.BankChangeStatus
import com.example.data.model.KycStatus
import com.example.data.model.NotificationEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserEntity

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = runCatching { TransactionType.valueOf(value) }.getOrDefault(TransactionType.PAYMENT)

    @TypeConverter
    fun fromTransactionStatus(status: TransactionStatus): String = status.name

    @TypeConverter
    fun toTransactionStatus(value: String): TransactionStatus = runCatching { TransactionStatus.valueOf(value) }.getOrDefault(TransactionStatus.SUCCESS)

    @TypeConverter
    fun fromKycStatus(status: KycStatus): String = status.name

    @TypeConverter
    fun toKycStatus(value: String): KycStatus = runCatching { KycStatus.valueOf(value) }.getOrDefault(KycStatus.PENDING)

    @TypeConverter
    fun fromBankChangeStatus(status: BankChangeStatus): String = status.name

    @TypeConverter
    fun toBankChangeStatus(value: String): BankChangeStatus = runCatching { BankChangeStatus.valueOf(value) }.getOrDefault(BankChangeStatus.PENDING_CS)

    @TypeConverter
    fun fromKivoPlusStatus(status: com.example.data.model.KivoPlusStatus): String = status.name

    @TypeConverter
    fun toKivoPlusStatus(value: String): com.example.data.model.KivoPlusStatus = runCatching { com.example.data.model.KivoPlusStatus.valueOf(value) }.getOrDefault(com.example.data.model.KivoPlusStatus.NOT_UPGRADED)
}

@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        NotificationEntity::class,
        BannerEntity::class,
        AdminConfigEntity::class,
        AuditLogEntity::class,
        BankChangeRequestEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KivoDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun notificationDao(): NotificationDao
    abstract fun bannerDao(): BannerDao
    abstract fun adminConfigDao(): AdminConfigDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun bankChangeRequestDao(): BankChangeRequestDao

    companion object {
        @Volatile
        private var INSTANCE: KivoDatabase? = null

        fun getInstance(context: Context): KivoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KivoDatabase::class.java,
                    "kivopay_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
