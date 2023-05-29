package com.example.objectboxpractice.di

import android.content.Context
import com.example.objectboxpractice.entity.MyObjectBox
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.objectbox.BoxStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun initObjectBox(@ApplicationContext context : Context) : BoxStore {
        return MyObjectBox.builder()
            .androidContext(context.applicationContext)
            .build()

    }
}