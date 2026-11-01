package com.example.misw4203_tsdc_vinilos.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.misw4203_tsdc_vinilos.ui.components.VinilosBottomBar
import com.example.misw4203_tsdc_vinilos.ui.components.VinilosTab
import com.example.misw4203_tsdc_vinilos.ui.screens.albumdetail.AlbumDetailScreen
import com.example.misw4203_tsdc_vinilos.ui.screens.albums.AlbumListScreen
import kotlinx.serialization.Serializable

/** Catálogo de álbumes (HU01). */
@Serializable
data object AlbumListRoute

/** Detalle de un álbum (HU02). */
@Serializable
data class AlbumDetailRoute(val albumId: Int)

/** Grafo de navegación de la aplicación; cada pantalla se registra con una ruta tipada. */
@Composable
fun VinilosNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentTab = when {
        currentDestination?.hasRoute<AlbumListRoute>() == true -> VinilosTab.ALBUMS
        else -> VinilosTab.ALBUMS
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            VinilosBottomBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    when (tab) {
                        VinilosTab.ALBUMS -> {
                            if (currentDestination?.hasRoute<AlbumListRoute>() != true) {
                                navController.navigate(AlbumListRoute) {
                                    popUpTo(AlbumListRoute) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }
                        VinilosTab.ARTISTS -> {
                            // Por ahora sin navegación según el requerimiento
                        }
                        VinilosTab.COLLECTORS -> {
                            // Por ahora sin navegación según el requerimiento
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AlbumListRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<AlbumListRoute> {
                AlbumListScreen(
                    onAlbumClick = { albumId ->
                        navController.navigate(AlbumDetailRoute(albumId))
                    }
                )
            }
            composable<AlbumDetailRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<AlbumDetailRoute>()
                AlbumDetailScreen(
                    albumId = route.albumId,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
