package com.example.data.api

import com.example.data.model.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(): TmdbResponse<MediaItem>

    @GET("trending/tv/week")
    suspend fun getTrendingTv(): TmdbResponse<MediaItem>

    @GET("movie/popular")
    suspend fun getPopularMovies(): TmdbResponse<MediaItem>

    @GET("tv/popular")
    suspend fun getPopularTv(): TmdbResponse<MediaItem>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(): TmdbResponse<MediaItem>

    @GET("tv/top_rated")
    suspend fun getTopRatedTv(): TmdbResponse<MediaItem>

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbResponse<MediaItem>

    @GET("discover/tv")
    suspend fun discoverAnimeTv(
        @Query("with_genres") withGenres: String = "16",
        @Query("with_original_language") withOriginalLanguage: String = "ja",
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): TmdbResponse<MediaItem>

    @GET("discover/movie")
    suspend fun discoverAnimeMovies(
        @Query("with_genres") withGenres: String = "16",
        @Query("with_original_language") withOriginalLanguage: String = "ja",
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): TmdbResponse<MediaItem>

    @GET("movie/{id}")
    suspend fun getMovieDetails(@Path("id") id: Int): MediaDetail

    @GET("tv/{id}")
    suspend fun getTvDetails(@Path("id") id: Int): MediaDetail

    @GET("movie/{id}/credits")
    suspend fun getMovieCredits(@Path("id") id: Int): CreditsResponse

    @GET("tv/{id}/credits")
    suspend fun getTvCredits(@Path("id") id: Int): CreditsResponse

    @GET("movie/{id}/videos")
    suspend fun getMovieVideos(@Path("id") id: Int): VideosResponse

    @GET("tv/{id}/videos")
    suspend fun getTvVideos(@Path("id") id: Int): VideosResponse

    @GET("https://docker-23e8-7860.prg1.zerops.app/api/media/1/{type}")
    suspend fun getPlaybackStreams(
        @Path("type") type: String,
        @Query("id") id: Int,
        @Query("s") season: Int? = null,
        @Query("e") episode: Int? = null
    ): List<PlaybackStream>
}
