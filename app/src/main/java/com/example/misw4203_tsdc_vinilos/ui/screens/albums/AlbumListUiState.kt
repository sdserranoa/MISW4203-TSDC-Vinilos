package com.example.misw4203_tsdc_vinilos.ui.screens.albums

import com.example.misw4203_tsdc_vinilos.model.Album

/** Estados posibles de la pantalla del catálogo de álbumes (HU01). */
sealed interface AlbumListUiState {

    data object Loading : AlbumListUiState

    data object Empty : AlbumListUiState

    data object Error : AlbumListUiState

    /**
     * @property albums álbumes de la página actual.
     * @property totalAlbums cantidad total de álbumes del catálogo.
     * @property currentPage página actual, empezando en 1.
     */
    data class Success(
        val albums: List<Album>,
        val totalAlbums: Int,
        val currentPage: Int,
        val totalPages: Int,
    ) : AlbumListUiState {
        val hasPreviousPage: Boolean
            get() = currentPage > 1
        val hasNextPage: Boolean
            get() = currentPage < totalPages
    }
}
