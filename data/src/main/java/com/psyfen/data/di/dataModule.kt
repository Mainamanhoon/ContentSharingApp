package com.psyfen.data.di

import com.psyfen.data.repository.ContentTileRepositoryImpl
import com.psyfen.domain.respository.ContentTileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object dataModule {

    @Provides
    @Singleton
    fun provideContentTileRepository(impl: ContentTileRepositoryImpl): ContentTileRepository {
        return impl
    }
}