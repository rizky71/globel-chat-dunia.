package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Query("SELECT * FROM chat_partner")
    fun getChatPartners(): Flow<List<ChatPartner>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatPartner(partner: ChatPartner)

    @Query("SELECT * FROM chat_partner WHERE id = :partnerId LIMIT 1")
    suspend fun getChatPartnerById(partnerId: String): ChatPartner?

    @Query("SELECT * FROM user_message WHERE partnerId = :partnerId ORDER BY timestamp ASC")
    fun getMessagesForPartner(partnerId: String): Flow<List<UserMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: UserMessage)

    @Query("SELECT * FROM user_message ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<UserMessage>>

    @Query("DELETE FROM user_message WHERE partnerId = :partnerId")
    suspend fun deleteMessagesForPartner(partnerId: String)

    @Query("DELETE FROM chat_partner WHERE id = :partnerId")
    suspend fun deletePartner(partnerId: String)

    @Query("DELETE FROM chat_partner")
    suspend fun deleteAllPartners()

    @Query("DELETE FROM user_message")
    suspend fun deleteAllMessages()
}

@Database(entities = [UserProfile::class, ChatPartner::class, UserMessage::class], version = 4, exportSchema = false)
abstract class AbloDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AbloDatabase? = null

        fun getDatabase(context: Context): AbloDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AbloDatabase::class.java,
                    "ablo_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
