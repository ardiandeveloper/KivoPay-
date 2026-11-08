package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "banners")
data class BannerEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val badgeText: String = "PROMO",
    val bgHexColor: String = "#0A192F",
    val actionUrl: String = "",
    val isActive: Boolean = true
)
