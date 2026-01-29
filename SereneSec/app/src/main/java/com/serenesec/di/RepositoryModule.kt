package com.serenesec.di

import com.serenesec.data.repository.ArticleRepositoryImpl
import com.serenesec.data.repository.SourceRepositoryImpl
import com.serenesec.domain.repository.ArticleRepository
import com.serenesec.domain.repository.SourceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindSourceRepository(
        impl: SourceRepositoryImpl
    ): SourceRepository
    
    @Binds
    @Singleton
    abstract fun bindArticleRepository(
        impl: ArticleRepositoryImpl
    ): ArticleRepository
}
