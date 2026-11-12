package com.example.misw4203_tsdc_vinilos.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
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
import com.example.misw4203_tsdc_vinilos.ui.screens.artistdetail.ArtistDetailScreen
import com.example.misw4203_tsdc_vinilos.ui.screens.collectordetail.CollectorDetailScreen
import com.example.misw4203_tsdc_vinilos.ui.screens.collectors.CollectorListScreen
import kotlinx.serialization.Serializable

/** Catálogo de álbumes (HU01). */
@Serializable
data object AlbumListRoute

/** Detalle de un álbum (HU02). */
@Serializable
data class AlbumDetailRoute(val albumId: Int)

/** Detalle de un artista / agrupación (HU04). */
@Serializable
data class ArtistDetailRoute(val artistId: Int, val isBand: Boolean = false)

/** Listado de coleccionistas (HU05). */
@Serializable
data object CollectorListRoute

/** Detalle de un coleccionista. */
@Serializable
data class CollectorDetailRoute(val collectorId: Int)

/** Grafo de navegación de la aplicación; cada pantalla se registra con una ruta tipada. */
@Composable
fun VinilosNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = navBackStackEntry?.destination.toTab()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            VinilosBottomBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    val reselected = tab == currentTab
                    when (tab) {
                        VinilosTab.ALBUMS -> navController.navigateToTab(AlbumListRoute, reselected)
                        VinilosTab.ARTISTS -> {
                            // Sección de artistas
                        }
                        VinilosTab.COLLECTORS -> navController.navigateToTab(CollectorListRoute, reselected)
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
                    onPerformerClick = { performerId, isBand ->
                        navController.navigate(ArtistDetailRoute(artistId = performerId, isBand = isBand))
                    },
                )
            }
            composable<ArtistDetailRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<ArtistDetailRoute>()
                ArtistDetailScreen(
                    artistId = route.artistId,
                    isBand = route.isBand,
                    onBack = { navController.popBackStack() },
                    onAlbumClick = { albumId ->
                        navController.navigate(AlbumDetailRoute(albumId))
                    },
                    onMusicianClick = { musicianId ->
                        navController.navigate(ArtistDetailRoute(artistId = musicianId, isBand = false))
                    },
                    onBandClick = { bandId ->
                        navController.navigate(ArtistDetailRoute(artistId = bandId, isBand = true))
                    },
                )
            }
            composable<CollectorListRoute> {
                CollectorListScreen(
                    onCollectorClick = { collectorId ->
                        navController.navigate(CollectorDetailRoute(collectorId))
                    }
                )
            }
            composable<CollectorDetailRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<CollectorDetailRoute>()
                CollectorDetailScreen(
                    collectorId = route.collectorId,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

/** Pestaña de la barra inferior a la que pertenece el destino actual. */
private fun NavDestination?.toTab(): VinilosTab = when {
    this == null -> VinilosTab.ALBUMS
    hasRoute<CollectorListRoute>() || hasRoute<CollectorDetailRoute>() -> VinilosTab.COLLECTORS
    hasRoute<ArtistDetailRoute>() -> VinilosTab.ARTISTS
    else -> VinilosTab.ALBUMS
}

/**
 * Navega a la pantalla principal de una pestaña. Al cambiar de pestaña, las pantallas de la que se
 * deja se guardan junto con sus ViewModels y se restauran al volver, de modo que no se repiten las
 * consultas a la API ni se pierde la página o el filtro. Volver a tocar la pestaña activa regresa
 * a su pantalla principal.
 */
private fun <T : Any> NavHostController.navigateToTab(root: T, reselected: Boolean) {
    if (reselected && popBackStack(route = root, inclusive = false)) return
    navigate(root) {
        popUpTo(AlbumListRoute) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
