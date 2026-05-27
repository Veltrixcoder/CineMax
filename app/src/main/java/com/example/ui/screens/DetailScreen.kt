package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineRed
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DetailScreen(
    mediaType: String,
    id: Int,
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isMovie = mediaType.equals("movie", ignoreCase = true)

    // Trigger details load on opening
    LaunchedEffect(mediaType, id) {
        viewModel.loadMediaDetails(mediaType, id)
        if (isMovie) {
            viewModel.loadPlaybackStreams(mediaType, id)
        } else {
            viewModel.loadPlaybackStreams(mediaType, id, 1, 1)
        }
    }

    val detail by viewModel.activeDetail.collectAsState()
    val isLoading by viewModel.isLoadingDetails.collectAsState()
    val errorMsg by viewModel.detailsError.collectAsState()

    val streams by viewModel.playbackStreams.collectAsState()
    val isLoadingPlayback by viewModel.isLoadingPlayback.collectAsState()

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090A0E)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = CineRed, strokeWidth = 3.dp)
        }
    } else if (errorMsg != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090A0E))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = errorMsg ?: "Failed to find movie",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { viewModel.loadMediaDetails(mediaType, id) },
                    colors = ButtonDefaults.buttonColors(containerColor = CineRed)
                ) {
                    Text("Retry", color = Color.White)
                }
            }
        }
    } else if (detail != null) {
        val activeDetail = detail!!

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF090A0E))
        ) {
            // 1. Cinematic Full-screen Blur Backdrop / Poster Background
            if (!activeDetail.posterUrl.isEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(activeDetail.posterUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = activeDetail.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (!activeDetail.backdropUrl.isEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(activeDetail.backdropUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = activeDetail.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. High-Fidelity Layer Scrim Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.65f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // 3. Floating Glass Back Button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(46.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), CircleShape)
                    .testTag("detail_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // 4. Center Concentric Pulsing Glass Play Button with state feedback
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )

            val rotationDegrees by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(6000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotateGlass"
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(150.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                ) {
                    // Outermost ambient glow circle
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(8.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(CineRed.copy(alpha = 0.4f), Color.Transparent)
                                ),
                                shape = CircleShape
                            )
                    )

                    // Inner Frosted glass action orb
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .border(
                                BorderStroke(1.5.dp, Color.White.copy(alpha = 0.25f)),
                                CircleShape
                            )
                            .clickable {
                                val streamToPlay = streams.firstOrNull()
                                viewModel.playMediaUrl(
                                    mediaId = activeDetail.id,
                                    title = activeDetail.displayName,
                                    sub = if (isMovie) {
                                        "Movie" + (activeDetail.releaseDate?.take(4)?.let { " • $it" } ?: "")
                                    } else {
                                        "Season 1 Ep 1" + (activeDetail.firstAirDate?.take(4)?.let { " • $it" } ?: "")
                                    },
                                    posterUrl = activeDetail.posterUrl,
                                    url = streamToPlay?.url ?: "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                                    referer = streamToPlay?.referer,
                                    origin = streamToPlay?.origin
                                )
                                onPlayClick()
                            }
                            .testTag("btn_play_main")
                    ) {
                        if (isLoadingPlayback) {
                            CircularProgressIndicator(
                                color = CineRed,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(46.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play Video",
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Title and Subtitle at bottom of Center layout
                Text(
                    text = activeDetail.displayName,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("detail_title_main")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isMovie) "MOVIE" else "TV SERIES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = CineRed,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = activeDetail.displayDate.take(4),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )

                    if (activeDetail.voteAverage != null && activeDetail.voteAverage > 0.0) {
                        Text(
                            text = "★ ${activeDetail.ratingText}",
                            style = MaterialTheme.typography.labelMedium,
                            color = CineGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
