package com.hydrotrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hydrotrack.data.local.entity.DailyRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: DailyRecordEntity)

    @Update
    suspend fun update(record: DailyRecordEntity)

    @Query("SELECT * FROM daily_records WHERE date = :date")
    suspend fun getRecordForDate(date: String): DailyRecordEntity?

    @Query("SELECT * FROM daily_records WHERE date = :date")
    fun observeRecordForDate(date: String): Flow<DailyRecordEntity?>

    @Query("SELECT * FROM daily_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<DailyRecordEntity>>

    @Query("SELECT * FROM daily_records ORDER BY date DESC")
    suspend fun getAllRecordsOnce(): List<DailyRecordEntity>

    @Query("SELECT * FROM daily_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getRecordsInRange(startDate: String, endDate: String): List<DailyRecordEntity>

    @Query("SELECT * FROM daily_records WHERE goalMet = 1 ORDER BY date DESC")
    suspend fun getSuccessfulDays(): List<DailyRecordEntity>

    @Query("SELECT COUNT(*) FROM daily_records WHERE goalMet = 1")
    fun getSuccessfulDayCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_records WHERE goalMet = 1")
    suspend fun getSuccessfulDayCountOnce(): Int

    @Query("DELETE FROM daily_records")
    suspend fun deleteAll()
}
