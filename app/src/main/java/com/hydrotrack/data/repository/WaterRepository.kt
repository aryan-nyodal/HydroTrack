package com.hydrotrack.data.repository

import com.hydrotrack.data.local.dao.DailyRecordDao
import com.hydrotrack.data.local.dao.DailyTotal
import com.hydrotrack.data.local.dao.WaterLogDao
import com.hydrotrack.data.local.entity.DailyRecordEntity
import com.hydrotrack.data.local.entity.WaterLogEntity
import com.hydrotrack.util.DateUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaterRepository @Inject constructor(
    private val waterLogDao: WaterLogDao,
    private val dailyRecordDao: DailyRecordDao
) {
    // Water log operations
    suspend fun logWater(amountMl: Int, date: String = DateUtils.today()): Long {
        val entity = WaterLogEntity(
            amountMl = amountMl,
            timestamp = System.currentTimeMillis(),
            localDate = date
        )
        return waterLogDao.insert(entity)
    }

    suspend fun deleteWaterLog(log: WaterLogEntity) {
        waterLogDao.delete(log)
    }

    fun getTodayLogs(): Flow<List<WaterLogEntity>> {
        return waterLogDao.getLogsForDate(DateUtils.today())
    }

    suspend fun getTodayLogsOnce(): List<WaterLogEntity> {
        return waterLogDao.getLogsForDateOnce(DateUtils.today())
    }

    fun getTodayTotal(): Flow<Int> {
        return waterLogDao.getTotalForDate(DateUtils.today())
    }

    suspend fun getTodayTotalOnce(): Int {
        return waterLogDao.getTotalForDateOnce(DateUtils.today())
    }

    suspend fun getTotalForDate(date: String): Int {
        return waterLogDao.getTotalForDateOnce(date)
    }

    fun getTotalAllTime(): Flow<Long> {
        return waterLogDao.getTotalAllTime()
    }

    suspend fun getDailyTotals(startDate: String, endDate: String): List<DailyTotal> {
        return waterLogDao.getDailyTotals(startDate, endDate)
    }

    // Daily record operations
    suspend fun getOrCreateDailyRecord(date: String, goalMl: Int): DailyRecordEntity {
        val existing = dailyRecordDao.getRecordForDate(date)
        if (existing != null) return existing

        val newRecord = DailyRecordEntity(
            date = date,
            goalMl = goalMl,
            totalMl = 0,
            goalMet = false
        )
        dailyRecordDao.insertOrUpdate(newRecord)
        return newRecord
    }

    suspend fun updateDailyRecord(record: DailyRecordEntity) {
        dailyRecordDao.insertOrUpdate(record)
    }

    suspend fun getDailyRecord(date: String): DailyRecordEntity? {
        return dailyRecordDao.getRecordForDate(date)
    }

    fun observeDailyRecord(date: String): Flow<DailyRecordEntity?> {
        return dailyRecordDao.observeRecordForDate(date)
    }

    fun getAllDailyRecords(): Flow<List<DailyRecordEntity>> {
        return dailyRecordDao.getAllRecords()
    }

    suspend fun getAllDailyRecordsOnce(): List<DailyRecordEntity> {
        return dailyRecordDao.getAllRecordsOnce()
    }

    suspend fun getRecordsInRange(startDate: String, endDate: String): List<DailyRecordEntity> {
        return dailyRecordDao.getRecordsInRange(startDate, endDate)
    }

    fun getSuccessfulDayCount(): Flow<Int> {
        return dailyRecordDao.getSuccessfulDayCount()
    }

    suspend fun getSuccessfulDayCountOnce(): Int {
        return dailyRecordDao.getSuccessfulDayCountOnce()
    }

    // Reset
    suspend fun deleteAllData() {
        waterLogDao.deleteAll()
        dailyRecordDao.deleteAll()
    }
}
