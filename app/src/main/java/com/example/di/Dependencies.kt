package com.example.di

import android.content.Context
import com.example.data.api.RetrofitClient
import com.example.data.db.AppDatabase
import com.example.data.repository.MediaRepository

object Dependencies {
    @Volatile
    private var repository: MediaRepository? = null

    fun getRepository(context: Context): MediaRepository {
        return repository ?: synchronized(this) {
            val database = AppDatabase.getDatabase(context)
            val repo = MediaRepository(
                apiService = RetrofitClient.apiService,
                dao = database.savedMediaDao()
            )
            repository = repo
            repo
        }
    }
}
