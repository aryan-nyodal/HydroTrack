package com.hydrotrack.di

import android.content.Context
import com.hydrotrack.data.datastore.SettingsDataStore
import com.hydrotrack.data.repository.SettingsRepository
import com.hydrotrack.data.repository.WaterRepository
import com.hydrotrack.domain.PointsCalculator
import com.hydrotrack.domain.StreakCalculator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideStreakCalculator(): StreakCalculator {
        return StreakCalculator()
    }

    @Provides
    @Singleton
    fun providePointsCalculator(): PointsCalculator {
        return PointsCalculator()
    }
}
