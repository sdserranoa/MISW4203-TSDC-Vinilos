package com.example.misw4203_tsdc_vinilos.ui.screens.albumCreate

import com.example.misw4203_tsdc_vinilos.model.Album

/** Estados posibles de la pantalla de creación de un álbum (HU07). */
sealed interface AlbumCreateUiState {

    data object Loading : AlbumCreateUiState

    data object Error : AlbumCreateUiState

    data class Success(
        val album: Album,
        val isSaving: Boolean = false,
        val isCreated: Boolean = false,
        val showSnackbar: Boolean = false,
        val errorMessage: String? = null,
    ) : AlbumCreateUiState
}
