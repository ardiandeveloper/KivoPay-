package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_configs")
data class AdminConfigEntity(
    @PrimaryKey val keyName: String, // e.g. "withdraw_fee", "transfer_interbank_fee"
    val value: String,
    val description: String = ""
)
