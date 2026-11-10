package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class KycStatus {
    PENDING,
    APPROVED,
    REJECTED
}

enum class KivoPlusStatus {
    NOT_UPGRADED,
    PENDING_VERIFICATION,
    APPROVED,
    REJECTED
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, // e.g. "usr_1"
    val email: String,
    val fullName: String,
    val phoneNumber: String,
    val accountId: String, // 10-digit KivoPay Account / Virtual Account No
    val pin: String, // 6-digit transaction PIN
    val password: String,
    val isAdmin: Boolean = false,
    val saldoAvailable: Double = 0.0,
    val saldoHold: Double = 0.0,
    val isKycVerified: Boolean = false,
    val avatarUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),

    // Bank Account & Buku Tabungan Verification fields
    val userBankName: String = "",
    val userBankAccountNumber: String = "",
    val userBankAccountHolder: String = "",
    val bankBookDocumentUri: String = "", // URI or camera capture file path
    val kycStatus: KycStatus = KycStatus.PENDING,
    val kycRejectionReason: String = "",

    // KivoPay+ Upgrade Tier Fields
    val isKivoPlus: Boolean = false,
    val kivoPlusStatus: KivoPlusStatus = KivoPlusStatus.NOT_UPGRADED,
    val kivoPlusNik: String = "",
    val kivoPlusKtpUri: String = "", // Camera capture of e-KTP
    val kivoPlusSelfieUri: String = "", // Camera capture selfie with e-KTP
    val kivoPlusRejectionReason: String = "",
    val kivoPlusRequestedAt: Long = 0L
)

