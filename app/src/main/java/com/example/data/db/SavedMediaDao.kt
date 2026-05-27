package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedMediaDao {

    @Query("SELECT * FROM saved_media ORDER BY savedAt DESC")
    fun getAllSaved(): Flow<List<SavedMedia>>

    @Query("SELECT * FROM saved_media WHERE isFavorite = 1 ORDER BY savedAt DESC")
    fun getFavorites(): Flow<List<SavedMedia>>

    @Query("SELECT * FROM saved_media WHERE isFavorite = 0 ORDER BY savedAt DESC")
    fun getWatchlist(): Flow<List<SavedMedia>>

    @Query("SELECT * FROM saved_media WHERE id = :id LIMIT 1")
    suspend fun getSavedMediaById(id: Int): SavedMedia?

    @Query("SELECT * FROM saved_media WHERE id = :id LIMIT 1")
    fun getSavedMediaByIdFlow(id: Int): Flow<SavedMedia?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: SavedMedia)

    @Update
    suspend fun updateMedia(media: SavedMedia)

    @Query("DELETE FROM saved_media WHERE id = :id")
    suspend fun deleteSavedMediaById(id: Int)

    @Query("DELETE FROM saved_media")
    suspend fun clearAll()
}
