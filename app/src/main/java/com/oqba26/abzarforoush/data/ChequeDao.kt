package com.oqba26.abzarforoush.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChequeDao {
    @Query("SELECT * FROM cheques ORDER BY dueDate ASC")
    fun getAllCheques(): Flow<List<Cheque>>

    @Query("SELECT * FROM cheques")
    suspend fun getAllChequesList(): List<Cheque>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheque(cheque: Cheque)

    @Update
    suspend fun updateCheque(cheque: Cheque)

    @Delete
    suspend fun deleteCheque(cheque: Cheque)
}
