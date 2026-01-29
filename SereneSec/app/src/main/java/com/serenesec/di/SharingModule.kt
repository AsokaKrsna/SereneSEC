package com.serenesec.di

import android.content.Context
import com.serenesec.data.sharing.ShareManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SharingModule {
    
    @Provides
    @Singleton
    fun provideShareManager(
        @ApplicationContext context: Context
    ): ShareManager {
        return ShareManager(context)
    }
}
