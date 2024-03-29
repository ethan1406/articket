package com.trufflear.trufflear.modules

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.net.ConnectivityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    fun provideAssetManager(@ApplicationContext context: Context): AssetManager =
        context.assets

    @Provides
    fun providesResources(@ApplicationContext context: Context): Resources = context.resources

    @Provides
    fun providesConnectivityManager(@ApplicationContext context: Context): ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
}