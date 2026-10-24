package com.example.misw4203_tsdc_vinilos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.misw4203_tsdc_vinilos.ui.screens.albums.AlbumListScreen
import kotlinx.serialization.Serializable

/** Catálogo de álbumes (HU01). */
@Serializable
data object AlbumListRoute

/** Grafo de navegación de la aplicación; cada pantalla se registra con una ruta tipada. */
@Composable
fun VinilosNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AlbumListRoute,
        modifier = modifier,
    ) {
        composable<AlbumListRoute> {
            AlbumListScreen()
        }
    }
}
