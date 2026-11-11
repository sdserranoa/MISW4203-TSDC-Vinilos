package com.example.misw4203_tsdc_vinilos.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.misw4203_tsdc_vinilos.ui.components.VinilosBottomBar
import com.example.misw4203_tsdc_vinilos.ui.components.VinilosTab
import com.example.misw4203_tsdc_vinilos.ui.screens.addtrack.AddTrackScreen
import com.example.misw4203_tsdc_vinilos.ui.screens.albumCreate.AlbumCreateScreen
import com.example.misw4203_tsdc_vinilos.ui.screens.albumdetail.AlbumDetailScreen
import com.example.misw4203_tsdc_vinilos.ui.screens.albums.AlbumListScreen
import com.example.misw4203_tsdc_vinilos.ui.screens.artistdetail.ArtistDetailScreen
import com.example.misw4203_tsdc_vinilos.ui.screens.artists.ArtistListScreen
import com.example.misw4203_tsdc_vinilos.ui.screens.collectordetail.CollectorDetailScreen
import com.example.misw4203_tsdc_vinilos.ui.screens.collectors.CollectorListScreen
import kotlinx.serialization.Serializable

/** Catálogo de álbumes (HU01). */
@Serializable
data object AlbumListRoute

/** Detalle de un álbum (HU02). */
@Serializable
data class AlbumDetailRoute(val albumId: Int)

/** Asociar canción a un álbum (HU08). */
@Serializable
data class AddTrackRoute(val albumId: Int)

/** Listado de artistas (HU03). */
@Serializable
data object ArtistListRoute

/** Detalle de un artista / agrupación (HU04). */
@Serializable
data class ArtistDetailRoute(val artistId: Int, val isBand: Boolean = false)

/** Listado de coleccionistas (HU05). */
@Serializable
data object CollectorListRoute

/** Detalle de un coleccionista. */
@Serializable
data class CollectorDetailRoute(val collectorId: Int)

/** Crear un nuevo álbum (HU07). */
@Serializable
data object AlbumCreateRoute

/** Grafo de navegación de la aplicación; cada pantalla se registra con una ruta tipada. */
@Composable
fun VinilosNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = remember(navBackStackEntry) { navController.currentTab() }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            VinilosBottomBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    val reselected = tab == currentTab
                    when (tab) {
                        VinilosTab.ALBUMS -> navController.navigateToTab(AlbumListRoute, reselected)
                        VinilosTab.ARTISTS -> navController.navigateToTab(ArtistListRoute, reselected)
                        VinilosTab.COLLECTORS -> navController.navigateToTab(CollectorListRoute, reselected)
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AlbumListRoute,
            // Las barras del sistema ya se descuentan aquí: se marcan como consumidas para que el
            // Scaffold y la barra superior de cada pantalla no vuelvan a sumar esos márgenes.
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
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
                val trackAdded by backStackEntry.savedStateHandle
                    .getStateFlow("track_added", false)
                    .collectAsStateWithLifecycle()
                AlbumDetailScreen(
                    albumId = route.albumId,
                    onBack = { navController.popBackStack() },
                    refreshOnTrackAdded = trackAdded,
                    onTrackAddedConsumed = {
                        backStackEntry.savedStateHandle["track_added"] = false
                    },
                    onAddTrackClick = { albumId ->
                        navController.navigate(AddTrackRoute(albumId))
                    },
                    onPerformerClick = { performerId, isBand ->
                        navController.navigate(ArtistDetailRoute(artistId = performerId, isBand = isBand))
                    },
                )
            }
            composable<AddTrackRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<AddTrackRoute>()
                AddTrackScreen(
                    albumId = route.albumId,
                    onBack = { navController.popBackStack() },
                    onSuccess = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("track_added", true)
                        navController.popBackStack()
                    },
                    onErrorBack = { navController.popBackStack() },
                )
            }
            composable<ArtistListRoute> {
                ArtistListScreen(
                    onArtistClick = { artistId, isBand ->
                        navController.navigate(ArtistDetailRoute(artistId = artistId, isBand = isBand))
                    }
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
                    onAlbumClick = { albumId ->
                        navController.navigate(AlbumDetailRoute(albumId))
                    },
                    onPerformerClick = { performerId, isBand ->
                        navController.navigate(ArtistDetailRoute(artistId = performerId, isBand = isBand))
                    },
                )
            }
            composable<AlbumCreateRoute> {
                AlbumCreateScreen(
                    onAlbumSubmit = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

/**
 * Pestaña activa de la barra inferior. Al cambiar de pestaña, las pantallas de la anterior salen de
 * la pila, así que la sección de coleccionistas está activa mientras su listado esté en la pila,
 * incluso cuando encima se abre el detalle de un álbum o de un artista desde un coleccionista.
 */
private fun NavHostController.currentTab(): VinilosTab {
    val collectorsInBackStack = runCatching { getBackStackEntry<CollectorListRoute>() }.isSuccess
    val artistsInBackStack = runCatching { getBackStackEntry<ArtistListRoute>() }.isSuccess
    return when {
        collectorsInBackStack -> VinilosTab.COLLECTORS
        artistsInBackStack || currentDestination?.hasRoute<ArtistDetailRoute>() == true -> VinilosTab.ARTISTS
        else -> VinilosTab.ALBUMS
    }
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
