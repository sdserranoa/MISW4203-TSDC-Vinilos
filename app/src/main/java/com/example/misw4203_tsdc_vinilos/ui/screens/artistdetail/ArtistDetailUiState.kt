package com.example.misw4203_tsdc_vinilos.ui.screens.artistdetail

import androidx.compose.runtime.Immutable
import com.example.misw4203_tsdc_vinilos.model.ArtistDetail

/**
 * Estados posibles de la pantalla de detalle de un artista o agrupación (HU04).
 * Marcada con [@Immutable] para optimizar la estabilidad en el compilador de Compose.
 */
@Immutable
sealed interface ArtistDetailUiState {

    @Immutable
    data object Loading : ArtistDetailUiState

    @Immutable
    data object Error : ArtistDetailUiState

    @Immutable
    data class Success(
        val artist: ArtistDetail,
        val isFavorite: Boolean = false,
    ) : ArtistDetailUiState
}
