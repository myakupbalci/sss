package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DoseRecord
import com.example.data.model.DoseReminder
import com.example.data.model.DoseType
import com.example.data.model.InsulinStock
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseDao {
    // Dose records
    @Query("SELECT * FROM dose_records ORDER BY timestamp DESC")
    fun getAllDosesFlow(): Flow<List<DoseRecord>>

    @Query("SELECT * FROM dose_records ORDER BY timestamp DESC")
    suspend fun getAllDosesList(): List<DoseRecord>

    @Query("SELECT * FROM dose_records WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay ORDER BY timestamp DESC")
    fun getDosesForRangeFlow(startOfDay: Long, endOfDay: Long): Flow<List<DoseRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoses(records: List<DoseRecord>)

    @Query("DELETE FROM dose_records")
    suspend fun deleteAllDoses()

    @Query("SELECT * FROM dose_records ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastDose(): DoseRecord?

    @Query("SELECT * FROM dose_records ORDER BY timestamp DESC LIMIT 1")
    fun getLastDoseFlow(): Flow<DoseRecord?>

    @Query("SELECT * FROM dose_records WHERE type = :type ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastDoseOfType(type: DoseType): DoseRecord?

    @Query("SELECT * FROM dose_records WHERE id = :id")
    suspend fun getDoseById(id: Long): DoseRecord?

    @Query("UPDATE dose_records SET bloodGlucose = :glucose WHERE id = :id")
    suspend fun updateDoseBloodGlucose(id: Long, glucose: Int)

    @Query("UPDATE dose_records SET postDoseGlucose = :glucose WHERE id = :id")
    suspend fun updateDosePostGlucose(id: Long, glucose: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDose(record: DoseRecord): Long

    @Update
    suspend fun updateDose(record: DoseRecord)

    @Delete
    suspend fun deleteDose(record: DoseRecord)

    // Reminders
    @Query("SELECT * FROM dose_reminders ORDER BY hour ASC, minute ASC")
    fun getAllRemindersFlow(): Flow<List<DoseReminder>>

    @Query("SELECT * FROM dose_reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): DoseReminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: DoseReminder): Long

    @Update
    suspend fun updateReminder(reminder: DoseReminder)

    @Delete
    suspend fun deleteReminder(reminder: DoseReminder)

    // Stocks
    @Query("SELECT * FROM insulin_stocks ORDER BY id ASC")
    fun getAllStocksFlow(): Flow<List<InsulinStock>>

    @Query("SELECT * FROM insulin_stocks WHERE type = :type LIMIT 1")
    suspend fun getStockByType(type: DoseType): InsulinStock?

    @Query("SELECT * FROM insulin_stocks WHERE type = :type LIMIT 1")
    fun getStockByTypeFlow(type: DoseType): Flow<InsulinStock?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: InsulinStock): Long

    @Update
    suspend fun updateStock(stock: InsulinStock)

    @Query("UPDATE insulin_stocks SET totalUnitsRemaining = MAX(0.0, totalUnitsRemaining - :units), lastUpdated = :now WHERE type = :type")
    suspend fun decrementStock(type: DoseType, units: Double, now: Long = System.currentTimeMillis())
}
