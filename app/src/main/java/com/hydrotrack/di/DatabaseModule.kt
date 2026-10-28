package com.hydrotrack.di

import android.content.Context
import androidx.room.Room
import com.hydrotrack.data.local.HydroDatabase
import com.hydrotrack.data.local.dao.DailyRecordDao
import com.hydrotrack.data.local.dao.WaterLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HydroDatabase {
        return Room.databaseBuilder(
            context,
            HydroDatabase::class.java,
            "hydrotrack.db"
        ).build()
    }

    @Provides
    fun provideWaterLogDao(database: HydroDatabase): WaterLogDao {
        return database.waterLogDao()
    }

    @Provides
    fun provideDailyRecordDao(database: HydroDatabase): DailyRecordDao {
        return database.dailyRecordDao()
    }
}
