package com.psyfen.domain.di

import com.psyfen.domain.respository.AuthRepository
import com.psyfen.domain.respository.ContentTileRepository
import com.psyfen.domain.respository.FileRepository
import com.psyfen.domain.use_cases.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object DomainModule {

    // Content Tiles Use Cases
    @Provides
    fun providesGetContentTilesUseCase(repository: ContentTileRepository): GetContentTilesUseCase {
        return GetContentTilesUseCase(repository)
    }

    @Provides
    fun providesAddContentTileUseCase(repository: ContentTileRepository): AddContentTileUseCase {
        return AddContentTileUseCase(repository)
    }

    // Auth Use Cases
    @Provides
    fun providesSendVerificationCodeUseCase(repository: AuthRepository): SendVerificationCodeUseCase {
        return SendVerificationCodeUseCase(repository)
    }

    @Provides
    fun providesVerifyCodeUseCase(repository: AuthRepository): VerifyCodeUseCase {
        return VerifyCodeUseCase(repository)
    }

    @Provides
    fun providesSignOutUseCase(repository: AuthRepository): SignOutUseCase {
        return SignOutUseCase(repository)
    }

    @Provides
    fun providesGetCurrentUserUseCase(repository: AuthRepository): GetCurrentUserUseCase {
        return GetCurrentUserUseCase(repository)
    }

    @Provides
    fun providesIsUserLoggedInUseCase(repository: AuthRepository): IsUserLoggedInUseCase {
        return IsUserLoggedInUseCase(repository)
    }

//    // File Use Cases
//    @Provides
//    fun providesUploadFileUseCase(repository: FileRepository): UploadFileUseCase {
//        return UploadFileUseCase(repository)
//    }
//
//    @Provides
//    fun providesGetPublicFilesUseCase(repository: FileRepository): GetPublicFilesUseCase {
//        return GetPublicFilesUseCase(repository)
//    }
//
//    @Provides
//    fun providesGetUserFilesUseCase(repository: FileRepository): GetUserFilesUseCase {
//        return GetUserFilesUseCase(repository)
//    }
//
//    @Provides
//    fun providesDeleteFileUseCase(repository: FileRepository): DeleteFileUseCase {
//        return DeleteFileUseCase(repository)
//    }
//
//    @Provides
//    fun providesShareFileUseCase(repository: FileRepository): ShareFileUseCase {
//        return ShareFileUseCase(repository)
//    }
}