package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.KycStatus
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserByIdFlow(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE accountId = :accountId LIMIT 1")
    suspend fun getUserByAccountId(accountId: String): UserEntity?

    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE kycStatus = 'PENDING' AND isAdmin = 0 ORDER BY createdAt DESC")
    fun getPendingKycUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET saldoAvailable = :newSaldoAvailable, saldoHold = :newSaldoHold WHERE id = :userId")
    suspend fun updateBalances(userId: String, newSaldoAvailable: Double, newSaldoHold: Double)

    @Query("UPDATE users SET kycStatus = :status, isKycVerified = :isVerified, kycRejectionReason = :reason WHERE id = :userId")
    suspend fun updateKycStatus(userId: String, status: KycStatus, isVerified: Boolean, reason: String)
}
