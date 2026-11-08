package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BankChangeRequestEntity
import com.example.data.model.BankChangeStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BankChangeRequestDao {
    @Query("SELECT * FROM bank_change_requests ORDER BY timestamp DESC")
    fun getAllRequestsFlow(): Flow<List<BankChangeRequestEntity>>

    @Query("SELECT * FROM bank_change_requests WHERE userId = :userId ORDER BY timestamp DESC")
    fun getRequestsByUserFlow(userId: String): Flow<List<BankChangeRequestEntity>>

    @Query("SELECT * FROM bank_change_requests WHERE id = :id LIMIT 1")
    suspend fun getRequestById(id: String): BankChangeRequestEntity?

    @Query("SELECT * FROM bank_change_requests WHERE userId = :userId AND status = 'PENDING_CS' LIMIT 1")
    suspend fun getActivePendingRequestForUser(userId: String): BankChangeRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: BankChangeRequestEntity)

    @Update
    suspend fun updateRequest(request: BankChangeRequestEntity)

    @Query("UPDATE bank_change_requests SET status = :status, rejectionReason = :reason WHERE id = :id")
    suspend fun updateRequestStatus(id: String, status: BankChangeStatus, reason: String = "")
}
