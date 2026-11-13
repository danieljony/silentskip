package com.vertex.silentskip.di.data

import android.content.Context
import com.vertex.silentskip.data.datasource.AppSettingsDataSource
import com.vertex.silentskip.data.datasource.AudioStorageDataSource
import com.vertex.silentskip.data.repository.AppSettingsRepositoryImpl
import com.vertex.silentskip.data.repository.AudioRecordingRepositoryImpl
import com.vertex.silentskip.domain.repository.AppSettingsRepository
import com.vertex.silentskip.domain.repository.AudioRecordingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAppSettingsRepository(
        impl: AppSettingsRepositoryImpl
    ): AppSettingsRepository

    @Binds
    @Singleton
    abstract fun bindAudioRecordingRepository(
        impl: AudioRecordingRepositoryImpl
    ): AudioRecordingRepository

    companion object {
        @Provides
        @Singleton
        fun provideAppSettingsDataSource(
            @ApplicationContext context: Context
        ): AppSettingsDataSource {
            return AppSettingsDataSource(context)
        }

        @Provides
        @Singleton
        fun provideAudioStorageDataSource(
            @ApplicationContext context: Context
        ): AudioStorageDataSource {
            return AudioStorageDataSource(context)
        }
    }
}
