package com.oqba26.abzarforoush.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete

@Entity(tableName = "pending_deletions")
data class PendingDeletion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tableName: String,
    val identifier: String, // String representation of the ID
    val identifierColumn: String = "id"
)

@Dao
interface PendingDeletionDao {
    @Insert
    suspend fun insert(deletion: PendingDeletion)

    @Query("SELECT * FROM pending_deletions")
    suspend fun getAll(): List<PendingDeletion>

    @Delete
    suspend fun delete(deletion: PendingDeletion)
}
