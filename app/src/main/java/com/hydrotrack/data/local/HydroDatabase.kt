package com.hydrotrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hydrotrack.data.local.dao.DailyRecordDao
import com.hydrotrack.data.local.dao.WaterLogDao
import com.hydrotrack.data.local.entity.DailyRecordEntity
import com.hydrotrack.data.local.entity.WaterLogEntity

@Database(
    entities = [WaterLogEntity::class, DailyRecordEntity::class],
    version = 1,
    exportSchema = true
)
abstract class HydroDatabase : RoomDatabase() {
    abstract fun waterLogDao(): WaterLogDao
    abstract fun dailyRecordDao(): DailyRecordDao
}
