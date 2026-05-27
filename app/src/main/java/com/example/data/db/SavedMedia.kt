package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.MediaItem

@Entity(tableName = "saved_media")
data class SavedMedia(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String,
    val backdropPath: String,
    val mediaType: String, // "movie", "tv", "anime"
    val voteAverage: Double,
    val releaseDate: String,
    val isFavorite: Boolean = false,
    val savedAt: Long = System.currentTimeMillis()
) {
    fun toMediaItem(): MediaItem {
        return MediaItem(
            id = id,
            title = if (mediaType == "movie") title else null,
            name = if (mediaType != "movie") title else null,
            overview = overview,
            posterPath = posterPath.takeIf { it.isNotEmpty() },
            backdropPath = backdropPath.takeIf { it.isNotEmpty() },
            mediaType = mediaType,
            voteAverage = voteAverage,
            releaseDate = if (mediaType == "movie") releaseDate else null,
            firstAirDate = if (mediaType != "movie") releaseDate else null
        )
    }

    companion object {
        fun fromMediaItem(item: MediaItem, customType: String? = null, isFavorite: Boolean = false): SavedMedia {
            val resolvedType = customType ?: item.mediaType ?: "movie"
            return SavedMedia(
                id = item.id,
                title = item.displayName,
                overview = item.overview ?: "",
                posterPath = item.posterPath ?: "",
                backdropPath = item.backdropPath ?: "",
                mediaType = resolvedType,
                voteAverage = item.voteAverage ?: 0.0,
                releaseDate = item.displayDate,
                isFavorite = isFavorite,
                savedAt = System.currentTimeMillis()
            )
        }
    }
}
