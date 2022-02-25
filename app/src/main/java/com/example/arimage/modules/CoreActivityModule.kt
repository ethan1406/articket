package com.example.arimage.modules

import android.content.Context
import android.view.LayoutInflater
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(ActivityComponent::class)
class CoreActivityModule {

    @Provides
    fun provideLayoutInflater(@ActivityContext context: Context): LayoutInflater =
        LayoutInflater.from(context)
}