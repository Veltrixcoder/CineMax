package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.ui.components.MiniPlayer
import com.example.ui.components.LiquidGlassBackground
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.di.Dependencies
import com.example.ui.screens.*
import com.example.ui.theme.CineRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MainViewModelFactory


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val repository = Dependencies.getRepository(applicationContext)
            val viewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(application, repository)
            )
            
            val isDark by viewModel.isDarkMode.collectAsState()
            
            MyApplicationTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Determine whether to show bottom bar (hide it on details and full-screen player)
                val showBottomBar = currentRoute != null && 
                        !currentRoute.startsWith("details") &&
                        currentRoute != "player"

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = Color(0xCC0D0E15),
                                tonalElevation = 8.dp,
                                modifier = Modifier
                                    .testTag("app_bottom_nav_bar")
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .border(
                                        BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                        RoundedCornerShape(24.dp)
                                    )
                            ) {
                                // Home Item
                                NavigationBarItem(
                                    selected = currentRoute == "home",
                                    onClick = {
                                        if (currentRoute != "home") {
                                            navController.navigate("home") {
                                                popUpTo("home") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == "home") Icons.Filled.Home else Icons.Outlined.Home,
                                            contentDescription = "Home"
                                        )
                                    },
                                    label = { Text("Home") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = CineRed,
                                        selectedTextColor = CineRed,
                                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                )

                                // Search Item
                                NavigationBarItem(
                                    selected = currentRoute == "search",
                                    onClick = {
                                        if (currentRoute != "search") {
                                            navController.navigate("search") {
                                                popUpTo("home") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == "search") Icons.Filled.Search else Icons.Outlined.Search,
                                            contentDescription = "Search"
                                        )
                                    },
                                    label = { Text("Search") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = CineRed,
                                        selectedTextColor = CineRed,
                                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                )

                                // Anime Item (dedicated separate page for Anime)
                                NavigationBarItem(
                                    selected = currentRoute == "anime",
                                    onClick = {
                                        if (currentRoute != "anime") {
                                            navController.navigate("anime") {
                                                popUpTo("home") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == "anime") Icons.Filled.Tv else Icons.Outlined.Tv,
                                            contentDescription = "Anime"
                                        )
                                    },
                                    label = { Text("Anime") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = CineRed,
                                        selectedTextColor = CineRed,
                                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                )

                                // Library Item (saved watchlist / favorites)
                                NavigationBarItem(
                                    selected = currentRoute == "library",
                                    onClick = {
                                        if (currentRoute != "library") {
                                            navController.navigate("library") {
                                                popUpTo("home") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == "library") Icons.Filled.Bookmark else Icons.Outlined.Bookmark,
                                            contentDescription = "Library"
                                        )
                                    },
                                    label = { Text("Library") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = CineRed,
                                        selectedTextColor = CineRed,
                                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                )

                                // Settings Item
                                NavigationBarItem(
                                    selected = currentRoute == "settings",
                                    onClick = {
                                        if (currentRoute != "settings") {
                                            navController.navigate("settings") {
                                                popUpTo("home") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == "settings") Icons.Filled.Settings else Icons.Outlined.Settings,
                                            contentDescription = "Settings"
                                        )
                                    },
                                    label = { Text("Settings") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = CineRed,
                                        selectedTextColor = CineRed,
                                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    LiquidGlassBackground {
                        Box(modifier = Modifier.fillMaxSize()) {
                            NavHost(
                            navController = navController,
                            startDestination = "home",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // Home
                            composable("home") {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToDetail = { mediaType, id ->
                                        navController.navigate("details/$mediaType/$id")
                                    }
                                )
                            }

                            // Search
                            composable("search") {
                                SearchScreen(
                                    viewModel = viewModel,
                                    onNavigateToDetail = { mediaType, id ->
                                        navController.navigate("details/$mediaType/$id")
                                    }
                                )
                            }

                            // Anime (Separate page)
                            composable("anime") {
                                AnimeScreen(
                                    viewModel = viewModel,
                                    onNavigateToDetail = { mediaType, id ->
                                        navController.navigate("details/$mediaType/$id")
                                    }
                                )
                            }

                            // Library
                            composable("library") {
                                LibraryScreen(
                                    viewModel = viewModel,
                                    onNavigateToDetail = { mediaType, id ->
                                        navController.navigate("details/$mediaType/$id")
                                    }
                                )
                            }

                            // Settings
                            composable("settings") {
                                SettingsScreen(viewModel = viewModel)
                            }

                            // Detailed Screen (id is parsed as Int, mediaType as String)
                            composable(
                                route = "details/{mediaType}/{id}",
                                arguments = listOf(
                                    navArgument("mediaType") { type = NavType.StringType },
                                    navArgument("id") { type = NavType.IntType }
                                )
                            ) { backStackEntry ->
                                val mediaType = backStackEntry.arguments?.getString("mediaType") ?: "movie"
                                val id = backStackEntry.arguments?.getInt("id") ?: 0
                                DetailScreen(
                                    mediaType = mediaType,
                                    id = id,
                                    viewModel = viewModel,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }

                            // Full Screen Player destination
                            composable("player") {
                                PlayerScreen(
                                    viewModel = viewModel,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }
                        }

                        // Persistent YouTube-style MiniPlayer just above bottom bar
                        if (currentRoute != "player") {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = if (showBottomBar) 1.dp else 12.dp)
                            ) {
                                MiniPlayer(
                                    viewModel = viewModel,
                                    onMaximizeClick = {
                                        navController.navigate("player") {
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            }
        }
    }
}
