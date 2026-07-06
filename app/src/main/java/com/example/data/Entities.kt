package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String = "me",
    val name: String,
    val nativeLanguage: String = "Indonesia",
    val nativeLanguageCode: String = "id",
    val homeCountry: String = "Indonesia",
    val homeCountryCode: String = "ID",
    val homeFlagEmoji: String = "🇮🇩",
    val milesTravelled: Int = 0,
    val currentCountry: String? = null,
    val avatarUrl: String = "",
    val bio: String = "Halo! Saya suka bertualang, belajar budaya baru, dan mengobrol dengan orang-orang dari seluruh dunia! 🌎"
)

@Entity(tableName = "chat_partner")
data class ChatPartner(
    @PrimaryKey val id: String,
    val name: String,
    val country: String,
    val countryCode: String,
    val flagEmoji: String,
    val bio: String,
    val nativeLanguage: String,
    val nativeLanguageCode: String,
    val city: String,
    val localTimeOffset: Int,
    val distanceKm: Int,
    val avatarColorSeed: Int // To generate a beautiful, consistent dynamic color avatar
)

@Entity(tableName = "user_message")
data class UserMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: String,
    val partnerId: String,
    val messageText: String,
    val translatedText: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null
)
