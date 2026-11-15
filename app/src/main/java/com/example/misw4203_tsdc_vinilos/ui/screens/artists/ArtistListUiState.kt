package com.example.misw4203_tsdc_vinilos.ui.screens.artists

import com.example.misw4203_tsdc_vinilos.model.Performer

/** Filtro por tipo de artista en la HU03. */
enum class ArtistFilterType {
    ALL,
    BANDS,
    MUSICIANS,
}

/** Estado de la interfaz del listado de artistas (HU03). */
sealed interface ArtistListUiState {
    data object Loading : ArtistListUiState
    data object Empty : ArtistListUiState
    data object Error : ArtistListUiState
    data class Success(
        val artists: List<Performer>,
        val totalArtists: Int,
        val currentPage: Int,
        val totalPages: Int,
        val searchQuery: String = "",
        val filterType: ArtistFilterType = ArtistFilterType.ALL,
    ) : ArtistListUiState {
        val hasPreviousPage: Boolean get() = currentPage > 1
        val hasNextPage: Boolean get() = currentPage < totalPages
    }
}
