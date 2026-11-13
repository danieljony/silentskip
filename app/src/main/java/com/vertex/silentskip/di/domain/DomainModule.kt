package com.vertex.silentskip.di.domain

import com.vertex.silentskip.domain.interactor.DeleteAudioRecordingInteractor
import com.vertex.silentskip.domain.interactor.DeleteAudioRecordingInteractorImpl
import com.vertex.silentskip.domain.interactor.GetAppSettingsInteractor
import com.vertex.silentskip.domain.interactor.GetAppSettingsInteractorImpl
import com.vertex.silentskip.domain.interactor.GetAudioRecordingsInteractor
import com.vertex.silentskip.domain.interactor.GetAudioRecordingsInteractorImpl
import com.vertex.silentskip.domain.interactor.RenameAudioRecordingInteractor
import com.vertex.silentskip.domain.interactor.RenameAudioRecordingInteractorImpl
import com.vertex.silentskip.domain.interactor.SaveAppSettingsInteractor
import com.vertex.silentskip.domain.interactor.SaveAppSettingsInteractorImpl
import com.vertex.silentskip.domain.interactor.ToggleSubscriptionInteractor
import com.vertex.silentskip.domain.interactor.ToggleSubscriptionInteractorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {

    @Binds
    @Singleton
    abstract fun bindGetAppSettingsInteractor(
        impl: GetAppSettingsInteractorImpl
    ): GetAppSettingsInteractor

    @Binds
    @Singleton
    abstract fun bindSaveAppSettingsInteractor(
        impl: SaveAppSettingsInteractorImpl
    ): SaveAppSettingsInteractor

    @Binds
    @Singleton
    abstract fun bindToggleSubscriptionInteractor(
        impl: ToggleSubscriptionInteractorImpl
    ): ToggleSubscriptionInteractor

    @Binds
    @Singleton
    abstract fun bindGetAudioRecordingsInteractor(
        impl: GetAudioRecordingsInteractorImpl
    ): GetAudioRecordingsInteractor

    @Binds
    @Singleton
    abstract fun bindRenameAudioRecordingInteractor(
        impl: RenameAudioRecordingInteractorImpl
    ): RenameAudioRecordingInteractor

    @Binds
    @Singleton
    abstract fun bindDeleteAudioRecordingInteractor(
        impl: DeleteAudioRecordingInteractorImpl
    ): DeleteAudioRecordingInteractor
}
