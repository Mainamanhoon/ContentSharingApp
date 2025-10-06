package com.psyfen.domain.di

import com.psyfen.domain.respository.ContentTileRepository
import com.psyfen.domain.use_cases.AddContentTileUseCase
import com.psyfen.domain.use_cases.GetContentTilesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object DomainModule {

//    @Provides
//    fun providesSaveVideoProgressUseCase(repository: youtubePlayerRepository): SaveVideoProgressUseCase {
//        return SaveVideoProgressUseCase(repository)
//    }
//
//    @Provides
//    fun providesGetVideoProgressUseCase(repository: youtubePlayerRepository): GetVideoProgressUseCase {
//        return GetVideoProgressUseCase(repository)
//    }
//
//    @Provides
//    fun providesClearVideoProgressUseCase(repository: youtubePlayerRepository): ClearVideoProgressUseCase {
//        return ClearVideoProgressUseCase(repository)
//    }
//
//    @Provides
//    fun providesStreamVideoUseCase(): StreamVideoUseCase {
//        return StreamVideoUseCase()
//    }

    // Part 1: Content Tiles Use Cases
    @Provides
    fun providesGetContentTilesUseCase(repository: ContentTileRepository): GetContentTilesUseCase {
        return GetContentTilesUseCase(repository)
    }

    @Provides
    fun providesAddContentTileUseCase(repository: ContentTileRepository): AddContentTileUseCase {
        return AddContentTileUseCase(repository)
    }
}