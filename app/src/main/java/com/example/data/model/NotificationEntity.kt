package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // "TRANSACTION", "SYSTEM", "SECURITY", "PROMO"
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
