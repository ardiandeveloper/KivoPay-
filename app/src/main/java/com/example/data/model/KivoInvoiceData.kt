package com.example.data.model

data class KivoInvoiceData(
    val orderId: String,
    val externalId: String,
    val userId: String,
    val amount: Double,
    val totalAmount: Double,
    val channelCode: String,
    val channelName: String,
    val paymentUrl: String?,
    val qrisUrl: String?,
    val qrisString: String?,
    val vaNumber: String?,
    val customerName: String,
    val customerEmail: String,
    val status: String = "PENDING", // "PENDING", "PAID", "EXPIRED"
    val createdAt: Long = System.currentTimeMillis(),
    val expirySeconds: Long = 900L // 15 mins
)
