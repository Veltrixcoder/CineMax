package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.db.SavedMedia
import com.example.data.model.*
import com.example.data.repository.MediaRepository
import com.example.di.Dependencies
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

// Media3 player imports
import androidx.media3.common.MediaItem as Media3MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.DefaultDataSource

class MainViewModel(
    application: Application,
    private val repository: MediaRepository
) : AndroidViewModel(application) {

    // --- Theme State ---
    val isDarkMode = MutableStateFlow(true)

    fun toggleTheme() {
        isDarkMode.value = !isDarkMode.value
    }

    // --- Global Persistent Player States ---
    val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(getApplication()).build().apply {
            playWhenReady = true
        }
    }

    private val _currentPlayingMediaId = MutableStateFlow<Int?>(null)
    val currentPlayingMediaId: StateFlow<Int?> = _currentPlayingMediaId.asStateFlow()

    private val _currentPlayingTitle = MutableStateFlow<String>("")
    val currentPlayingTitle: StateFlow<String> = _currentPlayingTitle.asStateFlow()

    private val _currentPlayingSub = MutableStateFlow<String>("")
    val currentPlayingSub: StateFlow<String> = _currentPlayingSub.asStateFlow()

    private val _currentPlayingPoster = MutableStateFlow<String>("")
    val currentPlayingPoster: StateFlow<String> = _currentPlayingPoster.asStateFlow()

    private val _currentPlayingUrl = MutableStateFlow<String?>(null)
    val currentPlayingUrl: StateFlow<String?> = _currentPlayingUrl.asStateFlow()

    private val _currentPlayingReferer = MutableStateFlow<String?>(null)
    val currentPlayingReferer: StateFlow<String?> = _currentPlayingReferer.asStateFlow()

    private val _currentPlayingOrigin = MutableStateFlow<String?>(null)
    val currentPlayingOrigin: StateFlow<String?> = _currentPlayingOrigin.asStateFlow()

    private val _isPlayingState = MutableStateFlow(false)
    val isPlayingState: StateFlow<Boolean> = _isPlayingState.asStateFlow()

    private val _showMiniPlayer = MutableStateFlow(false)
    val showMiniPlayer: StateFlow<Boolean> = _showMiniPlayer.asStateFlow()

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun playMediaUrl(
        mediaId: Int,
        title: String,
        sub: String,
        posterUrl: String,
        url: String,
        referer: String? = null,
        origin: String? = null
    ) {
        _currentPlayingMediaId.value = mediaId
        _currentPlayingTitle.value = title
        _currentPlayingSub.value = sub
        _currentPlayingPoster.value = posterUrl
        _currentPlayingUrl.value = url
        _currentPlayingReferer.value = referer
        _currentPlayingOrigin.value = origin
        _isPlayingState.value = true
        _showMiniPlayer.value = true

        val context = getApplication<Application>()
        val headers = mutableMapOf<String, String>()
        referer?.let { headers["Referer"] = it }
        origin?.let { headers["Origin"] = it }

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
            .setDefaultRequestProperties(headers)

        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
        val mediaItem = Media3MediaItem.fromUri(url)
        val mediaSource = androidx.media3.exoplayer.source.ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun pauseMedia() {
        exoPlayer.pause()
        _isPlayingState.value = false
    }

    fun resumeMedia() {
        exoPlayer.play()
        _isPlayingState.value = true
    }

    fun togglePlaybackState() {
        if (exoPlayer.isPlaying) {
            pauseMedia()
        } else {
            resumeMedia()
        }
    }

    fun closeMiniPlayer() {
        _showMiniPlayer.value = false
        _isPlayingState.value = false
        _currentPlayingUrl.value = null
        exoPlayer.stop()
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }

    // --- Home Screen States ---
    private val _trendingMovies = MutableStateFlow<List<MediaItem>>(emptyList())
    val trendingMovies: StateFlow<List<MediaItem>> = _trendingMovies.asStateFlow()

    private val _trendingTv = MutableStateFlow<List<MediaItem>>(emptyList())
    val trendingTv: StateFlow<List<MediaItem>> = _trendingTv.asStateFlow()

    private val _popularMovies = MutableStateFlow<List<MediaItem>>(emptyList())
    val popularMovies: StateFlow<List<MediaItem>> = _popularMovies.asStateFlow()

    private val _topRatedMovies = MutableStateFlow<List<MediaItem>>(emptyList())
    val topRatedMovies: StateFlow<List<MediaItem>> = _topRatedMovies.asStateFlow()

    private val _isLoadingHome = MutableStateFlow(false)
    val isLoadingHome: StateFlow<Boolean> = _isLoadingHome.asStateFlow()

    private val _homeError = MutableStateFlow<String?>(null)
    val homeError: StateFlow<String?> = _homeError.asStateFlow()

    // --- Search Screen States ---
    val searchQuery = MutableStateFlow("")

    private val _searchResults = MutableStateFlow<List<MediaItem>>(emptyList())
    val searchResults: StateFlow<List<MediaItem>> = _searchResults.asStateFlow()

    private val _isLoadingSearch = MutableStateFlow(false)
    val isLoadingSearch: StateFlow<Boolean> = _isLoadingSearch.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    private var searchJob: Job? = null

    // --- Anime Screen States ---
    private val _animeShows = MutableStateFlow<List<MediaItem>>(emptyList())
    val animeShows: StateFlow<List<MediaItem>> = _animeShows.asStateFlow()

    private val _animeMovies = MutableStateFlow<List<MediaItem>>(emptyList())
    val animeMovies: StateFlow<List<MediaItem>> = _animeMovies.asStateFlow()

    private val _isLoadingAnime = MutableStateFlow(false)
    val isLoadingAnime: StateFlow<Boolean> = _isLoadingAnime.asStateFlow()

    private val _animeError = MutableStateFlow<String?>(null)
    val animeError: StateFlow<String?> = _animeError.asStateFlow()

    // --- Detailed Multi Screen States ---
    private val _activeDetail = MutableStateFlow<MediaDetail?>(null)
    val activeDetail: StateFlow<MediaDetail?> = _activeDetail.asStateFlow()

    private val _activeCast = MutableStateFlow<List<CastMember>>(emptyList())
    val activeCast: StateFlow<List<CastMember>> = _activeCast.asStateFlow()

    private val _activeVideos = MutableStateFlow<List<VideoItem>>(emptyList())
    val activeVideos: StateFlow<List<VideoItem>> = _activeVideos.asStateFlow()

    private val _isLoadingDetails = MutableStateFlow(false)
    val isLoadingDetails: StateFlow<Boolean> = _isLoadingDetails.asStateFlow()

    private val _detailsError = MutableStateFlow<String?>(null)
    val detailsError: StateFlow<String?> = _detailsError.asStateFlow()

    // Observe active item local DB state reactively
    private val _activeSavedMedia = MutableStateFlow<SavedMedia?>(null)
    val activeSavedMedia: StateFlow<SavedMedia?> = _activeSavedMedia.asStateFlow()

    // --- Playback States ---
    private val _playbackStreams = MutableStateFlow<List<PlaybackStream>>(emptyList())
    val playbackStreams: StateFlow<List<PlaybackStream>> = _playbackStreams.asStateFlow()

    private val _isLoadingPlayback = MutableStateFlow(false)
    val isLoadingPlayback: StateFlow<Boolean> = _isLoadingPlayback.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    fun loadPlaybackStreams(type: String, id: Int, season: Int? = null, episode: Int? = null) {
        viewModelScope.launch {
            _isLoadingPlayback.value = true
            _playbackError.value = null
            _playbackStreams.value = emptyList()
            try {
                val streams = repository.getPlaybackStreams(type, id, season, episode)
                _playbackStreams.value = streams
                if (streams.isEmpty()) {
                    _playbackError.value = "No streaming links returned from server."
                }
            } catch (e: Exception) {
                _playbackError.value = e.localizedMessage ?: "Failed to fetch playback sources"
            } finally {
                _isLoadingPlayback.value = false
            }
        }
    }

    // --- Library States ---
    val allSaved: StateFlow<List<SavedMedia>> = repository.allSaved
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<SavedMedia>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchlist: StateFlow<List<SavedMedia>> = repository.watchlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Load default app lists on launch
        loadHomeData()
        loadAnimeData()

        // Set up search flow observer for instant reactive typing with debounce!
        viewModelScope.launch {
            searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank()) {
                        executeSearch(query)
                    } else {
                        _searchResults.value = emptyList()
                    }
                }
        }
    }

    // --- Core Operations ---

    fun loadHomeData() {
        viewModelScope.launch {
            _isLoadingHome.value = true
            _homeError.value = null
            try {
                supervisorScope {
                    val trendingMoviesDeffered = async { repository.getTrendingMovies() }
                    val trendingTvDeffered = async { repository.getTrendingTv() }
                    val popularMoviesDeffered = async { repository.getPopularMovies() }
                    val topRatedMoviesDeffered = async { repository.getTopRatedMovies() }

                    _trendingMovies.value = try { trendingMoviesDeffered.await() } catch (e: Exception) { emptyList() }
                    _trendingTv.value = try { trendingTvDeffered.await() } catch (e: Exception) { emptyList() }
                    _popularMovies.value = try { popularMoviesDeffered.await() } catch (e: Exception) { emptyList() }
                    _topRatedMovies.value = try { topRatedMoviesDeffered.await() } catch (e: Exception) { emptyList() }
                }
            } catch (e: Exception) {
                _homeError.value = "Failed to load movies: ${e.localizedMessage ?: "Network error"}"
            } finally {
                _isLoadingHome.value = false
            }
        }
    }

    fun loadAnimeData() {
        viewModelScope.launch {
            _isLoadingAnime.value = true
            _animeError.value = null
            try {
                supervisorScope {
                    val tvDeffered = async { repository.getAnimeTv() }
                    val movieDeffered = async { repository.getAnimeMovies() }

                    _animeShows.value = try { tvDeffered.await() } catch (e: Exception) { emptyList() }
                    _animeMovies.value = try { movieDeffered.await() } catch (e: Exception) { emptyList() }
                }
            } catch (e: Exception) {
                _animeError.value = "Failed to fetch anime: ${e.localizedMessage ?: "Network error"}"
            } finally {
                _isLoadingAnime.value = false
            }
        }
    }

    private suspend fun executeSearch(query: String) {
        _isLoadingSearch.value = true
        _searchError.value = null
        try {
            _searchResults.value = repository.search(query)
        } catch (e: Exception) {
            _searchError.value = "Search failed: ${e.localizedMessage ?: "Network error"}"
        } finally {
            _isLoadingSearch.value = false
        }
    }

    fun retrySearch() {
        val query = searchQuery.value
        if (query.isNotBlank()) {
            viewModelScope.launch { executeSearch(query) }
        }
    }

    // Load detailed information for MediaDetail view
    private var detailObserverJob: Job? = null

    fun loadMediaDetails(mediaType: String, id: Int) {
        // Cancel previous observer
        detailObserverJob?.cancel()

        viewModelScope.launch {
            _isLoadingDetails.value = true
            _detailsError.value = null
            _activeDetail.value = null
            _activeCast.value = emptyList()
            _activeVideos.value = emptyList()

            try {
                val isMovie = mediaType.equals("movie", ignoreCase = true)
                
                supervisorScope {
                    // Load details, cast, and videos in parallel
                    val detailDeferred = async {
                        if (isMovie) repository.getMovieDetails(id) else repository.getTvDetails(id)
                    }
                    val castDeferred = async {
                        if (isMovie) repository.getMovieCredits(id) else repository.getTvCredits(id)
                    }
                    val videosDeferred = async {
                        if (isMovie) repository.getMovieVideos(id) else repository.getTvVideos(id)
                    }

                    _activeDetail.value = detailDeferred.await()
                    _activeCast.value = try { castDeferred.await() } catch (e: Exception) { emptyList() }
                    _activeVideos.value = try { videosDeferred.await() } catch (e: Exception) { emptyList() }
                }
            } catch (e: Exception) {
                _detailsError.value = "Failed to load details: ${e.localizedMessage ?: "API Error"}"
            } finally {
                _isLoadingDetails.value = false
            }
        }

        // Reactively observe local DB status for this item
        detailObserverJob = viewModelScope.launch {
            repository.getSavedFlow(id).collect { saved ->
                _activeSavedMedia.value = saved
            }
        }
    }

    // Toggle Favorite Action (adds/updates item in local DB)
    fun toggleFavorite(item: MediaItem, customType: String? = null) {
        viewModelScope.launch {
            repository.toggleFavorite(item, customType)
        }
    }

    // Toggle Watchlist Action (adds/removes item in local DB)
    fun toggleWatchlist(item: MediaItem, customType: String? = null) {
        viewModelScope.launch {
            repository.toggleWatchlist(item, customType)
        }
    }

    // Helper for direct detail toggles
    fun toggleFavoriteFromDetail() {
        val detail = _activeDetail.value ?: return
        val saved = _activeSavedMedia.value
        
        // Assemble dummy / helper media item representing active details
        val mediaItem = MediaItem(
            id = detail.id,
            title = detail.title,
            name = detail.name,
            overview = detail.overview,
            posterPath = detail.posterPath,
            backdropPath = detail.backdropPath,
            mediaType = if (detail.title != null) "movie" else "tv",
            voteAverage = detail.voteAverage,
            releaseDate = detail.releaseDate,
            firstAirDate = detail.firstAirDate
        )

        viewModelScope.launch {
            if (saved != null) {
                // If saved, toggle favorite status
                val updated = saved.copy(isFavorite = !saved.isFavorite, savedAt = System.currentTimeMillis())
                repository.saveMedia(mediaItem, customType = saved.mediaType, isFavorite = updated.isFavorite)
            } else {
                // If not saved, saved as Favorite directly
                repository.saveMedia(mediaItem, customType = if (detail.title != null) "movie" else "tv", isFavorite = true)
            }
        }
    }

    fun toggleWatchlistFromDetail() {
        val detail = _activeDetail.value ?: return
        val saved = _activeSavedMedia.value

        val mediaItem = MediaItem(
            id = detail.id,
            title = detail.title,
            name = detail.name,
            overview = detail.overview,
            posterPath = detail.posterPath,
            backdropPath = detail.backdropPath,
            mediaType = if (detail.title != null) "movie" else "tv",
            voteAverage = detail.voteAverage,
            releaseDate = detail.releaseDate,
            firstAirDate = detail.firstAirDate
        )

        viewModelScope.launch {
            if (saved != null) {
                // Remove from database if already in watchlist and not a favorite,
                // or toggle. Let's make it easy: remove from database.
                repository.deleteSaved(detail.id)
            } else {
                // Save to Watchlist (isFavorite = false)
                repository.saveMedia(mediaItem, customType = if (detail.title != null) "movie" else "tv", isFavorite = false)
            }
        }
    }
}

// Factory to inject Application and Repository singletons
class MainViewModelFactory(
    private val application: Application,
    private val repository: MediaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
