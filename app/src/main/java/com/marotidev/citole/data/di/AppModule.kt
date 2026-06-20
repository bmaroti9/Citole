/*
Copyright (C) <2026>  <Balint Maroti>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

*/

package com.marotidev.citole.data.di

import android.app.Application
import androidx.room.Room
import com.marotidev.citole.data.repository.AudioRepository
import com.marotidev.citole.data.repository.DataStoreRepository
import com.marotidev.citole.data.service.AppDatabase
import com.marotidev.citole.data.service.AudioService
import com.marotidev.citole.data.service.TrackPlayLogDao
import com.marotidev.citole.data.state.SearchQueryStateHolder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideAudioService() : AudioService {
        return AudioService()
    }

    @Provides
    @Singleton
    fun provideAudioRepository(audioService: AudioService, app: Application) : AudioRepository {
        return AudioRepository(audioService, app)
    }

    @Provides
    @Singleton
    fun provideDataStoreRepository(app: Application) : DataStoreRepository {
        return DataStoreRepository(app)
    }

    @Provides
    @Singleton
    fun provideSearchQueryStateHolder() : SearchQueryStateHolder {
        return SearchQueryStateHolder()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "app-database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideTrackPlayLogDao(database: AppDatabase): TrackPlayLogDao {
        return database.trackPlayLogDao()
    }
}