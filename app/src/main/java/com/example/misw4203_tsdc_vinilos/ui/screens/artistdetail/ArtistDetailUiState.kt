package com.example.misw4203_tsdc_vinilos.ui.screens.artistdetail

import com.example.misw4203_tsdc_vinilos.model.ArtistDetail

/** Estados posibles de la pantalla de detalle de un artista o agrupación (HU04). */
sealed interface ArtistDetailUiState {

    data object Loading : ArtistDetailUiState

    data object Error : ArtistDetailUiState

    data class Success(
        val artist: ArtistDetail,
        val isFavorite: Boolean = false,
    ) : ArtistDetailUiState
}
