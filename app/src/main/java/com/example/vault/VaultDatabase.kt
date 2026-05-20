package com.example.vault

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// --- Data Models ---

@Entity(tableName = "vault_media")
data class VaultMedia(
    @PrimaryKey val id: String, // typically UUID or filename
    val filename: String,
    val mimeType: String,
    val type: String, // "image" or "video"
    val timestamp: Long = System.currentTimeMillis()
)

// --- Daos ---

@Dao
interface VaultMediaDao {
    @Query("SELECT * FROM vault_media ORDER BY timestamp DESC")
    fun getAllMedia(): Flow<List<VaultMedia>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: VaultMedia)

    @Query("DELETE FROM vault_media WHERE id = :id")
    suspend fun deleteMedia(id: String)
}

// --- Database ---

@Database(entities = [VaultMedia::class], version = 1, exportSchema = false)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultMediaDao(): VaultMediaDao

    companion object {
        @Volatile
        private var INSTANCE: VaultDatabase? = null

        fun getDatabase(context: Context): VaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VaultDatabase::class.java,
                    "vault_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
