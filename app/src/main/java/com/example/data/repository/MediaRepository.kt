package com.example.data.repository

import com.example.data.api.TmdbApiService
import com.example.data.db.SavedMedia
import com.example.data.db.SavedMediaDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class MediaRepository(
    private val apiService: TmdbApiService,
    private val dao: SavedMediaDao
) {
    // --- Local DB (Library/Watchlist/Favorites) ---
    val allSaved: Flow<List<SavedMedia>> = dao.getAllSaved()
    val favorites: Flow<List<SavedMedia>> = dao.getFavorites()
    val watchlist: Flow<List<SavedMedia>> = dao.getWatchlist()

    fun getSavedFlow(id: Int): Flow<SavedMedia?> {
        return dao.getSavedMediaByIdFlow(id)
    }

    suspend fun isSaved(id: Int): Boolean {
        return dao.getSavedMediaById(id) != null
    }

    suspend fun saveMedia(item: MediaItem, customType: String? = null, isFavorite: Boolean = false) {
        val saved = SavedMedia.fromMediaItem(item, customType, isFavorite)
        dao.insertMedia(saved)
    }

    suspend fun toggleFavorite(item: MediaItem, customType: String? = null) {
        val existing = dao.getSavedMediaById(item.id)
        if (existing != null) {
            val updated = existing.copy(isFavorite = !existing.isFavorite, savedAt = System.currentTimeMillis())
            // If both isFavorite and watchlist are false (wait, we only have isFavorite, if isFavorite is false, it stays in watchlist!)
            dao.updateMedia(updated)
        } else {
            // Save as favorite!
            val saved = SavedMedia.fromMediaItem(item, customType = customType, isFavorite = true)
            dao.insertMedia(saved)
        }
    }

    suspend fun toggleWatchlist(item: MediaItem, customType: String? = null) {
        val existing = dao.getSavedMediaById(item.id)
        if (existing != null) {
            // If it is already in local database, toggle or delete
            // Let's make it standard: if we click watchlist button:
            // if it exists, delete it completely unless it's a favorite (then we just keep it but since we don't have separate watchlist boolean, we can just delete from database or toggle favorite)
            // Let's do a smart thing: if it exists, delete it. If it doesn't, add to database (with isFavorite = false)
            dao.deleteSavedMediaById(item.id)
        } else {
            val saved = SavedMedia.fromMediaItem(item, customType = customType, isFavorite = false)
            dao.insertMedia(saved)
        }
    }

    suspend fun deleteSaved(id: Int) {
        dao.deleteSavedMediaById(id)
    }

    // --- Remote API ---
    suspend fun getTrendingMovies(): List<MediaItem> {
        return apiService.getTrendingMovies().results
    }

    suspend fun getTrendingTv(): List<MediaItem> {
        return apiService.getTrendingTv().results
    }

    suspend fun getPopularMovies(): List<MediaItem> {
        return apiService.getPopularMovies().results
    }

    suspend fun getPopularTv(): List<MediaItem> {
        return apiService.getPopularTv().results
    }

    suspend fun getTopRatedMovies(): List<MediaItem> {
        return apiService.getTopRatedMovies().results
    }

    suspend fun getTopRatedTv(): List<MediaItem> {
        return apiService.getTopRatedTv().results
    }

    suspend fun search(query: String): List<MediaItem> {
        if (query.isBlank()) return emptyList()
        return apiService.searchMulti(query).results
    }

    suspend fun getAnimeTv(): List<MediaItem> {
        return apiService.discoverAnimeTv().results
    }

    suspend fun getAnimeMovies(): List<MediaItem> {
        return apiService.discoverAnimeMovies().results
    }

    suspend fun getMovieDetails(id: Int): MediaDetail {
        return apiService.getMovieDetails(id)
    }

    suspend fun getTvDetails(id: Int): MediaDetail {
        return apiService.getTvDetails(id)
    }

    suspend fun getMovieCredits(id: Int): List<CastMember> {
        return apiService.getMovieCredits(id).cast
    }

    suspend fun getTvCredits(id: Int): List<CastMember> {
        return apiService.getTvCredits(id).cast
    }

    suspend fun getMovieVideos(id: Int): List<VideoItem> {
        return apiService.getMovieVideos(id).results
    }

    suspend fun getTvVideos(id: Int): List<VideoItem> {
        return apiService.getTvVideos(id).results
    }

    suspend fun getPlaybackStreams(type: String, id: Int, season: Int? = null, episode: Int? = null): List<PlaybackStream> {
        val mappedType = if (type.equals("movie", ignoreCase = true)) "movie" else "tv"
        return apiService.getPlaybackStreams(mappedType, id, season, episode)
    }
}
