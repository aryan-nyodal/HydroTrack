package com.hydrotrack.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hydrotrack.data.local.entity.WaterLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: WaterLogEntity): Long

    @Delete
    suspend fun delete(log: WaterLogEntity)

    @Query("SELECT * FROM water_logs WHERE localDate = :date ORDER BY timestamp DESC")
    fun getLogsForDate(date: String): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs WHERE localDate = :date ORDER BY timestamp DESC")
    suspend fun getLogsForDateOnce(date: String): List<WaterLogEntity>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_logs WHERE localDate = :date")
    fun getTotalForDate(date: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_logs WHERE localDate = :date")
    suspend fun getTotalForDateOnce(date: String): Int

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_logs")
    fun getTotalAllTime(): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_logs")
    suspend fun getTotalAllTimeOnce(): Long

    @Query("""
        SELECT localDate, SUM(amountMl) as total 
        FROM water_logs 
        WHERE localDate BETWEEN :startDate AND :endDate 
        GROUP BY localDate 
        ORDER BY localDate
    """)
    suspend fun getDailyTotals(startDate: String, endDate: String): List<DailyTotal>

    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int = 100): List<WaterLogEntity>

    @Query("DELETE FROM water_logs")
    suspend fun deleteAll()
}

data class DailyTotal(
    val localDate: String,
    val total: Int
)
