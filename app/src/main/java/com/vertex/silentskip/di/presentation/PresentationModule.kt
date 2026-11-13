package com.vertex.silentskip.di.presentation

import android.content.Context
import com.vertex.silentskip.common.DefaultDispatcherProvider
import com.vertex.silentskip.common.DispatcherProvider
import com.vertex.silentskip.data.billing.BillingManager
import com.vertex.silentskip.data.datasource.AppSettingsDataSource
import com.vertex.silentskip.data.datasource.AudioStorageDataSource
import com.vertex.silentskip.domain.interactor.ToggleSubscriptionInteractor
import com.vertex.silentskip.presentation.action.AdManagerAction
import com.vertex.silentskip.presentation.action.AudioPlaybackAction
import com.vertex.silentskip.presentation.action.AudioRecordAction
import com.vertex.silentskip.presentation.action.ShareAudioAction
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PresentationModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider {
        return DefaultDispatcherProvider()
    }

    @Provides
    @Singleton
    fun provideAdManagerAction(
        appSettingsDataSource: AppSettingsDataSource
    ): AdManagerAction {
        return AdManagerAction(appSettingsDataSource)
    }

    @Provides
    @Singleton
    fun provideAudioRecordAction(
        @ApplicationContext context: Context,
        audioStorageDataSource: AudioStorageDataSource,
        appSettingsDataSource: AppSettingsDataSource,
        dispatcherProvider: DispatcherProvider
    ): AudioRecordAction {
        return AudioRecordAction(context, audioStorageDataSource, appSettingsDataSource, dispatcherProvider)
    }

    @Provides
    @Singleton
    fun provideAudioPlaybackAction(
        dispatcherProvider: DispatcherProvider
    ): AudioPlaybackAction {
        return AudioPlaybackAction(dispatcherProvider)
    }

    @Provides
    @Singleton
    fun provideShareAudioAction(): ShareAudioAction {
        return ShareAudioAction()
    }

    @Provides
    @Singleton
    fun provideBillingManager(
        @ApplicationContext context: Context,
        toggleSubscriptionInteractor: ToggleSubscriptionInteractor,
        dispatcherProvider: DispatcherProvider
    ): BillingManager {
        return BillingManager(context) { isSubscribed ->
            CoroutineScope(dispatcherProvider.io).launch {
                toggleSubscriptionInteractor.execute(isSubscribed)
            }
        }
    }
}
