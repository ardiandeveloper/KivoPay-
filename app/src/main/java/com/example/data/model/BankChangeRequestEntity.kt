package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BankChangeStatus {
    PENDING_CS,
    APPROVED,
    REJECTED
}

@Entity(tableName = "bank_change_requests")
data class BankChangeRequestEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userFullName: String,
    val userEmail: String,
    val userPhone: String,
    val oldBankName: String,
    val oldAccountNumber: String,
    val oldAccountHolder: String,
    val newBankName: String,
    val newAccountNumber: String,
    val newAccountHolder: String,
    val changeReason: String,
    val ktpDocumentUri: String,
    val selfieWithKtpUri: String,
    val status: BankChangeStatus = BankChangeStatus.PENDING_CS,
    val rejectionReason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
