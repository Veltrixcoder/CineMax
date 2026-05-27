package com.example.ui.screens

import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.data.model.PlaybackStream
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineRed
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.components.LiquidGlassBackground
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTitle by viewModel.currentPlayingTitle.collectAsState()
    val currentSub by viewModel.currentPlayingSub.collectAsState()
    val currentPoster by viewModel.currentPlayingPoster.collectAsState()
    val currentUrl by viewModel.currentPlayingUrl.collectAsState()
    val isPlaying by viewModel.isPlayingState.collectAsState()
    val streams by viewModel.playbackStreams.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    var userIsDraggingSlider by remember { mutableStateOf(false) }
    var videoDuration by remember { mutableStateOf(0L) }
    var currentPosition by remember { mutableStateOf(0L) }
    var videoSpeed by remember { mutableStateOf(1.0f) }
    var resizeMode by remember { mutableStateOf(androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var isExpandedDescription by remember { mutableStateOf(false) }

    // Recommendation source based on home lists
    val recommendations by viewModel.trendingMovies.collectAsState()

    // Activity reference to toggle orientation if needed
    val activity = context as? android.app.Activity

    // Back action minimizes to miniplayer
    BackHandler {
        onBackClick()
    }

    // Toggle controls visibility after screen idle
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(5000)
            showControls = false
        }
    }

    // Poll current position of ExoPlayer
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = viewModel.exoPlayer.currentPosition
            videoDuration = viewModel.exoPlayer.duration.coerceAtLeast(0L)
            delay(1000)
        }
    }

    // iOS 26 Liquid Frosted Glass Style Background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090B11))
    ) {
        LiquidGlassBackground {
            // Main Screen Interface
            Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. YouTube Action Header Bar with Liquid Glass effect
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.03f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Minimize Player",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentTitle.ifEmpty { "Playing Streaming Video" },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (currentSub.isNotEmpty()) {
                        Text(
                            text = currentSub,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }

                IconButton(
                    onClick = { showQualityDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Quality Settings",
                        tint = Color.White
                    )
                }
            }

            // 2. Main High-Fidelity Video Player Container (YouTube Style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showControls = !showControls
                    }
            ) {
                // ExoPlayer Surface
                AndroidView(
                    factory = { ctx ->
                        androidx.media3.ui.PlayerView(ctx).apply {
                            player = viewModel.exoPlayer
                            useController = false // Custom modern Compose controllers on top
                            setBackgroundColor(android.graphics.Color.BLACK)
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { playerView ->
                        playerView.resizeMode = resizeMode
                    }
                )

                // High-End Gestures and Controls Overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
                    exit = fadeOut(animationSpec = tween(300)) + shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        // REWIND 10s BUTTON
                        IconButton(
                            onClick = {
                                val newPos = (viewModel.exoPlayer.currentPosition - 10000).coerceAtLeast(0)
                                viewModel.exoPlayer.seekTo(newPos)
                                currentPosition = newPos
                            },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 24.dp)
                                .size(50.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FastRewind,
                                contentDescription = "Rewind 10s",
                                tint = Color.White
                            )
                        }

                        // PLAY/PAUSE BIG GLOW BUTTON
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(64.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(CineRed.copy(alpha = 0.4f), Color.Transparent)
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { viewModel.togglePlaybackState() },
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(Color.White.copy(alpha = 0.25f), CircleShape)
                                    .border(BorderStroke(1.5.dp, Color.White.copy(alpha = 0.4f)), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // FORWARD 10s BUTTON
                        IconButton(
                            onClick = {
                                val newPos = (viewModel.exoPlayer.currentPosition + 10000).coerceAtMost(videoDuration)
                                viewModel.exoPlayer.seekTo(newPos)
                                currentPosition = newPos
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 24.dp)
                                .size(50.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FastForward,
                                contentDescription = "Forward 10s",
                                tint = Color.White
                            )
                        }

                        // BOTTOM BAR OVERLAYS (Timeline & Slider)
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            // Video time labels
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatTime(currentPosition),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatTime(videoDuration),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Custom Red Seek Bar slider mimicking YouTube
                            Slider(
                                value = currentPosition.toFloat(),
                                onValueChange = { value ->
                                    userIsDraggingSlider = true
                                    currentPosition = value.toLong()
                                },
                                onValueChangeFinished = {
                                    userIsDraggingSlider = false
                                    viewModel.exoPlayer.seekTo(currentPosition)
                                },
                                valueRange = 0f..videoDuration.toFloat().coerceAtLeast(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = CineRed,
                                    activeTrackColor = CineRed,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.24f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .height(16.dp)
                            )

                            // Playback action options bar (Resize mode, Speed)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Aspect Ratio toggle
                                IconButton(
                                    onClick = {
                                        resizeMode = when (resizeMode) {
                                            androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT -> androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                            androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                                            else -> androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AspectRatio,
                                        contentDescription = "Resize Viewport",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Playback Speed
                                Text(
                                    text = "${videoSpeed}x",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .clickable {
                                            videoSpeed = when (videoSpeed) {
                                                1.0f -> 1.25f
                                                1.25f -> 1.5f
                                                1.5f -> 2.0f
                                                2.0f -> 0.75f
                                                else -> 1.0f
                                            }
                                            viewModel.exoPlayer.setPlaybackSpeed(videoSpeed)
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Middle Content & Action Controls Scroll Panel
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Movie Action Bar (Favorites, Watchlist, Share etc.)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Favorite button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.toggleFavoriteFromDetail() }
                        ) {
                            val isFavorite by viewModel.activeSavedMedia.collectAsState()
                            Icon(
                                imageVector = if (isFavorite?.isFavorite == true) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite?.isFavorite == true) CineRed else Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Favorite", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }

                        // Watchlist icon
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.toggleWatchlistFromDetail() }
                        ) {
                            val savedState by viewModel.activeSavedMedia.collectAsState()
                            Icon(
                                imageVector = if (savedState != null) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                contentDescription = "Watchlist",
                                tint = if (savedState != null) CineGold else Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Watchlist", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }

                        // Quality selection shortcut
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showQualityDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.HighQuality,
                                contentDescription = "Resolution/Streams",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Servers", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }

                        // Help / Report link
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.HelpOutline,
                                contentDescription = "Report Issue",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Report", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                    }
                }

                // Expandable Description Panel
                item {
                    val activeDetail by viewModel.activeDetail.collectAsState()
                    activeDetail?.let { detail ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.02f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .border(
                                    BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { isExpandedDescription = !isExpandedDescription }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "About this project",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Icon(
                                        imageVector = if (isExpandedDescription) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                        contentDescription = "Expand info",
                                        tint = Color.White.copy(alpha = 0.6f)
                                    )
                                }

                                if (isExpandedDescription) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = (detail.overview ?: "").ifEmpty { "No overview database text available for this title." },
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text(
                                            text = "Released: " + (detail.releaseDate ?: detail.firstAirDate ?: "Unknown"),
                                            color = CineGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = "Rating",
                                                tint = CineGold,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = String.format(Locale.getDefault(), "%.1f", detail.voteAverage ?: 0.0),
                                                color = Color.LightGray,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = (detail.overview ?: "").ifEmpty { "Click to expand details and rating." },
                                        color = Color.White.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Recommend up-next header
                item {
                    Text(
                        text = "Related Recommendations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Recommended lists item rows
                items(recommendations) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.02f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                // Load details for the selected recommendation
                                viewModel.loadMediaDetails(item.mediaType ?: "movie", item.id)
                                // If streams has endpoints, prepare playback
                                viewModel.loadPlaybackStreams(
                                    type = item.mediaType ?: "movie",
                                    id = item.id
                                )
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.posterUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp, 75.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = item.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Year: " + item.displayDate.take(4),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Rating",
                                    tint = CineGold,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item.ratingText,
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                viewModel.loadMediaDetails(item.mediaType ?: "movie", item.id)
                                viewModel.loadPlaybackStreams(item.mediaType ?: "movie", item.id)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(CineRed.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Play immediately",
                                tint = CineRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // 4. Stream Source / Quality Selection Dialog
    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = {
                Text(
                    "Select Server Quality Source",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            containerColor = Color(0xFF131622),
            tonalElevation = 6.dp,
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (streams.isEmpty()) {
                        Text(
                            "No additional stream servers provided from the TMDB backend service.",
                            color = Color.LightGray
                        )
                    } else {
                        streams.forEach { stream ->
                            val isSelected = currentUrl == stream.url
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (isSelected) CineRed.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (isSelected) CineRed else Color.White.copy(alpha = 0.08f)
                                        ),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        viewModel.playMediaUrl(
                                            mediaId = viewModel.currentPlayingMediaId.value ?: 0,
                                            title = viewModel.currentPlayingTitle.value,
                                            sub = viewModel.currentPlayingSub.value,
                                            posterUrl = viewModel.currentPlayingPoster.value,
                                            url = stream.url,
                                            referer = stream.referer,
                                            origin = stream.origin
                                        )
                                        showQualityDialog = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = stream.metadata ?: "Primary Server",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Quality: " + (stream.quality?.let { "${it}p" } ?: "Adaptive Auto"),
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = CineRed
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQualityDialog = false }) {
                    Text("Close", color = CineRed, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// Format duration to string 00:00:00
private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
