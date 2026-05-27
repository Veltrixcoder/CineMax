package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TmdbResponse<T>(
    @Json(name = "results") val results: List<T> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MediaItem(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "media_type") val mediaType: String? = null,
    @Json(name = "vote_average") val voteAverage: Double? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "genre_ids") val genreIds: List<Int>? = null
) {
    val displayName: String
        get() = title ?: name ?: "Untitled"

    val displayDate: String
        get() = releaseDate ?: firstAirDate ?: "N/A"

    val posterUrl: String
        get() = if (!posterPath.isNullOrEmpty()) "https://image.tmdb.org/t/p/w500$posterPath" else ""

    val backdropUrl: String
        get() = if (!backdropPath.isNullOrEmpty()) "https://image.tmdb.org/t/p/w780$backdropPath" else ""

    val ratingText: String
        get() = String.format("%.1f", voteAverage ?: 0.0)
}

@JsonClass(generateAdapter = true)
data class MediaDetail(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "vote_average") val voteAverage: Double? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "genres") val genres: List<Genre>? = null,
    @Json(name = "tagline") val tagline: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "runtime") val runtime: Int? = null, // for movies
    @Json(name = "number_of_seasons") val numberOfSeasons: Int? = null, // for TV shows
    @Json(name = "number_of_episodes") val numberOfEpisodes: Int? = null // for TV shows
) {
    val displayName: String
        get() = title ?: name ?: "Untitled"

    val displayDate: String
        get() = releaseDate ?: firstAirDate ?: "N/A"

    val posterUrl: String
        get() = if (!posterPath.isNullOrEmpty()) "https://image.tmdb.org/t/p/w500$posterPath" else ""

    val backdropUrl: String
        get() = if (!backdropPath.isNullOrEmpty()) "https://image.tmdb.org/t/p/w780$backdropPath" else ""

    val ratingText: String
        get() = String.format("%.1f", voteAverage ?: 0.0)

    val subtitleInfo: String
        get() {
            val list = mutableListOf<String>()
            if (!displayDate.isEmpty() && displayDate != "N/A") {
                list.add(displayDate.take(4))
            }
            if (runtime != null && runtime > 0) {
                list.add("${runtime}m")
            } else if (numberOfSeasons != null) {
                list.add("$numberOfSeasons Seasons")
            }
            if (!status.isNullOrEmpty()) {
                list.add(status)
            }
            return list.joinToString(" • ")
        }
}

@JsonClass(generateAdapter = true)
data class Genre(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class CreditsResponse(
    @Json(name = "cast") val cast: List<CastMember> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CastMember(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "character") val character: String?,
    @Json(name = "profile_path") val profilePath: String?
) {
    val profileUrl: String
        get() = if (!profilePath.isNullOrEmpty()) "https://image.tmdb.org/t/p/w185$profilePath" else ""
}

@JsonClass(generateAdapter = true)
data class VideosResponse(
    @Json(name = "results") val results: List<VideoItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VideoItem(
    @Json(name = "id") val id: String,
    @Json(name = "key") val key: String,
    @Json(name = "name") val name: String,
    @Json(name = "site") val site: String,
    @Json(name = "type") val type: String
) {
    val isYouTubeTrailer: Boolean
        get() = site.equals("YouTube", ignoreCase = true) && type.equals("Trailer", ignoreCase = true)
}

@JsonClass(generateAdapter = true)
data class PlaybackStream(
    @Json(name = "url") val url: String,
    @Json(name = "metadata") val metadata: String? = null,
    @Json(name = "referer") val referer: String? = null,
    @Json(name = "origin") val origin: String? = null,
    @Json(name = "quality") val quality: Int? = null,
    @Json(name = "type") val type: String? = null
)

