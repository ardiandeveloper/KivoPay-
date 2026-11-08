package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    TOPUP,
    TRANSFER_OUT,
    TRANSFER_IN,
    WITHDRAWAL,
    PAYMENT,
    REFUND
}

enum class TransactionStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    REJECTED
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val transactionId: String, // e.g. "KV-20261107-9842"
    val userId: String,
    val type: TransactionType,
    val category: String, // e.g., "Virtual Account BCA", "Transfer Bank Mandiri", "Tarik Dana", "PLN Electricity"
    val amount: Double,
    val serviceFee: Double,
    val totalAmount: Double, // amount + serviceFee (or amount deducted/added)
    val status: TransactionStatus,
    val recipientName: String = "",
    val recipientAccount: String = "",
    val bankName: String = "",
    val paymentMethod: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
