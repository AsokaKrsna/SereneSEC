package com.serenesec.di

import android.content.Context
import androidx.room.Room
import com.serenesec.data.local.SereneSecDatabase
import com.serenesec.data.local.dao.ArticleDao
import com.serenesec.data.local.dao.BookmarkDao
import com.serenesec.data.local.dao.CollectionDao
import com.serenesec.data.local.dao.SourceDao
import com.serenesec.data.local.dao.TagDao
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): SereneSecDatabase {
        return Room.databaseBuilder(
            context,
            SereneSecDatabase::class.java,
            SereneSecDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()  // For development - reset DB on schema change
            .build()
    }
    
    @Provides
    @Singleton
    fun provideSourceDao(database: SereneSecDatabase): SourceDao {
        return database.sourceDao()
    }
    
    @Provides
    @Singleton
    fun provideArticleDao(database: SereneSecDatabase): ArticleDao {
        return database.articleDao()
    }
    
    @Provides
    @Singleton
    fun provideBookmarkDao(database: SereneSecDatabase): BookmarkDao {
        return database.bookmarkDao()
    }
    
    @Provides
    @Singleton
    fun provideTagDao(database: SereneSecDatabase): TagDao {
        return database.tagDao()
    }
    
    @Provides
    @Singleton
    fun provideCollectionDao(database: SereneSecDatabase): CollectionDao {
        return database.collectionDao()
    }
}
