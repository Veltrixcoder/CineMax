package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
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
fun AnimeScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (mediaType: String, id: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val animeTvList by viewModel.animeShows.collectAsState()
    val animeMovieList by viewModel.animeMovies.collectAsState()
    val isLoading by viewModel.isLoadingAnime.collectAsState()
    val errorMsg by viewModel.animeError.collectAsState()

    if (isLoading && animeTvList.isEmpty()) {
        FullScreenLoading()
    } else if (errorMsg != null && animeTvList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ErrorLoadingBox(message = errorMsg ?: "Connection Error", onRetry = { viewModel.loadAnimeData() })
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .verticalScroll(rememberScrollState())
        ) {
            // Anime Featured Banner Header
            val featuredAnime = animeTvList.firstOrNull() ?: animeMovieList.firstOrNull()
            if (featuredAnime != null) {
                AnimeFeaturedBanner(
                    item = featuredAnime,
                    onClick = { onNavigateToDetail("tv", featuredAnime.id) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // TV Series Rows
            MediaRow(
                title = "Popular Anime Series",
                items = animeTvList,
                customMediaType = "tv",
                onCardClick = { onNavigateToDetail("tv", it.id) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Movie Rows
            MediaRow(
                title = "Blockbuster Anime Movies",
                items = animeMovieList,
                customMediaType = "movie",
                onCardClick = { onNavigateToDetail("movie", it.id) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AnimeFeaturedBanner(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clickable { onClick() }
            .testTag("anime_banner")
    ) {
        // Background
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

        // Overlay Shading Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // Contents
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF673AB7), shape = RoundedCornerShape(4.dp)) // Anime branded Purple accent tag!
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ANIME FORCE",
                        fontSize = 9.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                if (item.voteAverage != null && item.voteAverage > 0.0) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = item.ratingText,
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Title
            Text(
                text = item.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Overview
            if (!item.overview.isNullOrEmpty()) {
                Text(
                    text = item.overview,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            // Simple Play Overlay button
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .background(CineRed, shape = RoundedCornerShape(6.dp))
                    .clickable { onClick() }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Enter details",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "See Details",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
