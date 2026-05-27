package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaItem
import com.example.ui.components.ErrorLoadingBox
import com.example.ui.components.FullScreenLoading
import com.example.ui.components.MediaRow
import com.example.ui.theme.CineRed
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (mediaType: String, id: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val trendingM by viewModel.trendingMovies.collectAsState()
    val trendingT by viewModel.trendingTv.collectAsState()
    val popularM by viewModel.popularMovies.collectAsState()
    val topRatedM by viewModel.topRatedMovies.collectAsState()
    val isLoading by viewModel.isLoadingHome.collectAsState()
    val errorMsg by viewModel.homeError.collectAsState()

    if (isLoading && trendingM.isEmpty()) {
        FullScreenLoading()
    } else if (errorMsg != null && trendingM.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ErrorLoadingBox(message = errorMsg ?: "Connection Error", onRetry = { viewModel.loadHomeData() })
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Banner Feature
            val featuredItem = trendingM.firstOrNull() ?: popularM.firstOrNull()
            if (featuredItem != null) {
                HeroBanner(
                    item = featuredItem,
                    onClick = { onNavigateToDetail("movie", featuredItem.id) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Carousel Rows
            MediaRow(
                title = "Trending Movies",
                items = trendingM,
                customMediaType = "movie",
                onCardClick = { onNavigateToDetail("movie", it.id) }
            )

            MediaRow(
                title = "Hot TV Series",
                items = trendingT,
                customMediaType = "tv",
                onCardClick = { onNavigateToDetail("tv", it.id) }
            )

            MediaRow(
                title = "Popular Cinema",
                items = popularM,
                customMediaType = "movie",
                onCardClick = { onNavigateToDetail("movie", it.id) }
            )

            MediaRow(
                title = "All Time Masterpieces",
                items = topRatedM,
                customMediaType = "movie",
                onCardClick = { onNavigateToDetail("movie", it.id) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HeroBanner(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .clickable { onClick() }
            .testTag("hero_banner")
    ) {
        // Backdrop image
        if (!item.backdropUrl.isEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.backdropUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (!item.posterUrl.isEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.posterUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Multilayer Scrim Shadow for details contrasts
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.95f)
                        ),
                        startY = 100f
                    )
                )
        )

        // Contents
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tag
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(CineRed, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "TRENDING",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                if (item.voteAverage != null && item.voteAverage > 0.0) {
                    Text(
                        text = "★ ${item.ratingText}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Title
            Text(
                text = item.displayName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 36.sp
            )

            // Overview/Tagline
            if (!item.overview.isNullOrEmpty()) {
                Text(
                    text = item.overview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            }

            // Play / Info Buttons Row
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CineRed),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("hero_play_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Details",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Explore Show",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                FilledTonalButton(
                    onClick = onClick,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Details info"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Info",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
